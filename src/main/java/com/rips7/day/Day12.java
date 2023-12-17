package com.rips7.day;

import com.rips7.util.maths.Combinatorics.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

public class Day12 implements Day<Long> {

    @Override
    public Long part1(String input) {
        final List<Record> records = Arrays.stream(input.split("\n"))
            .map(Record::parse)
            .toList();
        final List<Long> arrangements = records.stream()
            .map(Record::findArrangements)
            .toList();
        return arrangements.stream()
            .reduce(Long::sum)
            .orElseThrow();
    }

    @Override
    public Long part2(String input) {
        final List<Record> records = Arrays.stream(input.split("\n"))
            .map(Day12::unfold)
            .map(Record::parse)
            .toList();
        final List<Long> arrangements = records.stream()
            .map(Record::findArrangements)
            .toList();
        return arrangements.stream()
            .reduce(Long::sum)
            .orElseThrow();
    }

    private static String unfold(final String input) {
        final int foldTimes = 5;
        final String[] parts = input.split(" ");
        final String unfoldedSprings = String.join("?", IntStream.range(0, foldTimes).mapToObj(i -> parts[0]).toList());
        final String unfoldedGroupSizes = String.join(",", IntStream.range(0, foldTimes).mapToObj(i -> parts[1]).toList());
        return "%s %s".formatted(unfoldedSprings, unfoldedGroupSizes);
    }

    private record Record(List<Spring> springs, List<Integer> groupSizes) {
        private static Record parse(final String input) {
            final String[] parts = input.split(" ");
            final List<Spring> springs = parts[0].chars().mapToObj(c -> Spring.from((char) c)).toList();
            final List<Integer> groupSizes = Arrays.stream(parts[1].split(",")).map(Integer::parseInt).toList();
            return new Record(springs, groupSizes);
        }

        private long findArrangements() {
            return countArrangements(springs, groupSizes, new HashMap<>());
        }

        private static long countArrangements(final List<Spring> springs, final List<Integer> groupSizes, final Map<Pair<List<Spring>, List<Integer>>, Long> cache) {
            if (springs.isEmpty()) {
                return groupSizes.isEmpty() ? 1 : 0;
            }
            if (groupSizes.isEmpty()) {
                return !springs.contains(Spring.DAMAGED) ? 1 : 0;
            }

            final Pair<List<Spring>, List<Integer>> key = Pair.of(springs, groupSizes);
            if (cache.containsKey(key)) {
                return cache.get(key);
            }

            long count = 0;
            if (Set.of(Spring.OPERATIONAL, Spring.UNKNOWN).contains(springs.get(0))) {
                count += countArrangements(springs.subList(1, springs.size()), groupSizes, cache);
            }
            if (Set.of(Spring.DAMAGED, Spring.UNKNOWN).contains(springs.get(0))) {
                if (groupSizes.get(0) <= springs.size()
                    && !springs.subList(0, groupSizes.get(0)).contains(Spring.OPERATIONAL)
                    && (groupSizes.get(0) == springs.size() || springs.get(groupSizes.get(0)) != Spring.DAMAGED)) {
                    if (groupSizes.get(0) + 1 >= springs.size()) {
                        count += countArrangements(List.of(), groupSizes.subList(1, groupSizes.size()), cache);
                    } else {
                        count += countArrangements(springs.subList(groupSizes.get(0) + 1, springs.size()), groupSizes.subList(1, groupSizes.size()), cache);
                    }
                }
            }

            cache.put(key, count);
            return count;
        }

    }

    private enum Spring {
        OPERATIONAL('.'),
        DAMAGED('#'),
        UNKNOWN('?');

        private final char value;

        Spring(final char value) {
            this.value = value;
        }

        private static Spring from(final char c) {
            return Arrays.stream(Spring.values())
                .filter(spring -> spring.value == c)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Value %s is not recognized as spring".formatted(String.valueOf(c))));
        }

    }

}
