package com.rips7.day;

import com.rips7.util.Util;
import com.rips7.util.maths.Maths;

import java.util.Arrays;
import java.util.stream.IntStream;

public class Day13 implements Day<Integer> {

    @Override
    public Integer part1(String input) {
        return Arrays.stream(input.split("\n\n"))
            .map(Mirror::parse)
            .map(Mirror::evaluateReflection)
            .reduce(Integer::sum)
            .orElseThrow();
    }

    @Override
    public Integer part2(String input) {
        return Arrays.stream(input.split("\n\n"))
            .map(Mirror::parse)
            .map(Mirror::evaluateReflectionWithSmudge)
            .reduce(Integer::sum)
            .orElseThrow();
    }

    private record Mirror(Character[][] pattern) {
        private static Mirror parse(final String input) {
            final Character[][] pattern = Arrays.stream(input.split("\n"))
                .map(row -> row.chars()
                    .mapToObj(c -> (char) c)
                    .toArray(Character[]::new))
                .toArray(Character[][]::new);
            return new Mirror(pattern);
        }

        private int evaluateReflection() {
            return evaluateReflection(false);
        }

        private int evaluateReflectionWithSmudge() {
            return evaluateReflection(true);
        }

        private int evaluateReflection(boolean withSmudge) {
            final int rowReflection = findReflection(pattern, withSmudge);
            if (rowReflection != 0) {
                return 100 * rowReflection;
            }
            return findReflection(Maths.transpose(Character.class, pattern), withSmudge);
        }

        private int findReflection(final Character[][] pattern, boolean withSmudge) {
            for (int r = 1; r < pattern.length; r++) {
                final Character[][] below = Util.slice2D(Character.class, pattern, r, pattern.length);
                final Character[][] above = Util.slice2D(Character.class, pattern, 0, r);
                final Character[][] aboveInv = Util.invert2D(Character.class, above);
                final int overlapSize = Math.min(aboveInv.length, below.length);
                final Character[][] aboveOverlap = Util.slice2D(Character.class, aboveInv, 0, overlapSize);
                final Character[][] belowOverlap = Util.slice2D(Character.class, below, 0, overlapSize);
                if (!withSmudge && Util.equal2D(aboveOverlap, belowOverlap)) {
                    return r;
                } else if (withSmudge) {
                    final int diff = IntStream.range(0, aboveOverlap.length)
                        .mapToObj(i -> IntStream.range(0, aboveOverlap[i].length)
                            .mapToObj(j -> aboveOverlap[i][j].equals(belowOverlap[i][j]) ? 0 : 1)
                            .reduce(Integer::sum)
                            .orElseThrow())
                        .reduce(Integer::sum)
                        .orElseThrow();
                    if (diff == 1) {
                        return r;
                    }
                }
            }
            return 0;
        }
    }
}
