package com.rips7.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.Callable;

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

  public static String readResource(String filename) {
    try (InputStream in = Util.class.getResourceAsStream(filename)) {
      return new String(Objects.requireNonNull(in).readAllBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> TimedResult<T> time(Callable<T> runnable) {
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

  public static void printColor(String text, AnsiColor col) {
    System.out.printf("%s%s%s", col, text, AnsiColor.RESET);
  }

  public record TimedResult<T>(T res, String timeInfo) {}

}
