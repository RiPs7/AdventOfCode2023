package com.rips7.day;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.rips7.util.Util.firstElement;
import static com.rips7.util.Util.lastElement;

public class Day9 implements Day<Integer> {

  @Override
  public Integer part1(String input) {
    final List<Record> records = Arrays.stream(input.split("\n"))
      .map(Record::parseAndProcess)
      .toList();
    return records.stream()
      .map(Record::extrapolate)
      .reduce(Integer::sum)
      .orElse(0);
  }

  @Override
  public Integer part2(String input) {
    final List<Record> records = Arrays.stream(input.split("\n"))
      .map(Record::parseAndProcess)
      .toList();
    return records.stream()
      .map(Record::extrapolateBackwards)
      .reduce(Integer::sum)
      .orElse(0);
  }

  private record Record(List<ArrayList<Integer>> history) {
    private static Record parseAndProcess(final String input) {
      final ArrayList<Integer> first = Arrays.stream(input.split(" "))
        .map(Integer::parseInt)
        .collect(Collectors.toCollection(ArrayList::new));
      return new Record(process(first));
    }

    private static List<ArrayList<Integer>> process(final ArrayList<Integer> first) {
      final List<ArrayList<Integer>> history = new ArrayList<>();
      history.add(first);
      ArrayList<Integer> iterator = first;
      while(iterator.stream().anyMatch(entry -> entry != 0)) {
        final ArrayList<Integer> current = iterator;
        final ArrayList<Integer> next = IntStream.range(1, current.size())
          .mapToObj(i -> current.get(i) - current.get(i - 1))
          .collect(Collectors.toCollection(ArrayList::new));
        history.add(next);
        iterator = next;
      }
      return history;
    }

    private int extrapolate() {
      for (int i = history.size() - 1; i >= 1; i--) {
        final ArrayList<Integer> current = history.get(i);
        final ArrayList<Integer> previous = history.get(i - 1);
        final int currentLastValue = Objects.requireNonNull(lastElement(current));
        final int previousLastValue = Objects.requireNonNull(lastElement(previous));
        previous.add(currentLastValue + previousLastValue);
      }
      return Objects.requireNonNull(lastElement(history.get(0)));
    }

    private int extrapolateBackwards() {
      for (int i = history.size() - 1; i >= 1; i--) {
        final ArrayList<Integer> current = history.get(i);
        final ArrayList<Integer> previous = history.get(i - 1);
        final int currentFirstValue = Objects.requireNonNull(firstElement(current));
        final int previousFirstValue = Objects.requireNonNull(firstElement(previous));
        previous.add(0,  previousFirstValue - currentFirstValue);
      }
      return Objects.requireNonNull(firstElement(history.get(0)));
    }
  }

}
