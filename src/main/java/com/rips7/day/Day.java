package com.rips7.day;

import com.rips7.util.Util;

import java.util.List;

import static com.rips7.util.Util.TimedResult;
import static com.rips7.util.Util.printColor;
import static com.rips7.util.Util.time;

public interface Day<T> {

  T part1(String input);

  T part2(String input);

  default void run() {
    System.out.printf("----- %s -----%n", getClass().getSimpleName());

    final TimedResult<T> part1Res = time(() -> part1(loadInput()));
    System.out.print("Part 1: ");
    printColor("%s ".formatted(part1Res.res()), Util.AnsiColor.GREEN);
    printColor("(%s)%n".formatted(part1Res.timeInfo()), Util.AnsiColor.YELLOW);

    final TimedResult<T> part2Res = time(() -> part2(loadInput()));
    System.out.print("Part 2: ");
    printColor("%s ".formatted(part2Res.res()), Util.AnsiColor.GREEN);
    printColor("(%s)%n".formatted(part2Res.timeInfo()), Util.AnsiColor.YELLOW);

    System.out.println("----------------");
  }

  default String loadInput() {
    final String inputFilename = "/%s/input".formatted(this.getClass().getSimpleName().toLowerCase());
    try {
      return Util.readResource(inputFilename);
    } catch (final NullPointerException e) {
      throw new RuntimeException("No input file '%s'".formatted(inputFilename));
    }
  }

  static List<Day<?>> getAllDays() {
    return List.of(
        new Day1(),
        new Day2(),
        new Day3(),
        new Day4(),
        new Day5(),
        new Day6(),
        new Day7(),
        new Day8(),
        new Day9(),
        new Day10(),
        new Day11(),
        new Day12(),
        new Day13(),
        new Day14(),
        new Day15(),
        new Day16(),
        new Day17(),
        new Day18(),
        new Day19(),
        new Day20(),
        new Day21(),
        new Day22(),
        new Day23(),
        new Day24(),
        new Day25());
  }

}
