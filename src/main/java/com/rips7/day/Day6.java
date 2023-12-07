package com.rips7.day;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class Day6 implements Day<Integer> {

  @Override
  public Integer part1(String input) {
    return parseRaces(input).stream()
      .map(Race::countWins)
      .reduce(1, (a, b) -> a * b);
  }

  @Override
  public Integer part2(String input) {
    return parseRacesIgnoreSpaces(input).countWins();
  }

  private static List<Race> parseRaces(final String input) {
    final String[] lines = input.split("\n");
    final List<Long> times = Arrays.stream(lines[0].replace("Time:", "").trim().split("\s+"))
      .map(Long::parseLong)
      .toList();
    final List<Long> distances = Arrays.stream(lines[1].replace("Distance:", "").trim().split("\s+"))
      .map(Long::parseLong)
      .toList();
    return IntStream.range(0, times.size())
      .mapToObj(i -> new Race(times.get(i), distances.get(i)))
      .toList();
  }

  private static Race parseRacesIgnoreSpaces(final String input) {
    final String[] lines = input.split("\n");
    final long time = Long.parseLong(lines[0].replace("Time:", "").trim().replaceAll("\\s+", ""));
    final long distance = Long.parseLong(lines[1].replace("Distance:", "").trim().replaceAll("\\s+", ""));
    return new Race(time, distance);
  }

  private record Race (long time, long distance) {
    private int countWins() {
      int wins = 0;
      for (int hold = 1; hold < time; hold++) {
        final long timeToTravel = time - hold;
        final long travel = hold * timeToTravel;
        if (travel > distance) {
          wins += 1;
        }
      }
      return wins;
    }
  }
}
