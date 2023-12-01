package com.rips7.day;

import com.rips7.util.Util;

import static com.rips7.util.Util.TimedResult;
import static com.rips7.util.Util.printColor;
import static com.rips7.util.Util.time;

public interface Day<T> {

  T part1(String input);

  T part2(String input);

  default void run() {
    System.out.printf("----- %s -----%n", getClass().getSimpleName());

    final TimedResult<T> part1Res = time(() -> part1(loadPart1()));
    System.out.print("Part 1: ");
    printColor("%s ".formatted(part1Res.res()), Util.AnsiColor.GREEN);
    printColor("(%s)%n".formatted(part1Res.timeInfo()), Util.AnsiColor.YELLOW);

    final TimedResult<T> part2Res = time(() -> part2(loadPart2()));
    System.out.print("Part 2: ");
    printColor("%s ".formatted(part2Res.res()), Util.AnsiColor.GREEN);
    printColor("(%s)%n".formatted(part2Res.timeInfo()), Util.AnsiColor.YELLOW);

    System.out.println("----------------");
  }

  default String loadPart1() {
    return Util.readResource("/%s/part1".formatted(this.getClass().getSimpleName().toLowerCase()));
  }

  default String loadPart2() {
    return Util.readResource("/%s/part2".formatted(this.getClass().getSimpleName().toLowerCase()));
  }

}
