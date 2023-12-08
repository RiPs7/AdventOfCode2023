package com.rips7.day;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.rips7.util.maths.Combinatorics.frequencyMap;

public class Day7 implements Day<Integer> {

  @Override
  public Integer part1(String input) {
    final Map<Hand, Integer> handsAndBids = parseHandsAndBids(input, false);
    final Map<Hand, Integer> handsAndRanks = calculateRanks(new ArrayList<>(handsAndBids.keySet()));
    return handsAndBids.keySet().stream()
      .map(hand -> handsAndBids.get(hand) * handsAndRanks.get(hand))
      .reduce(Integer::sum)
      .orElse(0);
  }

  @Override
  public Integer part2(String input) {
    final Map<Hand, Integer> handsAndBids = parseHandsAndBids(input, true);
    final Map<Hand, Integer> handsAndRanks = calculateRanks(new ArrayList<>(handsAndBids.keySet()));
    return handsAndBids.keySet().stream()
      .map(hand -> handsAndBids.get(hand) * handsAndRanks.get(hand))
      .reduce(Integer::sum)
      .orElse(0);
  }

  private static Map<Hand, Integer> parseHandsAndBids(final String input, final boolean withWildcards) {
    return Arrays.stream(input.split("\n"))
      .map(line -> line.split(" "))
      .collect(Collectors.toMap(parts -> Hand.parse(parts[0], withWildcards), parts -> Integer.parseInt(parts[1])));
  }

  private static Map<Hand, Integer> calculateRanks(final List<Hand> hands) {
    final Map<HandType, List<Hand>> handTypesGroups = hands.stream().collect(Collectors.groupingBy(Hand::type));
    final Map<Hand, Integer> ranks = new HashMap<>();
    final AtomicInteger currentRank = new AtomicInteger(1);
    Arrays.stream(HandType.values())
      .filter(handTypesGroups::containsKey)
      .sorted(Comparator.comparing(handType -> handType.strength))
      .map(handTypesGroups::get)
      .map(Day7::calculateWins)
      .forEach(winsPerHand -> {
        final int currentRankValue = currentRank.get();
        winsPerHand.forEach((win, hand) -> ranks.put(hand, currentRankValue + win));
        currentRank.addAndGet(winsPerHand.size());
      });
    return ranks;
  }

  private static TreeMap<Integer, Hand> calculateWins(final List<Hand> hands) {
    final Map<Hand, Integer> winsPerHand = new HashMap<>();
    for (int i = 0; i < hands.size() - 1; i++) {
      final Hand hand1 = hands.get(i);
      for (int j = i + 1; j < hands.size(); j++) {
        final Hand hand2 = hands.get(j);
        if (hand1.isStronger(hand2)) {
          winsPerHand.merge(hand1, 1, Integer::sum);
        } else if (hand2.isStronger(hand1)) {
          winsPerHand.merge(hand2, 1, Integer::sum);
        }
      }
    }
    // Add any non-winning hands
    hands.stream().filter(hand -> !winsPerHand.containsKey(hand)).forEach(hand -> winsPerHand.put(hand, 0));

    return winsPerHand.entrySet().stream()
      .collect(Collectors.toMap(Entry::getValue, Entry::getKey, (v1, v2) -> v1, TreeMap::new));
  }

  private record Hand(Card[] cards, HandType type) {
    private static Hand parse(final String input, final boolean withWildcards) {
      final Card[] cards = input.chars()
        .mapToObj(c -> (char) c)
        .map(c -> Card.from(c, withWildcards))
        .toArray(Card[]::new);
      return new Hand(cards, withWildcards ? findTypeWithWildcards(cards) : findType(cards));
    }

    private static HandType findType(final Card[] cards) {
      final Map<Card, Long> cardFrequency = frequencyMap(cards);
      return switch (cardFrequency.size()) {
        case 1 -> HandType.FIVE_OF_A_KIND;
        case 2 -> cardFrequency.containsValue(1L) ? HandType.FOUR_OF_A_KIND : HandType.FULL_HOUSE;
        case 3 -> cardFrequency.containsValue(3L) ? HandType.THREE_OF_A_KIND : HandType.TWO_PAIR;
        case 4 -> HandType.ONE_PAIR;
        case 5 -> HandType.HIGH_CARD;
        default -> throw new RuntimeException(
          "Cannot identify hand number: %s".formatted(
            Arrays.stream(cards)
              .map(Card::toString)
              .collect(Collectors.joining())));
      };
    }

