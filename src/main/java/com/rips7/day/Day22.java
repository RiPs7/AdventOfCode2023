package com.rips7.day;

import com.rips7.util.Util;
import com.rips7.util.maths.Maths.Vector3D;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Day22 implements Day<Integer> {

    @Override
    public Integer part1(String input) {
        final List<Brick> bricks = Arrays.stream(input.split("\n"))
                .map(Brick::parse)
                .toList();

        final List<Brick> fallenBricks = fall(bricks);

        final StructureInfo structureInfo = StructureInfo.from(fallenBricks);

        return (int) structureInfo.getDisintegrateableBricks();
    }

    @Override
    public Integer part2(String input) {
        final List<Brick> bricks = Arrays.stream(input.split("\n"))
                .map(Brick::parse)
                .toList();

        final List<Brick> fallenBricks = fall(bricks);

        final StructureInfo structureInfo = StructureInfo.from(fallenBricks);

        return (int) structureInfo.getFallableBricksForBestBrickToDisintegrate();
    }

    private List<Brick> fall(final List<Brick> bricks) {
        final List<Brick> fallenBricks = bricks.stream()
                .sorted(Comparator.comparing(b -> b.start.z()))
                .toList();
        Util.enumerate(fallenBricks, (i, brick) -> {
            int maxZ = 1;
            for (final Brick brickBelow : fallenBricks.subList(0, i)) {
                if (brick.overlaps(brickBelow)) {
                    maxZ = Math.max(maxZ, Math.max(brickBelow.start.z(), brickBelow.end.z()) + 1);
                }
            }
            brick.updateZ(maxZ);
        });
        return fallenBricks.stream()
                .sorted(Comparator.comparing(b -> b.start.z()))
                .toList();
    }

    private static final class Brick {
        private static final Pattern BRICK_SNAPSHOT_PATTERN = Pattern.compile("(\\d+),(\\d+),(\\d+)~(\\d+),(\\d+),(\\d+)");
        private Vector3D<Integer> start;
        private Vector3D<Integer> end;

        private Brick(Vector3D<Integer> start, Vector3D<Integer> end) {
            this.start = start;
            this.end = end;
            assert Objects.equals(start.z(), end.z());
        }

        private static Brick parse(final String input) {
            final Matcher matcher = BRICK_SNAPSHOT_PATTERN.matcher(input);
            if (matcher.matches()) {
                return new Brick(
                        Vector3D.of(
                                Integer.parseInt(matcher.group(1)),
                                Integer.parseInt(matcher.group(2)),
                                Integer.parseInt(matcher.group(3))),
                        Vector3D.of(
                                Integer.parseInt(matcher.group(4)),
                                Integer.parseInt(matcher.group(5)),
                                Integer.parseInt(matcher.group(6))));
            }
            throw new RuntimeException("Cannot parse '%s' as brick snapshot");
        }

        private boolean overlaps(final Brick other) {
            return Math.max(this.start.x(), other.start.x()) <= Math.min(this.end.x(), other.end.x()) &&
                    Math.max(this.start.y(), other.start.y()) <= Math.min(this.end.y(), other.end.y());
        }

        private void updateZ(final int newZ) {
            this.end = Vector3D.of(this.end.x(), this.end.y(), this.end.z() - this.start.z() + newZ);
            this.start = Vector3D.of(this.start.x(), this.start.y(), newZ);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Brick) obj;
            return Objects.equals(this.start, that.start) &&
                    Objects.equals(this.end, that.end);
        }

        @Override
        public int hashCode() {
            return Objects.hash(start, end);
        }

        @Override
        public String toString() {
            return "(%s,%s,%s)-(%s,%s,%s)".formatted(start.x(), start.y(), start.z(), end.x(), end.y(), end.z());
        }

    }

    private record StructureInfo(Map<Brick, Set<Brick>> brickSupports, Map<Brick, Set<Brick>> brickSupportedBy) {
        private static StructureInfo from(final List<Brick> bricks) {
            final Map<Brick, Set<Brick>> brickSupports = bricks.stream()
                .collect(Collectors.toMap(Function.identity(), e -> new HashSet<>(), (v1, v2) -> v1, HashMap::new));
            final Map<Brick, Set<Brick>> brickSupportedBy = bricks.stream()
                    .collect(Collectors.toMap(Function.identity(), e -> new HashSet<>(), (v1, v2) -> v1, HashMap::new));
            Util.enumerate(bricks, (j, upper) -> Util.enumerate(bricks.subList(0, j), (i, lower) -> {
                if (lower.overlaps(upper) && upper.start.z() == lower.end.z() + 1) {
                    brickSupports.get(lower).add(upper);
                    brickSupportedBy.get(upper).add(lower);
                }
            }));
            return new StructureInfo(brickSupports, brickSupportedBy);
        }

        private long getDisintegrateableBricks() {
            return brickSupports.values().stream()
                    .filter(supports -> supports.stream().allMatch(support -> brickSupportedBy.get(support).size() >= 2))
                    .count();
        }

        private long getFallableBricksForBestBrickToDisintegrate() {
            final AtomicInteger total = new AtomicInteger();

            brickSupports.keySet().forEach(brick -> {
                final Queue<Brick> toFall = new ArrayDeque<>();
                toFall.add(brick);
                final Set<Brick> falling = new HashSet<>();
                falling.add(brick);

                while (!toFall.isEmpty()) {
                    final Brick current = toFall.poll();
                    brickSupports.get(current).stream()
                            .filter(other -> !falling.contains(other))
                            .filter(other -> falling.containsAll(brickSupportedBy.get(other)))
                            .forEach(other -> {
                                toFall.add(other);
                                falling.add(other);
                            });
                }

                total.addAndGet(falling.size() - 1);
            });

            return total.get();
        }
    }
}