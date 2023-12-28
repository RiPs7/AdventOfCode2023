package com.rips7.day;

import com.rips7.util.Util;
import com.rips7.util.maths.Combinatorics.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day19 implements Day<Long> {

    @Override
    public Long part1(String input) {
        final String[] split = input.split("\n\n");

        final Map<String, Workflow> workflows = Arrays.stream(split[0].split("\n"))
            .map(Workflow::parse)
            .collect(Collectors.toMap(Workflow::name, Function.identity()));

        final List<Part> parts = Arrays.stream(split[1].split("\n"))
            .map(Part::parse)
            .toList();

        return parts.stream()
            .filter(part -> part.apply(workflows).equals(Result.ACCEPTED))
            .map(part -> (long) (part.x + part.m + part.a + part.s))
            .reduce(Long::sum)
            .orElse(0L);
    }

    @Override
    public Long part2(String input) {
        final String[] split = input.split("\n\n");

        final Map<String, Workflow> workflows = Arrays.stream(split[0].split("\n"))
            .map(Workflow::parse)
            .collect(Collectors.toMap(Workflow::name, Function.identity()));

        return new RangedPart(1, 4000).apply(workflows);
    }

    private record Workflow(String name, List<Rule> rules) {
        private static final Pattern WORKFLOW_PATTERN = Pattern.compile("(?<name>.+)\\{(?<rules>.+)}");
        private static Workflow parse(final String input) {
            final Matcher matcher = WORKFLOW_PATTERN.matcher(input);
            if (matcher.matches()) {
                final String name = matcher.group("name");
                final String allRules = matcher.group("rules");
                final List<Rule> rules = Arrays.stream(allRules.split(","))
                    .map(Rule::parse)
                    .toList();
                return new Workflow(name, rules);
            }
            throw new RuntimeException("Cannot parse %s as workflow".formatted(input));
        }

        private String apply(final Part part) {
            for (final Rule rule : rules) {
                final String result = rule.evaluate(part);
                if (result != null) {
                    return result;
                }
            }
            throw new RuntimeException("Cannot apply part %s to workflow %s".formatted(part, name));
        }
    }

    private record Rule(Condition condition, String result) {
        private static Rule parse(final String input) {
            final String[] ruleParts = input.split(":");
            if (ruleParts.length == 1) {
                return new Rule(null, ruleParts[0]);
            } else if (ruleParts.length == 2) {
                final Condition condition = Condition.parse(ruleParts[0]);
                return new Rule(condition, ruleParts[1]);
            }
            throw new RuntimeException("Cannot parse %s as rule".formatted(input));
        }

        private String evaluate(final Part part) {
            if (condition == null || condition.evaluate(part)) {
                return result;
            }
            return null;
        }
    }

    private record Condition(String component, Function<Part, Integer> valueGetter, Inequality inequality, long value) {
        private static final Pattern CONDITION_PATTERN = Pattern.compile("(?<component>[xmas])(?<inequality>[><])(?<value>\\d+)");
        private static Condition parse(final String input) {
            final Matcher matcher = CONDITION_PATTERN.matcher(input);
            if (matcher.matches()) {
                final String component = matcher.group("component");
                final Function<Part, Integer> valueGetter = switch(component) {
                    case "x" -> Part::x;
                    case "m" -> Part::m;
                    case "a" -> Part::a;
                    case "s" -> Part::s;
                    default -> throw new RuntimeException("Unrecognized component %s in %s".formatted(component, input));
                };
                final Inequality inequality = Inequality.from(matcher.group("inequality").charAt(0));
                final long value = Long.parseLong(matcher.group("value"));
                return new Condition(component, valueGetter, inequality, value);
            }
            throw new RuntimeException("Cannot parse %s as condition".formatted(input));
        }

        private boolean evaluate(final Part part) {
            return inequality.evaluate(valueGetter.apply(part), value);
        }
    }

    private enum Inequality {
        LT('<', (a, b) -> a < b),
        GT('>', (a, b) -> a > b);

        private final char value;
        private final BiPredicate<Long, Long> check;

        Inequality(final char value, final BiPredicate<Long, Long> check) {
            this.value = value;
            this.check = check;
        }

        private static Inequality from(final char c) {
            return Arrays.stream(values())
                .filter(e -> e.value == c)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot parse %s as inequality".formatted(c)));
        }

        private boolean evaluate(final long a, final long b) {
            return check.test(a, b);
        }
    }

    private enum Result {
        ACCEPTED('A'),
        REJECTED('R');

        private final char value;

        Result(final char value) {
            this.value = value;
        }

        private static Result from(final char c) {
            return Arrays.stream(values())
                .filter(e -> e.value == c)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot parse %s as result".formatted(c)));
        }
    }

    private record Part(int x, int m, int a, int s) {
        private static final Pattern PART_PATTERN = Pattern.compile("\\{x=(?<x>\\d+),m=(?<m>\\d+),a=(?<a>\\d+),s=(?<s>\\d+)}");
        private static Part parse(final String input) {
            final Matcher matcher = PART_PATTERN.matcher(input);
            if (matcher.matches()) {
                final int x = Integer.parseInt(matcher.group("x"));
                final int m = Integer.parseInt(matcher.group("m"));
                final int a = Integer.parseInt(matcher.group("a"));
                final int s = Integer.parseInt(matcher.group("s"));
                return new Part(x, m, a, s);
            }
            throw new RuntimeException("Cannot parse %s as input".formatted(input));
        }

        private Result apply(final Map<String, Workflow> workflows) {
            String workflowName = "in";
            Workflow workflow = workflows.get(workflowName);
            while(workflow != null) {
                workflowName = workflow.apply(this);
                workflow = workflows.get(workflowName);
            }
            return Result.from(workflowName.charAt(0));
        }
    }

    private record RangedPart(long min, long max) {
        private long apply(final Map<String, Workflow> workflows) {
            final Map<String, Pair<Long, Long>> ranges = Stream.of("x", "m", "a", "s")
                .collect(Collectors.toMap(Function.identity(), x -> Pair.of(min, max)));
            return count(ranges, workflows, "in");
        }

        private long count(final Map<String, Pair<Long, Long>> ranges, final Map<String, Workflow> workflows,
                           final String name) {
            if (name.equals("R")) {
                return 0L;
            }
            if (name.equals("A")) {
                return ranges.values().stream()
                    .map(pair -> pair.right() - pair.left() + 1)
                    .reduce(1L, (a, b) -> a * b);
            }
            long total = 0;

            // We want a copy of the ranges so that we don't mutate the original map and propagate the mutation up the
            // recursion
            final Map<String, Pair<Long, Long>> rangesCopy = new HashMap<>(ranges);
            final Workflow workflow = workflows.get(name);
            boolean isFalseEmpty = false;
            for (final Rule rule : workflow.rules) {
                if (rule.condition == null) {
                    total += count(rangesCopy, workflows, rule.result);
                    break;
                }
                final Pair<Long, Long> range = rangesCopy.get(rule.condition.component);
                final Pair<Long, Long> truePart;
                final Pair<Long, Long> falsePart;
                switch(rule.condition.inequality) {
                    case LT -> {
                        truePart = Pair.of(range.left(), rule.condition.value - 1);
                        falsePart = Pair.of(rule.condition.value, range.right());
                    }
                    case GT -> {
                        truePart = Pair.of(rule.condition.value + 1, range.right());
                        falsePart = Pair.of(range.left(), rule.condition.value);
                    }
                    default -> throw new RuntimeException("Unexpected condition inequality %s".formatted(rule.condition.inequality));
                }
                if (truePart.left() <= truePart.right()) {
                    rangesCopy.put(rule.condition.component, truePart);
                    total += count(rangesCopy, workflows, rule.result);
                }
                if (falsePart.left() <= falsePart.right()) {
                    rangesCopy.put(rule.condition.component, falsePart);
                } else {
                    isFalseEmpty = true;
                    break;
                }
            }
            if (isFalseEmpty) {
                total += count(rangesCopy, workflows, Objects.requireNonNull(Util.lastElement(workflow.rules)).result);
            }

            return total;
        }
    }

}