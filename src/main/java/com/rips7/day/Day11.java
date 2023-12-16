package com.rips7.day;

import com.rips7.util.maths.Combinatorics;
import com.rips7.util.maths.Combinatorics.Pair;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day11 implements Day<Long> {

  @Override
  public Long part1(String input) {
    final Space space = Space.parse(input);
    final List<Pair<Galaxy, Galaxy>> galaxyPairs = Combinatorics.unorderedPairs(space.getGalaxiesOnly(), true);
    final Map<Pair<Galaxy, Galaxy>, Long> distances = space.findShortestDistances(galaxyPairs, 2L);
    return distances.values().stream().reduce(Long::sum).orElseThrow();
  }

  @Override
  public Long part2(String input) {
      final Space space = Space.parse(input);
      final List<Pair<Galaxy, Galaxy>> galaxyPairs = Combinatorics.unorderedPairs(space.getGalaxiesOnly(), true);
      final Map<Pair<Galaxy, Galaxy>, Long> distances = new HashMap<>(space.findShortestDistances(galaxyPairs, 1_000_000L));
      return distances.values().stream().reduce(Long::sum).orElseThrow();
  }

  private record Space(Galaxy[][] galaxies) {
    private static Space parse(final String input) {
      final String[] lines = input.split("\n");
      final AtomicInteger idCounter = new AtomicInteger();
      final Galaxy[][] galaxies = Arrays.stream(lines).map(line -> {
            final String[] chars = line.chars()
                .mapToObj(c -> String.valueOf((char) c))
                .toArray(String[]::new);
            return Arrays.stream(chars).map(aChar -> aChar.equals("#") ? new Galaxy(idCounter.incrementAndGet()) : null)
                .toArray(Galaxy[]::new);
          })
          .toArray(Galaxy[][]::new);
      return new Space(galaxies);
    }

      private Map<Pair<Galaxy, Galaxy>, Long> findShortestDistances(final List<Pair<Galaxy, Galaxy>> galaxyPairs, long times) {
          return galaxyPairs.stream()
              .collect(Collectors.toMap(
                  Function.identity(),
                  pair -> this.findShortestDistance(pair, times)
              ));
      }

    private Long findShortestDistance(final Pair<Galaxy, Galaxy> galaxyPair, final long times) {
        final List<Integer> rowsToExpand = IntStream.range(0, galaxies().length)
            .filter(r -> Arrays.stream(galaxies[r]).allMatch(Objects::isNull))
            .boxed()
            .toList();
        final List<Integer> colsToExpand = IntStream.range(0, galaxies[0].length)
            .filter(c -> Arrays.stream(galaxies).map(row -> row[c]).allMatch(Objects::isNull))
            .boxed()
            .toList();

        final long minRow = Math.min(galaxyPair.left().row, galaxyPair.right().row);
        final long maxRow = Math.max(galaxyPair.left().row, galaxyPair.right().row);
        final long minCol = Math.min(galaxyPair.left().col, galaxyPair.right().col);
        final long maxCol = Math.max(galaxyPair.left().col, galaxyPair.right().col);

        final long numberOfInBetweenRows = rowsToExpand.stream().filter(row -> minRow < row && row < maxRow).count();
        final long numberOfInBetweenCols = colsToExpand.stream().filter(col -> minCol < col && col < maxCol).count();

        return galaxyPair.left().dist(galaxyPair.right())
            + numberOfInBetweenRows * (times - 1)
            + numberOfInBetweenCols * (times - 1);
    }

    private List<Galaxy> getGalaxiesOnly() {
      return IntStream.range(0, galaxies.length).boxed()
          .flatMap(row -> IntStream.range(0, galaxies[row].length)
              .mapToObj(col -> Optional.ofNullable(galaxies[row][col]).map(galaxy -> new Galaxy(galaxy.id, row, col))))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .toList();
    }
  }

  private record Galaxy(int id, long row, long col) implements Comparable<Galaxy> {
    public Galaxy(final int id) {
      this(id, -1, -1);
    }

    @Override
    public int compareTo(Galaxy other) {
      return Comparator.comparing(Galaxy::id).compare(this, other);
    }

    public long dist(final Galaxy other) {
        return Math.abs(row - other.row) + Math.abs(col - other.col);
    }
  }

}
