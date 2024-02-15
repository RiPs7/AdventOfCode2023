package com.rips7.util;

import com.rips7.util.maths.Maths.Vector2D;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

  public enum Offset {
    UP(Vector2D.of(-1, 0)),
    RIGHT(Vector2D.of(0, 1)),
    DOWN(Vector2D.of(1, 0)),
    LEFT(Vector2D.of(0, -1));

    private final Vector2D<Integer> value;

    Offset(final Vector2D<Integer> value) {
      this.value = value;
    }

    public static List<Offset> getOffsets() {
      return Arrays.stream(values()).toList();
    }
  }

  public record Position(Vector2D<Integer> value) {
    public static Position of(final int i, final int j) {
      return new Position(Vector2D.of(i, j));
    }

    public Position apply(final Offset offset) {
      return new Position(Vector2D.add(this.value, offset.value, Integer::sum));
    }

    public int x() {
      return value.x();
    }

    public int y() {
      return value.y();
    }
  }

  public static String readResource(final String filename) {
    try (InputStream in = Util.class.getResourceAsStream(filename)) {
      return new String(Objects.requireNonNull(in).readAllBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Stream<String> lines(final String input) {
    return Arrays.stream(input.split("\n"));
  }

  public static boolean isBlank(final String input) {
    return input == null || input.length() == 0;
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

  public static <T> T randomElement(final List<T> list) {
    return list.get((int) Math.floor(Math.random() * list.size()));
  }

  public static <T> void enumerate(final List<T> list, final BiConsumer<Integer, T> callback) {
    IntStream.range(0, list.size()).forEach(i -> callback.accept(i, list.get(i)));
  }

  public static <T> boolean isWithinGrid(final Position pos, final T[][] grid) {
    return isWithinGrid(pos.x(), pos.y(), grid.length, grid[0].length);
  }

  public static boolean isWithinGrid(final Position pos, final int rows, final int cols) {
    return isWithinGrid(pos.x(), pos.y(), rows, cols);
  }

  public static <T> boolean isWithinGrid(final Vector2D<Integer> pos, final T[][] grid) {
    return isWithinGrid(pos.x(), pos.y(), grid.length, grid[0].length);
  }

  public static boolean isWithinGrid(final Vector2D<Integer> pos, final int rows, final int cols) {
    return isWithinGrid(pos.x(), pos.y(), rows, cols);
  }

  public static <T> boolean isWithinGrid(final int row, final int col, final T[][] grid) {
    return isWithinGrid(row, col, grid.length, grid[0].length);
  }

  public static boolean isWithinGrid(final int row, final int col, final int rows, final int cols) {
    return 0 <= row && row < rows && 0 <= col && col < cols;
  }

  public static long findLoopArea(final List<Vector2D<Integer>> loop) {
    // Shoelace formula for area inside the loop
    long innerAreaSum = 0;
    for (int i = 0; i < loop.size() - 1; i++) {
      final Vector2D<Integer> currentPoint = loop.get(i);
      final Vector2D<Integer> nextPoint = loop.get((i + 1));
      innerAreaSum += ((long) currentPoint.x() * nextPoint.y()) - ((long) nextPoint.x() * currentPoint.y());
    }
    final long innerArea = Math.abs(innerAreaSum) / 2;

    // In a grid, the shoelace formula calculates the area from the midpoint of the cells around the perimeter.
    // We need to add the number of cells along the perimeter halved
    long loopPerimeter = 0;
    for (int i = 0; i < loop.size() - 1; i++) {
      final Vector2D<Integer> currentPoint = loop.get(i);
      final Vector2D<Integer> nextPoint = loop.get((i + 1));
      if (currentPoint.x().equals(nextPoint.x())) {
        loopPerimeter += Math.abs(currentPoint.y() - nextPoint.y());
      } else if (currentPoint.y().equals(nextPoint.y())) {
        loopPerimeter += Math.abs(currentPoint.x() - nextPoint.x());
      }
    }

    return innerArea + loopPerimeter / 2 - 1;
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
      long millis = diff;
      final long seconds = millis / 1_000;
      millis -= seconds * 1_000;
      return new TimedResult<>(res, "Took %s sec, %s ms".formatted(seconds, millis));
    } else {
      long millis = diff;
      final long minutes = millis / 60_000;
      millis -= minutes * 60_000;
      final long seconds = millis / 1_000;
      millis -= seconds * 1_000;
      return new TimedResult<>(res, "Took %s min, %s sec, %s ms".formatted(minutes, seconds, millis));
    }
  }

  public static void printColor(final String text, final AnsiColor col) {
    System.out.printf("%s%s%s", col, text, AnsiColor.RESET);
  }

  public static void loop2D(final int rows, final int cols, final BiConsumer<Integer, Integer> cb) {
    IntStream.range(0, rows).forEach(r -> IntStream.range(0, cols).forEach(c -> cb.accept(r, c)));
  }

  public static <T> void loop2D(final T[][] arr, final Consumer<T> cb) {
    IntStream.range(0, arr.length).forEach(r -> IntStream.range(0, arr[r].length).forEach(c -> cb.accept(arr[r][c])));
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
