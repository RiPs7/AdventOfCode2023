package com.rips7;

import com.rips7.day.Day;
import com.rips7.util.Util;
import com.rips7.util.Util.TimedResult;

import java.util.List;

import static com.rips7.util.Util.printColor;

public class Main {

  private static final List<Day<?>> DAYS = Day.getAllDays();

  public static void main(String[] args) {
    final TimedResult<String> res = Util.time(() -> {
      DAYS.forEach(Day::run);
      return "Executed all days!";
    });
    printColor("%s ".formatted(res.res()), Util.AnsiColor.GREEN);
    printColor("(%s)%n".formatted(res.timeInfo()), Util.AnsiColor.YELLOW);
  }

}
