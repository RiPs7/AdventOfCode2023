package com.rips7.day;

import com.rips7.util.maths.Combinatorics;
import com.rips7.util.maths.Combinatorics.Pair;
import com.rips7.util.maths.Maths;
import com.rips7.util.maths.Maths.Vector2D;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.rips7.util.Util.lines;

public class Day24 implements Day<Long> {

    @Override
    public Long part1(String input) {
        final List<Hailstone> hailstones = lines(input).map(Hailstone::parse).toList();
        final List<Pair<Hailstone, Hailstone>> hailstonePairs = Combinatorics.unorderedPairs(hailstones, true);
        final BigDecimal min = new BigDecimal("200000000000000");
        final BigDecimal max = new BigDecimal("400000000000000");
        return hailstonePairs.stream()
            .filter(pair -> isValidIntersection(pair.left(), pair.right(), min, max))
            .count();
    }

    @Override
    public Long part2(String input) {
        final List<Hailstone> hailstones = lines(input).map(Hailstone::parse).toList();
        final List<Hailstone> usableHailstones = find3UsableHailstones(hailstones);

        final BigDecimal[][] m = createBDLinearMatrix(usableHailstones);
        Maths.solveGauss(m);
        final BigDecimal rockX = m[0][6];
        final BigDecimal rockY = m[1][6];
        final BigDecimal rockZ = m[2][6];

        return rockX.add(rockY).add(rockZ).setScale(0, RoundingMode.HALF_EVEN).longValue();
    }

    private boolean isValidIntersection(final Hailstone hailstone1, final Hailstone hailstone2, final BigDecimal min,
                                        final BigDecimal max) {
        final LinearTrajectory trajectory1 = hailstone1.getLinearTrajectory();
        final LinearTrajectory trajectory2 = hailstone2.getLinearTrajectory();
        final Vector2D<BigDecimal> intersection = trajectory1.intersection(trajectory2);
        if (intersection == null) {
            return false;
        }
        if (!(Maths.inBetween(intersection.x(), min, max) && Maths.inBetween(intersection.y(), min, max))) {
            return false;
        }
        final BigDecimal timeToIntersection1 = divide(intersection.x().subtract(hailstone1.pos.x()), hailstone1.vel.x());
        final BigDecimal timeToIntersection2 = divide(intersection.x().subtract(hailstone2.pos.x()), hailstone2.vel.x());
        return timeToIntersection1.compareTo(BigDecimal.ZERO) >= 0 && timeToIntersection2.compareTo(BigDecimal.ZERO) >= 0;
    }

    private List<Hailstone> find3UsableHailstones(final List<Hailstone> hailstones) {
        final List<Hailstone> result = new ArrayList<>();
        for (int a = 0; a < hailstones.size() - 2 && result.size() < 3; a++) {
            final Hailstone first = hailstones.get(a);
            for (int b = a + 1; b < hailstones.size() - 1 && result.size() < 3; b++) {
                final Hailstone second = hailstones.get(b);
                if (!first.vel.equals(second.vel)) {
                    for (int c = b + 1; c < hailstones.size() && result.size() < 3; c++) {
                        final Hailstone third = hailstones.get(c);
                        if(!first.vel.equals(third.vel) && !second.vel.equals(third.vel)) {
                            result.add(first);
                            result.add(second);
                            result.add(third);
                        }
                    }
                }
            }
        }
        if (result.size() < 3) {
            throw new RuntimeException("Can not find 3 usable hails");
        }
        return result;
    }

    private static BigDecimal[][] createBDLinearMatrix(final List<Hailstone> hailstones) {
        final Position aPos = hailstones.get(0).pos;
        final Position bPos = hailstones.get(1).pos;
        final Position cPos = hailstones.get(2).pos;
        final Velocity aVel = hailstones.get(0).vel;
        final Velocity bVel = hailstones.get(1).vel;
        final Velocity cVel = hailstones.get(2).vel;

        final BigDecimal d1 = aPos.y.multiply(aVel.x).subtract(aPos.x.multiply(aVel.y));
        final BigDecimal d2 = aPos.x.multiply(aVel.z).subtract(aPos.z.multiply(aVel.x));
        final BigDecimal d3 = aPos.z.multiply(aVel.y).subtract(aPos.y.multiply(aVel.z));

        // See for reference:
        // https://github.com/zebalu/advent-of-code-2023/blob/master/aoc2023/src/main/java/io/github/zebalu/aoc2023/days/Day24.java#L80
        // https://github.com/DeadlyRedCube/AdventOfCode/blob/main/2023/AOC2023/D24.h#L66-L157

        return new BigDecimal[][] {
            {aVel.y.subtract(bVel.y),          aVel.x.subtract(bVel.x).negate(), BigDecimal.ZERO,                  aPos.y.subtract(bPos.y).negate(), aPos.x.subtract(bPos.x),          BigDecimal.ZERO,                  bPos.y.multiply(bVel.x).subtract(bPos.x.multiply(bVel.y)).subtract(d1)},
            {aVel.y.subtract(cVel.y),          aVel.x.subtract(cVel.x).negate(), BigDecimal.ZERO,                  aPos.y.subtract(cPos.y).negate(), aPos.x.subtract(cPos.x),          BigDecimal.ZERO,                  cPos.y.multiply(cVel.x).subtract(cPos.x.multiply(cVel.y)).subtract(d1)},
            {aVel.z.subtract(bVel.z).negate(), BigDecimal.ZERO,                  aVel.x.subtract(bVel.x),          aPos.z.subtract(bPos.z),          BigDecimal.ZERO,                  aPos.x.subtract(bPos.x).negate(), bPos.x.multiply(bVel.z).subtract(bPos.z.multiply(bVel.x)).subtract(d2)},
            {aVel.z.subtract(cVel.z).negate(), BigDecimal.ZERO,                  aVel.x.subtract(cVel.x),          aPos.z.subtract(cPos.z),          BigDecimal.ZERO,                  aPos.x.subtract(cPos.x).negate(), cPos.x.multiply(cVel.z).subtract(cPos.z.multiply(cVel.x)).subtract(d2)},
            {BigDecimal.ZERO,                  aVel.z.subtract(bVel.z),          aVel.y.subtract(bVel.y).negate(), BigDecimal.ZERO,                  aPos.z.subtract(bPos.z).negate(), aPos.y.subtract(bPos.y),          bPos.z.multiply(bVel.y).subtract(bPos.y.multiply(bVel.z)).subtract(d3)},
            {BigDecimal.ZERO,                  aVel.z.subtract(cVel.z),          aVel.y.subtract(cVel.y).negate(), BigDecimal.ZERO,                  aPos.z.subtract(cPos.z).negate(), aPos.y.subtract(cPos.y),          cPos.z.multiply(cVel.y).subtract(cPos.y.multiply(cVel.z)).subtract(d3)}
        };
    }

