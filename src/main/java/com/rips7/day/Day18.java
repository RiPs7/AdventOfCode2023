package com.rips7.day;

import com.rips7.util.Util;
import com.rips7.util.maths.Maths.Vector2D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day18 implements Day<Long> {

    @Override
    public Long part1(String input) {
        final Plan plan = Plan.parse(input, true);
        final List<Vector2D<Integer>> loop = plan.dig();
        return Util.findLoopArea(loop);
    }

    @Override
    public Long part2(String input) {
        final Plan plan = Plan.parse(input, false);
        final List<Vector2D<Integer>> loop = plan.dig();
        return Util.findLoopArea(loop);
    }

    private record Plan(List<PlanEntry> entries) {
        private static Plan parse(final String input, final boolean part1) {
            final List<PlanEntry> entries = Arrays.stream(input.split("\n"))
                .map(line -> PlanEntry.parse(line, part1))
                .toList();
            return new Plan(entries);
        }

        private List<Vector2D<Integer>> dig() {
            final List<Vector2D<Integer>> loop = new ArrayList<>();
            final AtomicReference<Vector2D<Integer>> currentPosition = new AtomicReference<>(Vector2D.of(0, 0));
            loop.add(currentPosition.get());
            entries.forEach(entry -> {
                final Vector2D<Integer> nextPosition = entry.direction.apply(currentPosition.get(), entry.steps);
                loop.add(nextPosition);
                currentPosition.set(nextPosition);
            });
            return loop;
        }
    }

    private record PlanEntry(Direction direction, int steps) {
        private static final Pattern ENTRY_PATTERN = Pattern.compile("(?<dir>[UDLR]) (?<steps>\\d+) \\((?<color>.+)\\)");
        private static PlanEntry parse(final String input, final boolean part1) {
            final Matcher matcher = ENTRY_PATTERN.matcher(input);
            if (matcher.matches()) {
                if (part1) {
                    final Direction direction = Direction.from(matcher.group("dir").charAt(0));
                    final int steps = Integer.parseInt(matcher.group("steps"));
                    return new PlanEntry(direction, steps);
                } else {
                    final String color = matcher.group("color");
                    final Direction direction = switch (color.charAt(color.length() - 1)) {
                        case '0' -> Direction.RIGHT;
                        case '1' -> Direction.DOWN;
                        case '2' -> Direction.LEFT;
                        case '3' -> Direction.UP;
                        default -> throw new RuntimeException("Cannot parse direction");
                    };
                    final int steps = Integer.parseInt(color.substring(1, color.length() - 1), 16);
                    return new PlanEntry(direction, steps);
                }
            }
            throw new RuntimeException("Cannot parse %s".formatted(input));
        }
    }

    private enum Direction {
        UP('U', Vector2D.of(-1, 0)),
        DOWN('D', Vector2D.of(1, 0)),
        LEFT('L', Vector2D.of(0, -1)),
        RIGHT('R', Vector2D.of(0, 1));

        private final char value;
        private final Vector2D<Integer> offset;

        Direction(final char value, final Vector2D<Integer> offset) {
            this.value = value;
            this.offset = offset;
        }

        private static Direction from(final char c) {
            return Arrays.stream(values())
                .filter(dir -> dir.value == c)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot parse %s as direction".formatted(c)));
        }

        private Vector2D<Integer> apply(final Vector2D<Integer> position, int times) {
            final Vector2D<Integer> scaledOffset = Vector2D.of(offset.x() * times, offset.y() * times);
            return Vector2D.add(position, scaledOffset, Integer::sum);
        }
    }
}