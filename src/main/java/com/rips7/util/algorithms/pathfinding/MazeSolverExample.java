package com.rips7.util.algorithms.pathfinding;

import com.rips7.util.Util;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MazeSolverExample {

  private static final String MAZE_INPUT =
    """
    101100011110111111101111101011
    100111110011000100100100101110
    110000000001110100100101101010
    010111011011010110101101001011
    110001010010010010111001101000
    101111011111010010001110101110
    101000000000011011100000100011
    110001111111101000111110111001
    100111000000001111000010001101
    100000000111110000011110100101
    010101110000000011000100110101
    110100001101011110001100100111
    100100111001110000111011100010
    110101100000000011100000101110
    010101001111110110110111101001
    111101000100010000010001001001
    101001110111011111010111011001
    001110010001000001110010010011
    111010001101101100001111111110
    100010000100011001001000000011
    111001110111000101111011110001
    101001010001010110000110010111
    101011011101010011011100010100
    101110110001010001110110110101
    100000101111010000000010000111
    101011100010011110111010110001
    111010110110010011101000011101
    010010000101111100000111010101
    111011011101000001110001000101
    101001110111011111011111111101
    """;

  private static final Spot[][] MAZE = parseMaze();
  private static final Spot START = new Spot(0, 1, true);
  private static final Spot END = new Spot(29, 28, true);
  
  private static final int[][] NEIGHBOR_OFFSETS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

  public static void main(String[] args) {
    Util.time(MazeSolverExample::solveBFS);
    Util.time(MazeSolverExample::solveDFS);
    Util.time(MazeSolverExample::solveAStar);
  }

  private static void solveBFS() {
    final List<Spot> res = new BFS<Spot>().run(
      START,
      END,
      n -> Arrays.stream(NEIGHBOR_OFFSETS)
        .map(offset -> {
          try {
            final Spot neighbor = MAZE[n.i + offset[0]][n.j + offset[1]];
            return neighbor.isFree ? neighbor : null;
          } catch (Exception e) {
            return null;
          }
        })
        .filter(Objects::nonNull)
        .toList());
    printSolution(res);
  }

  private static void solveDFS() {
    final List<Spot> res = new DFS<Spot>().run(
      START,
      END,
      n -> Arrays.stream(NEIGHBOR_OFFSETS)
        .map(offset -> {
          try {
            final Spot neighbor = MAZE[n.i + offset[0]][n.j + offset[1]];
            return neighbor.isFree ? neighbor : null;
          } catch (Exception e) {
            return null;
          }
        })
        .filter(Objects::nonNull)
        .toList());
    printSolution(res);
  }

  private static void solveAStar() {
    final List<Spot> res = new AStar<Spot>().run(
      START,
      END,
      n -> Arrays.stream(NEIGHBOR_OFFSETS)
        .map(offset -> {
          try {
            final Spot neighbor = MAZE[n.i + offset[0]][n.j + offset[1]];
            return neighbor.isFree ? neighbor : null;
          } catch (Exception e) {
            return null;
          }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(Function.identity(), e -> 1.0f)),
      END::dist);
    printSolution(res);
  }

  private static Spot[][] parseMaze() {
    final String[] lines = MAZE_INPUT.trim().split("\n");
    return IntStream.range(0, lines.length)
      .mapToObj(i -> IntStream.range(0, lines[i].length())
        .mapToObj(j -> new Spot(i, j, lines[i].charAt(j) == '0'))
        .toArray(Spot[]::new))
      .toArray(Spot[][]::new);
  }

  private static void printSolution(List<Spot> res) {
    final StringBuilder sb = new StringBuilder();
    for (int r = 0; r < MAZE.length; r++) {
      for (int c = 0; c < MAZE[r].length; c++) {
        final int row = r;
        final int col = c;
        final boolean isInPath = res.stream().anyMatch(spot -> spot.i == row && spot.j == col);
        sb.append(isInPath ? "%s*%s".formatted(Util.AnsiColor.YELLOW, Util.AnsiColor.RESET) : (MAZE[r][c].isFree ? "0" : "1"));
      }
      sb.append("\n");
    }
    System.out.print(sb);
  }

  private record Spot(int i, int j, boolean isFree) {
    private float dist(Spot other) {
      return Math.abs(other.i - this.i) + Math.abs(other.j - this.j);
    }
  }

}
