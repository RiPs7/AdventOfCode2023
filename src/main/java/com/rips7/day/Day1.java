package com.rips7.day;

import java.util.Arrays;
import java.util.Map;

public class Day1 implements Day<Integer> {

  private final Map<String, Integer> SPELLED_DIGITS = Map.of(
    "one", 1,
    "two", 2,
    "three", 3,
    "four", 4 ,
    "five", 5,
    "six", 6,
    "seven", 7,
    "eight", 8,
    "nine", 9
  );

  @Override
  public Integer part1(String input) {
    return Arrays.stream(input.split("\n"))
      .map(this::readNumber1)
      .reduce(Integer::sum)
      .orElse(0);
  }

  @Override
  public Integer part2(String input) {
    return Arrays.stream(input.split("\n"))
      .map(this::readNumber2)
      .reduce(Integer::sum)
      .orElse(0);
  }

  private Integer readNumber1(final String s) {
    int digit1 = -1;
    int digit2 = -1;
    for (int left = 0, right = s.length() - 1; left < s.length() && right > -1 && (digit1 == -1 || digit2 == -1); left++, right--) {
      if (digit1 == -1 && Character.isDigit(s.charAt(left))) {
        digit1 = Integer.parseInt("" + s.charAt(left));
      }
      if (digit2 == -1 && Character.isDigit(s.charAt(right))) {
        digit2 = Integer.parseInt("" + s.charAt(right));
      }
    }
    return 10 * digit1 + digit2;
  }

  private Integer readNumber2(final String s) {
    int digit1 = -1;
    int digit2 = -1;
    for (int left = 0, right = s.length() - 1; left < s.length() && right > -1 && (digit1 == -1 || digit2 == -1); left++, right--) {
      if (digit1 == -1) {
        if (Character.isDigit(s.charAt(left))) {
          digit1 = Integer.parseInt("" + s.charAt(left));
        } else {
          digit1 = readSpelledDigit(s.substring(left));
        }
      }
      if (digit2 == -1) {
        if (Character.isDigit(s.charAt(right))) {
          digit2 = Integer.parseInt("" + s.charAt(right));
        } else {
          digit2 = readSpelledDigit(s.substring(right));
        }
      }
    }
    return 10 * digit1 + digit2;
  }

  private int readSpelledDigit(final String s) {
    return SPELLED_DIGITS.entrySet().stream()
      .filter(digitEntry -> s.startsWith(digitEntry.getKey()))
      .map(Map.Entry::getValue)
      .findFirst()
      .orElse(-1);
  }

}