    private static HandType findTypeWithWildcards(final Card[] cards) {
      final HandType nonWildcardType = findType(cards);
      if (Arrays.stream(cards).noneMatch(card -> card == Card.WILDCARD)) {
        return nonWildcardType;
      }

      final Map<Card, Long> cardFrequency = frequencyMap(cards);

      if (cardFrequency.get(Card.WILDCARD) == 5 || cardFrequency.get(Card.WILDCARD) == 4) {
        return HandType.FIVE_OF_A_KIND;
      } else if (cardFrequency.get(Card.WILDCARD) == 3) {
        if (nonWildcardType == HandType.THREE_OF_A_KIND) {
          return HandType.FOUR_OF_A_KIND;
        } else if (nonWildcardType == HandType.FULL_HOUSE) {
          return HandType.FIVE_OF_A_KIND;
        }
      } else if (cardFrequency.get(Card.WILDCARD) == 2) {
        if (nonWildcardType == HandType.ONE_PAIR) {
          return HandType.THREE_OF_A_KIND;
        } else if (nonWildcardType == HandType.TWO_PAIR) {
          return HandType.FOUR_OF_A_KIND;
        } else if (nonWildcardType == HandType.FULL_HOUSE) {
          return HandType.FIVE_OF_A_KIND;
        }
      } else {
        if (nonWildcardType == HandType.HIGH_CARD) {
          return HandType.ONE_PAIR;
        } else if (nonWildcardType == HandType.ONE_PAIR) {
          return HandType.THREE_OF_A_KIND;
        } else if (nonWildcardType == HandType.TWO_PAIR) {
          return HandType.FULL_HOUSE;
        } else if (nonWildcardType == HandType.THREE_OF_A_KIND) {
          return HandType.FOUR_OF_A_KIND;
        } else if (nonWildcardType == HandType.FOUR_OF_A_KIND) {
          return HandType.FIVE_OF_A_KIND;
        }
      }
      throw new RuntimeException(
        "Cannot identify hand number: %s".formatted(
          Arrays.stream(cards)
            .map(Card::toString)
            .collect(Collectors.joining())));
    }

    private boolean isStronger(final Hand other) {
      if (this.type.strength > other.type.strength) {
        return true;
      } else if (this.type.strength < other.type.strength) {
        return false;
      }
      for (int i = 0; i < this.cards.length; i++) {
        // This maybe needs adjusted for the case of using wildcards,
        // but it works for the example, and the puzzle input...
        if (this.cards[i].value > other.cards[i].value) {
          return true;
        } else if (this.cards[i].value < other.cards[i].value) {
          return false;
        }
      }
      return false;
    }

    @Override
    public String toString() {
      return "Hand[" +
        "cards=" + Arrays.toString(cards) +
        ",type=" + type +
        ']';
    }
  }

  private enum HandType {
    FIVE_OF_A_KIND  (7),
    FOUR_OF_A_KIND  (6),
    FULL_HOUSE      (5),
    THREE_OF_A_KIND (4),
    TWO_PAIR        (3),
    ONE_PAIR        (2),
    HIGH_CARD       (1);

    private final int strength;

    HandType(final int strength) {
      this.strength = strength;
    }
  }

  private enum Card {
    WILDCARD (1, 'J'),
    TWO      (2, '2'),
    THREE    (3, '3'),
    FOUR     (4, '4'),
    FIVE     (5, '5'),
    SIX      (6, '6'),
    SEVEN    (7, '7'),
    EIGHT    (8, '8'),
    NINE     (9, '9'),
    TEN      (10, 'T'),
    JACK     (11, 'J'),
    QUEEN    (12, 'Q'),
    KING     (13, 'K'),
    ACE      (14, 'A');

    private final int value;
    private final char symbol;

    Card(final int value, final char symbol) {
      this.value = value;
      this.symbol = symbol;
    }

    private static Card from(final char c, final boolean withWildcards) {
      if (c == 'J') {
        return withWildcards ? WILDCARD : JACK;
      }
      return Arrays.stream(values())
        .filter(card -> card.symbol == c)
        .findFirst()
        .orElseThrow();
    }

    @Override
    public String toString() {
      return String.valueOf(symbol);
    }
  }


}
