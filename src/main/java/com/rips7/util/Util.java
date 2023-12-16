package com.rips7.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class Util {

  public record Vector2D<T>(T x, T y) {
    public static <T> Vector2D<T> of(T x, T y) {
      return new Vector2D<>(x, y);
    }
  }

  public record Vector3D<T>(T x, T y, T z) {}

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

  public record TimedResult<T>(T res, String timeInfo) {}

}
