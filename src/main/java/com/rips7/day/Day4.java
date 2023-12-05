package com.rips7.day;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Day4 implements Day<Integer> {

  @Override
  public Integer part1(String input) {
    return Arrays.stream(input.split("\n"))
      .map(ScratchCard::parse)
      .map(ScratchCard::getScore)
      .reduce(Integer::sum)
      .orElse(0);
  }

  @Override
  public Integer part2(String input) {
    final Map<Integer, Integer> numberOfCards = new HashMap<>();
    Arrays.stream(input.split("\n"))
      .map(ScratchCard::parse)
      .forEach(card -> {
        numberOfCards.merge(card.id, 1, (oldValue, newValue) -> oldValue + 1);
        final int score = card.getMatchingNumbers();
        for (int i = 0; i < score; i++) {
          numberOfCards.merge(card.id + i + 1, numberOfCards.getOrDefault(card.id, 0), (oldValue, newValue) -> oldValue + numberOfCards.getOrDefault(card.id, 1));
        }
      });
    return numberOfCards.values().stream().reduce(Integer::sum).orElse(0);
  }

  private record ScratchCard(int id, Set<Integer> winningNumbers, Set<Integer> numbers) {
    private static ScratchCard parse(final String input) {
      final int id = Integer.parseInt(
        Optional.of(input.replaceAll("Card\\s+", ""))
          .map(s -> s.substring(0, s.indexOf(":")))
          .get());
      final String[] parts = input.substring(input.indexOf(": ") + 2).split(" \\| ");
      final Set<Integer> winningNumbers = Arrays.stream(parts[0].trim().split("\\s+"))
        .map(Integer::parseInt)
        .collect(Collectors.toSet());
      final Set<Integer> numbers = Arrays.stream(parts[1].trim().split("\\s+"))
        .map(Integer::parseInt)
        .collect(Collectors.toSet());
      return new ScratchCard(id, winningNumbers, numbers);
    }

    private int getMatchingNumbers() {
      final Set<Integer> matchingNumbers = new HashSet<>(winningNumbers);
      matchingNumbers.retainAll(numbers);
      return matchingNumbers.size();
    }

    private int getScore() {
      final int matchingNumbers = getMatchingNumbers();
      return matchingNumbers == 0 ? 0 : 1 << (matchingNumbers - 1);
    }
  }
}
