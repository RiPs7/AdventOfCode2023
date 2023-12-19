package com.rips7.day;

import com.rips7.util.maths.Combinatorics.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static com.rips7.util.Util.isBlank;

public class Day15 implements Day<Integer> {

    @Override
    public Integer part1(String input) {
        return Arrays.stream(input.split(","))
            .map(Day15::HASH)
            .reduce(Integer::sum)
            .orElseThrow();
    }

    @Override
    public Integer part2(String input) {
        final List<Step> steps = Arrays.stream(input.split(","))
            .map(Step::parse)
            .toList();
        final List<Box> boxes = IntStream.range(0, 256).boxed()
            .map(Box::new)
            .toList();
        steps.forEach(step -> boxes.get(HASH(step.label)).apply(step));
        return boxes.stream()
            .map(Box::calculateFocusPower)
            .reduce(Integer::sum)
            .orElseThrow();
    }

    private static int HASH(final String input) {
        int hash = 0;
        for (final char c : input.toCharArray()) {
            hash += c;
            hash *= 17;
            hash %= 256;
        }
        return hash;
    }

    private record Step(String label, Operation operation, Integer focalLength) {
        private static final Pattern STEP_PATTERN = Pattern.compile("(?<label>.+)(?<operation>[-=])(?<focalLength>\\d*)");
        private static Step parse(final String input) {
            final Matcher matcher = STEP_PATTERN.matcher(input);
            if (matcher.matches()) {
                final String label = matcher.group("label");
                final Operation operation = Operation.from(matcher.group("operation").charAt(0));
                final Integer focalLength = isBlank(matcher.group("focalLength")) ? null : Integer.parseInt(matcher.group("focalLength"));
                return new Step(label, operation, focalLength);
            }
            throw new RuntimeException("Cannot parse step %s".formatted(input));
        }
    }

    private enum Operation {
        DASH('-'),
        EQUALS('='),
        ;

        private final char value;

        Operation(final char value) {
            this.value = value;
        }

        private static Operation from(final char c) {
            return Arrays.stream(values())
                .filter(r -> r.value == c)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("%s is not recognized as an operation".formatted(c)));
        }

    }

    private record Box(int id, ArrayList<Pair<String, Integer>> slotLenses) {
        private Box(int id) {
            this(id, new ArrayList<>());
        }

        private int findSlot(final String label) {
            for (int i = 0; i < slotLenses.size(); i++) {
                if (slotLenses.get(i).left().equals(label)) {
                    return i;
                }
            }
            return -1;
        }

        private void apply(final Step step) {
            final int slot = findSlot(step.label);
            if (step.operation == Operation.EQUALS) {
                if (slot == -1) {
                    slotLenses.add(Pair.of(step.label, step.focalLength));
                } else {
                    slotLenses.set(slot, Pair.of(step.label, step.focalLength));
                }
            } else {
                if (slot != -1) {
                    slotLenses.remove(slot);
                }
            }
        }

        private int calculateFocusPower() {
            return slotLenses.isEmpty()
                ? 0
                : (id + 1) * IntStream.range(0, slotLenses.size())
                .mapToObj(i -> (i + 1) * slotLenses.get(i).right())
                .reduce(Integer::sum)
                .orElseThrow();
        }
    }
}