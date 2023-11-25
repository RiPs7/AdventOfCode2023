package com.rips7.day;

import com.rips7.util.Util;

public interface Day {

  void part1(String input);

  void part2(String input);

  default void runPart1() {
    part1(loadPart1());
  }

  default void runPart2() {
    part2(loadPart2());
  }

  default String loadPart1() {
    return Util.readResource("/%s/part1".formatted(this.getClass().getSimpleName().toLowerCase()));
  }

  default String loadPart2() {
    return Util.readResource("/%s/part2".formatted(this.getClass().getSimpleName().toLowerCase()));
  }

}
