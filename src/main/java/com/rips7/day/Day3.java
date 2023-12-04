package com.rips7.day;

import com.rips7.util.maths.Combinatorics.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Day3 implements Day<Integer> {

  @Override
  public Integer part1(String input) {
    return new EnginePart1(input).findPartNumbers().stream()
      .reduce(Integer::sum)
      .orElse(0);
  }

  @Override
  public Integer part2(String input) {
    return new EnginePart2(input).findGearedPartNumbers().stream()
      .reduce(Integer::sum)
      .orElse(0);
  }

  private static abstract class Engine {
    final char[][] schematic;

    private Engine(char[][] schematic) {
      this.schematic = schematic;
    }

    protected static char[][] parse(final String input) {
      return Arrays.stream(input.split("\n")).map(String::toCharArray).toArray(char[][]::new);
    }

    protected char getOrDefault(final int row, final int col) {
      try {
        return schematic[row][col];
      } catch (ArrayIndexOutOfBoundsException aioobe) {
        return '.';
      }
    }
  }

  private static final class EnginePart1 extends Engine {

    private EnginePart1(final String input) {
      super(parse(input));
    }

    private List<Integer> findPartNumbers() {
      final List<Integer> partNumbers = new ArrayList<>();
      for (int row = 0; row < schematic.length; row++) {
        int partNumber = 0;
        boolean hasAdjacentSymbols = false;
        for (int col = 0; col < schematic[row].length; col++) {
          final char c = schematic[row][col];
          if (Character.isDigit(c)) {
            partNumber = partNumber * 10 + Integer.parseInt("" + c);
            hasAdjacentSymbols |= hasAdjacentSymbol(row, col);
          } else if (partNumber != 0) {
            if (hasAdjacentSymbols) {
              partNumbers.add(partNumber);
            }
            partNumber = 0;
            hasAdjacentSymbols = false;
          }
        }
        if (partNumber != 0 && hasAdjacentSymbols) {
          partNumbers.add(partNumber);
        }
      }
      return partNumbers;
    }

    private boolean hasAdjacentSymbol(final int row, final int col) {
      for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
        for (int colOffset = -1; colOffset <= 1; colOffset++) {
          if (rowOffset == 0 && colOffset == 0) {
            continue;
          }
          final char c = getOrDefault(row + rowOffset, col + colOffset);
          if (c != '.' && !Character.isDigit(c)) {
            return true;
          }
        }
      }
      return false;
    }
  }

  private static final class EnginePart2 extends Engine {

    private EnginePart2(final String input) {
      super(parse(input));
    }

    private List<Integer> findGearedPartNumbers() {
      final Map<Pair<Integer, Integer>, List<Integer>> possibleGearedNumbers = new HashMap<>();
      for (int row = 0; row < schematic.length; row++) {
        final AtomicReference<Pair<Integer, Integer>> adjacentGear = new AtomicReference<>(null);
        final AtomicInteger partNumber = new AtomicInteger();
        for (int col = 0; col < schematic[row].length; col++) {
          final char c = schematic[row][col];
          if (Character.isDigit(c)) {
            partNumber.set(partNumber.get() * 10 + Integer.parseInt("" + c));
            getGearAdjacentSymbol(row, col).ifPresent(adjacentGear::set);
          } else if (partNumber.get() != 0) {
            if (adjacentGear.get() != null) {
              possibleGearedNumbers.merge(adjacentGear.get(), new ArrayList<>(List.of(partNumber.get())), (oldList, newList) -> {
                oldList.add(partNumber.get());
                return oldList;
              });
            }
            partNumber.set(0);
            adjacentGear.set(null);
          }
        }
        if (partNumber.get() != 0 && adjacentGear.get() != null) {
          possibleGearedNumbers.merge(adjacentGear.get(), new ArrayList<>(List.of(partNumber.get())), (oldList, newList) -> {
            oldList.add(partNumber.get());
            return oldList;
          });
        }
      }
      return possibleGearedNumbers.values().stream()
        .filter(list -> list.size() == 2)
        .map(list -> list.get(0) * list.get(1))
        .toList();
    }

    private Optional<Pair<Integer, Integer>> getGearAdjacentSymbol(final int row, final int col) {
      for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
        for (int colOffset = -1; colOffset <= 1; colOffset++) {
          if (rowOffset == 0 && colOffset == 0) {
            continue;
          }
          final char c = getOrDefault(row + rowOffset, col + colOffset);
          if (c == '*') {
            return Optional.of(Pair.of(row + rowOffset, col + colOffset));
          }
        }
      }
      return Optional.empty();
    }
  }
}
