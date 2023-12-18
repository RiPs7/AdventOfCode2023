package com.rips7.day;

import com.rips7.util.Util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Day14 implements Day<Integer> {

    @Override
    public Integer part1(String input) {
        final Platform platform = parsePlatform(input);
        platform.tiltUp();
        return platform.findLoad();
    }

    @Override
    public Integer part2(String input) {
        final Platform platform = parsePlatform(input);
        final Map<State, Integer> statesSteps = new HashMap<>();
        int steps = 0;
        statesSteps.put(new State(platform.rocks.clone()), steps++);
        int repetition;
        while(true) {
            platform.spin();
            final State newState = new State(platform.rocks.clone());
            if (statesSteps.containsKey(newState)) {
                repetition = steps - statesSteps.get(newState);
                break;
            }
            statesSteps.put(newState, steps++);
        }

        final int limit = 1_000_000_000;
        while (steps + repetition <= limit) {
            steps += repetition;
        }
        while(steps++ < limit) {
            platform.spin();
        }
        return platform.findLoad();
    }

    private static Platform parsePlatform(final String input) {
        final Rock[][] rocks = Arrays.stream(input.split("\n"))
            .map(line -> line.chars()
                .mapToObj(c -> (char) c)
                .map(c -> c == '.' ? null : Rock.from(c))
                .toArray(Rock[]::new))
            .toArray(Rock[][]::new);
        return new Platform(rocks);
    }

    private record Platform(Rock[][] rocks) {
        private void spin() {
            tiltUp();
            tiltLeft();
            tiltDown();
            tiltRight();
        }

        private void tiltUp() {
            for (int r = 0; r < rocks.length; r++) {
                for (int c = 0; c < rocks[r].length; c++) {
                    if (rocks[r][c] != Rock.ROUND) {
                        continue;
                    }
                    for (int current = r; current > 0 && rocks[current - 1][c] == null; current--) {
                        rocks[current - 1][c] = rocks[current][c];
                        rocks[current][c] = null;
                    }
                }
            }
        }

        private void tiltRight() {
            for (int c = rocks[0].length - 1; c >= 0; c--) {
                for (int r = 0; r < rocks.length; r++) {
                    if (rocks[r][c] != Rock.ROUND) {
                        continue;
                    }
                    for (int current = c; current < rocks[0].length - 1 && rocks[r][current + 1] == null; current++) {
                        rocks[r][current + 1] = rocks[r][current];
                        rocks[r][current] = null;
                    }
                }
            }
        }

        private void tiltLeft() {
            for (int c = 0; c < rocks[0].length; c++) {
                for (int r = 0; r < rocks.length; r++) {
                    if (rocks[r][c] != Rock.ROUND) {
                        continue;
                    }
                    for (int current = c; current > 0 && rocks[r][current - 1] == null; current--) {
                        rocks[r][current - 1] = rocks[r][current];
                        rocks[r][current] = null;
                    }
                }
            }
        }

        private void tiltDown() {
            for (int r = rocks.length - 1; r >= 0; r--) {
                for (int c = 0; c < rocks[r].length; c++) {
                    if (rocks[r][c] != Rock.ROUND) {
                        continue;
                    }
                    for (int current = r; current < rocks.length - 1 && rocks[current + 1][c] == null; current++) {
                        rocks[current + 1][c] = rocks[current][c];
                        rocks[current][c] = null;
                    }
                }
            }
        }

        private int findLoad() {
            final AtomicInteger load = new AtomicInteger();
            Util.loop2D(rocks.length, rocks[0].length, (i, j) -> load.addAndGet(rocks[i][j] == Rock.ROUND ? rocks.length - i : 0));
            return load.get();
        }
    }

    private enum Rock {
        ROUND('O'),
        SQUARE('#'),
        ;

        private final char value;

        Rock(final char value) {
            this.value = value;
        }

        private static Rock from(final char c) {
            return Arrays.stream(values())
                .filter(r -> r.value == c)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("%s is not recognized as a rock".formatted(c)));
        }

    }

    private record State(Rock[][] rocks) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            State state = (State) o;
            return Arrays.deepEquals(rocks, state.rocks);
        }

        @Override
        public int hashCode() {
            return Arrays.deepHashCode(rocks);
        }
    }
}
