package com.rips7.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

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

  public static void time(Runnable runnable) {
    final long start = System.currentTimeMillis();
    runnable.run();
    final long end = System.currentTimeMillis();
    final long diff = end - start;
    if (diff < 1_000) { // less than a second
      printColor("Took %s ms.%n%n".formatted(diff), AnsiColor.GREEN);
    } else if (diff < 60_000) { // less than a minute
      final long seconds = diff / 1_000;
      final long millis = diff - (seconds * 1_000);
      printColor("Took %s sec, %s ms.%n%n".formatted(seconds, millis), AnsiColor.GREEN);
    } else {
      final long minutes = diff / 60_000;
      final long seconds = diff - (minutes * 60_000);
      final long millis = diff - seconds * 1_000;
      printColor("Took %s min, %s sec, %s ms.%n%n".formatted(minutes, seconds, millis), AnsiColor.GREEN);
    }
  }

  @SuppressWarnings("SameParameterValue")
  private static void printColor(String text, AnsiColor col) {
    System.out.printf("%s%s%s", col, text, AnsiColor.RESET);
  }

}
