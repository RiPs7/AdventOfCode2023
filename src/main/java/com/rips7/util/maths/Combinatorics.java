package com.rips7.util.maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("unused")
public class Combinatorics {

  public record Pair<L, R>(L left, R right) {
    public static <L, R> Pair<L,R> of(final L left, final R right) {
      return new Pair<>(left, right);
    }

    @Override
    public String toString() {
      return "[%s,%s]".formatted(left, right);
    }
  }

  public record Triplet<F, S, T>(F first, S second, T third) {
    public static <F, S, T> Triplet<F, S, T> of(final F first, final S second, final T third) {
      return new Triplet<>(first, second, third);
    }

    @Override
    public String toString() {
      return "[%s,%s,%s]".formatted(first, second, third);
    }
  }

  public static <T extends Comparable<T>> List<T[]> permutations(final T[] input) {
    Arrays.sort(input, Comparable::compareTo);
    final List<T[]> result = new ArrayList<>();
    result.add(input);
    T[] nextPermutation = getNextPermutation(input);
    while (nextPermutation != null) {
      result.add(nextPermutation);
      nextPermutation = getNextPermutation(nextPermutation);
    }
    return result;
  }

  public static <T> Map<T, Long> frequencyMap(final T[] input) {
    return Arrays.stream(input)
      .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
  }

  public static <T extends Comparable<T>> List<Pair<T, T>> consecutivePairs(final List<T> input) {
    return IntStream.range(0, input.size() / 2)
        .mapToObj(i -> Pair.of(input.get(2 * i), input.get(2 * i + 1)))
        .toList();
  }

  public static <T extends Comparable<T>> List<Pair<T, T>> consecutivePairs(final T[] input) {
    return IntStream.range(0, input.length / 2)
        .mapToObj(i -> Pair.of(input[2 * i], input[2 * i + 1]))
        .toList();
  }

  public static <T extends Comparable<T>> List<Pair<T, T>> orderedPairs(final List<T> input) {
    final List<T> sortedInput = input.stream().sorted().toList();
    return input.stream()
        .flatMap(item1 -> input.stream()
        .map(item2 -> Pair.of(item1, item2)))
        .toList();
  }

  public static <T extends Comparable<T>> List<Pair<T, T>> orderedPairs(final T[] input) {
    Arrays.sort(input, Comparable::compareTo);
    return Arrays.stream(input)
      .flatMap(item1 -> Arrays.stream(input)
        .map(item2 -> new Pair<>(item1, item2)))
      .toList();
  }

  public static <T extends Comparable<T>> List<Pair<T, T>> unorderedPairs(final List<T> input, final boolean distinct) {
    final List<T> sortedInput = input.stream().sorted().toList();
    return IntStream.range(0, distinct ? input.size() - 1 : input.size())
        .mapToObj(i -> IntStream.range(distinct ? i + 1 : i, input.size())
            .mapToObj(j -> new Pair<>(input.get(i), input.get(j)))
            .toList())
        .flatMap(List::stream)
        .toList();
  }

  public static <T extends Comparable<T>> List<Pair<T, T>> unorderedPairs(final T[] input, final boolean distinct) {
    Arrays.sort(input, Comparable::compareTo);
    return IntStream.range(0, distinct ? input.length - 1 : input.length)
      .mapToObj(i -> IntStream.range(distinct ? i + 1 : i, input.length)
        .mapToObj(j -> new Pair<>(input[i], input[j]))
        .toList())
      .flatMap(List::stream)
      .toList();
  }

  private static <T extends Comparable<T>> T[] getNextPermutation(final T[] input) {
    // Step 1.
    int largestI = -1;
    for (int i = input.length - 2; i >= 0; i--){
      if (input[i].compareTo(input[i + 1]) < 0) {
        largestI = i;
        break;
      }
    }
    if (largestI == -1) {
      return null;
    }

    // Step 2.
    int largestJ = input.length - 1;
    for (int j = input.length - 1; j >= largestI; j--) {
      if (input[largestI].compareTo(input[j]) < 0) {
        largestJ = j;
        break;
      }
    }

    // Step 3.
    final T[] arrayCopy = input.clone();
    swap(arrayCopy, largestI, largestJ);

    // Step 4.
    for (int i = largestI + 1; i < (largestI + arrayCopy.length) / 2 + 1; i++) {
      swap(arrayCopy, i, largestI + arrayCopy.length - i);
    }

    return arrayCopy;
  }

  private static <T> void swap(T[] arr, int i, int j) {
    final T temp = arr[i];
    arr[i] = arr[j];
    arr[j] = temp;
  }

}