    private record LinearTrajectory(BigDecimal m, BigDecimal b) {
        private BigDecimal y(final BigDecimal x) {
            return m.multiply(x).add(b);
        }

        private Vector2D<BigDecimal> intersection(final LinearTrajectory other) {
            if (this.m.equals(other.m)) {
                return null;
            }
            final BigDecimal x = divide(other.b.subtract(this.b), this.m.subtract(other.m));
            final BigDecimal y = this.y(x);
            return Vector2D.of(x, y);
        }
    }

    private record Hailstone(Position pos, Velocity vel) implements Comparable<Hailstone> {
        private static final Pattern HAILSTONE_PATTERN =
            Pattern.compile("(?<posX>-?\\d+),\\s+(?<posY>-?\\d+),\\s+(?<posZ>-?\\d+)\\s+@\\s+(?<velX>-?\\d+),\\s+(?<velY>-?\\d+),\\s+(?<velZ>-?\\d+)");

        private static Hailstone parse(final String input) {
            final Matcher matcher = HAILSTONE_PATTERN.matcher(input);
            if (matcher.matches()) {
                final BigDecimal posX = new BigDecimal(matcher.group("posX"));
                final BigDecimal posY = new BigDecimal(matcher.group("posY"));
                final BigDecimal posZ = new BigDecimal(matcher.group("posZ"));
                final BigDecimal velX = new BigDecimal(matcher.group("velX"));
                final BigDecimal velY = new BigDecimal(matcher.group("velY"));
                final BigDecimal velZ = new BigDecimal(matcher.group("velZ"));
                return new Hailstone(Position.of(posX, posY, posZ), Velocity.of(velX, velY, velZ));
            }
            throw new RuntimeException("Cannot parse %s as hailstone entry".formatted(input));
        }

        @Override
        public int compareTo(Hailstone other) {
            return Comparator
                .comparing(Hailstone::pos)
                .thenComparing(Hailstone::vel)
                .compare(this, other);
        }

        private LinearTrajectory getLinearTrajectory() {
            final Vector2D<BigDecimal> pointA = Vector2D.of(pos.x(), pos.y());
            final Vector2D<BigDecimal> pointB = Vector2D.of(pos.x().add(vel.x()), pos.y().add(vel.y()));

            final BigDecimal m = divide(pointB.y().subtract(pointA.y()), pointB.x().subtract(pointA.x()));
            final BigDecimal b = pointA.y().subtract(m.multiply(pointA.x()));
            return new LinearTrajectory(m, b);
        }
    }

    private record Position(BigDecimal x, BigDecimal y, BigDecimal z) implements Comparable<Position> {
        private static Position of(final BigDecimal x, final BigDecimal y, final BigDecimal z) {
            return new Position(x, y, z);
        }

        @Override
        public int compareTo(Position other) {
            return Comparator
                .comparing(Position::x)
                .thenComparing(Position::y)
                .thenComparing(Position::z)
                .compare(this, other);
        }
    }

    private record Velocity(BigDecimal x, BigDecimal y, BigDecimal z) implements Comparable<Velocity> {
        private static Velocity of(final BigDecimal x, final BigDecimal y, final BigDecimal z) {
            return new Velocity(x, y, z);
        }

        @Override
        public int compareTo(Velocity other) {
            return Comparator
                .comparing(Velocity::x)
                .thenComparing(Velocity::y)
                .thenComparing(Velocity::z)
                .compare(this, other);
        }
    }

    private static BigDecimal divide(final BigDecimal a, final BigDecimal b) {
        return a.divide(b, 10, RoundingMode.HALF_UP);
    }
}