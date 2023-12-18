package com.rips7.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("unused")
public class Util {

  public enum AnsiColor {
    RESET("\u001B[0m"),
    BLACK("\u001B[30m"),
    RED("\u001B[31m"),
    GREEN("\u001B[32m"),
    YELLOW("\u001B[33m"),
    BLUE("\u001B[34m"),
    PURPLE("\u001B[35m"),
    CYAN("\u001B[36m"),
    ;

    private final String code;

    AnsiColor(final String code) {
      this.code = code;
    }

    @Override
    public String toString() {
      return code;
    }
  }

  public static String readResource(final String filename) {
    try (InputStream in = Util.class.getResourceAsStream(filename)) {
      return new String(Objects.requireNonNull(in).readAllBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @SafeVarargs
  public static <T> List<List<T>> zip(List<T>... lists) {
    final int zipSize = Arrays.stream(lists)
        .map(List::size)
        .min(Integer::compareTo)
        .orElse(0);
    return IntStream.range(0, zipSize)
        .mapToObj(i -> Arrays.stream(lists).map(l -> l.get(i)).toList())
        .toList();
  }

  public static <T> T lastElement(final List<T> list) {
    return list.isEmpty() ? null : list.get(list.size() - 1);
  }

  public static <T> T firstElement(final List<T> list) {
    return list.isEmpty() ? null : list.get(0);
  }

  public static <T> TimedResult<T> time(final Callable<T> runnable) {
    final long start = System.currentTimeMillis();
    final T res;
    try {
      res = runnable.call();
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
    final long end = System.currentTimeMillis();
    final long diff = end - start;
    if (diff < 1_000) { // less than a second
      return new TimedResult<>(res, "Took %s ms".formatted(diff));
    } else if (diff < 60_000) { // less than a minute
      final long seconds = diff / 1_000;
      final long millis = diff - (seconds * 1_000);
      return new TimedResult<>(res, "Took %s sec, %s ms".formatted(seconds, millis));
    } else {
      final long minutes = diff / 60_000;
      final long seconds = diff - (minutes * 60_000);
      final long millis = diff - seconds * 1_000;
      return new TimedResult<>(res, "Took %s min, %s sec, %s ms".formatted(minutes, seconds, millis));
    }
  }

  public static void printColor(final String text, final AnsiColor col) {
    System.out.printf("%s%s%s", col, text, AnsiColor.RESET);
  }

  public static void loop2D(final int rows, final int cols, final BiConsumer<Integer, Integer> cb) {
    IntStream.range(0, rows).forEach(r -> IntStream.range(0, cols).forEach(c -> cb.accept(r, c)));
  }

  public static <T> void print2DArray(final T[][] arr) {
    print2DArray(arr, T::toString);
  }

  public static <T> void print2DArray(final T[][] arr, final Function<T, String> stringifier) {
    System.out.println(Arrays.stream(arr)
        .map(row -> Arrays.stream(row)
            .map(stringifier)
            .collect(Collectors.joining()))
        .collect(Collectors.joining("\n")));
  }

  @SuppressWarnings("unchecked")
  public static <T> T[] newGenericArray(final Class<T> clazz, final int m) {
    return (T[]) Array.newInstance(clazz, m);
  }

  @SuppressWarnings("unchecked")
  public static <T> T[][] newGeneric2DArray(final Class<T> clazz, final int m, final int n) {
    return (T[][]) Array.newInstance(clazz, m, n);
  }

  public static <T> T[] slice(final Class<T> clazz, final T[] arr, final int start, final int end) {
    return Arrays.stream(arr, start, end).toArray(size -> newGenericArray(clazz, size));
  }

  public static <T> T[][] slice2D(final Class<T> clazz, final T[][] arr, final int start, final int end) {
    return Arrays.stream(arr, start, end).toArray(size -> newGeneric2DArray(clazz, size, arr[0].length));
  }

  public static <T> T[][] invert2D(final Class<T> clazz, final T[][] arr) {
    return IntStream.range(0, arr.length)
        .mapToObj(i -> arr.length - i - 1)
        .map(i -> arr[i])
        .toArray(size -> newGeneric2DArray(clazz, size, arr[0].length));
  }

  public static <T> boolean equal2D(final T[][] arr1, final T[][] arr2) {
    if (arr1.length != arr2.length || arr1[0].length != arr2[0].length) {
      return false;
    }
    for (int i = 0; i < arr1.length; i++) {
      for (int j = 0; j < arr1[0].length; j++) {
        if (!Objects.equals(arr1[i][j], arr2[i][j])) {
          return false;
        }
      }
    }
    return true;
  }

  public record TimedResult<T>(T res, String timeInfo) {}

}
