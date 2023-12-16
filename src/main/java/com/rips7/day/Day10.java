package com.rips7.day;

import com.rips7.util.algorithms.pathfinding.DFS;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day10 implements Day<Integer> {

  private static final Map<Pipe, Map<Direction, List<Pipe>>> CONNECTIONS = Map.of(
      Pipe.VERTICAL,
      Map.of(
          Direction.DOWN, List.of(Pipe.VERTICAL, Pipe.NE_BEND, Pipe.NW_BEND),
          Direction.UP, List.of(Pipe.VERTICAL, Pipe.SE_BEND, Pipe.SW_BEND)),
      Pipe.HORIZONTAL,
      Map.of(
          Direction.LEFT, List.of(Pipe.HORIZONTAL, Pipe.NE_BEND, Pipe.SE_BEND),
          Direction.RIGHT, List.of(Pipe.HORIZONTAL, Pipe.NW_BEND, Pipe.SW_BEND)),
      Pipe.NE_BEND,
      Map.of(
          Direction.UP, List.of(Pipe.VERTICAL, Pipe.SE_BEND, Pipe.SW_BEND),
          Direction.RIGHT, List.of(Pipe.HORIZONTAL, Pipe.SW_BEND, Pipe.NW_BEND)),
      Pipe.NW_BEND,
      Map.of(
          Direction.UP, List.of(Pipe.VERTICAL, Pipe.SE_BEND, Pipe.SW_BEND),
          Direction.LEFT, List.of(Pipe.HORIZONTAL, Pipe.SE_BEND, Pipe.NE_BEND)),
      Pipe.SE_BEND,
      Map.of(
          Direction.DOWN, List.of(Pipe.VERTICAL, Pipe.NE_BEND, Pipe.NW_BEND),
          Direction.RIGHT, List.of(Pipe.HORIZONTAL, Pipe.SW_BEND, Pipe.NW_BEND)),
      Pipe.SW_BEND,
      Map.of(
          Direction.DOWN, List.of(Pipe.VERTICAL, Pipe.NE_BEND, Pipe.NW_BEND),
          Direction.LEFT, List.of(Pipe.HORIZONTAL, Pipe.SE_BEND, Pipe.NE_BEND)));

  @Override
  public Integer part1(String input) {
    final Cell[][] cells = parseCells(input);

    final Cell start = Arrays.stream(cells)
        .flatMap(Arrays::stream)
        .filter(cell -> cell.isStart)
        .findFirst()
        .orElseThrow();

    final List<Cell> loop = new DFS<Cell>().run(start, start, cell -> getNeighbors(cell, cells), true);

    return loop.size() / 2;
  }

  @Override
  public Integer part2(String input) {
    final Cell[][] cells = parseCells(input);

    final Cell start = Arrays.stream(cells)
        .flatMap(Arrays::stream)
        .filter(cell -> cell.isStart)
        .findFirst()
        .orElseThrow();

    final List<Cell> loop = new DFS<Cell>().run(
        start,
        start,
        cell -> getNeighbors(cell, cells),
        true);

    final Cell startCell = new Cell(
        new Position(start.pos.row, start.pos.col),
        identifyStart(loop, start.pos),
        false,
        true);

    cells[start.pos.row][start.pos.col] = startCell;

    final Set<Cell> loopCells = Stream.concat(
            loop.stream().filter(cell -> !cell.isStart),
            Stream.of(startCell))
        .collect(Collectors.toSet());

    final Set<Cell> withinLoop = findCellsWithinLoop(cells, loopCells);

    return withinLoop.size();
  }

  private static Cell[][] parseCells(final String input) {
    final Character[][] grid = Arrays.stream(input.split("\n"))
        .map(line -> line.chars()
            .mapToObj(c -> (char) c)
            .toArray(Character[]::new)
        ).toArray(Character[][]::new);

    return IntStream.range(0, grid.length)
        .mapToObj(row ->
            IntStream.range(0, grid[row].length)
                .mapToObj(col -> switch (grid[row][col]) {
                  case 'S':
                    yield new Cell(new Position(row, col), null, false, true);
                  case '.':
                    yield new Cell(new Position(row, col), null, true, false);
                  default:
                    yield new Cell(new Position(row, col), Pipe.from(grid[row][col]), false, false);
                }).
                toArray(Cell[]::new))
        .toArray(Cell[][]::new);
  }

  private static List<Cell> getNeighbors(final Cell cell, final Cell[][] cells) {
    final List<Direction> directions = List.of(Direction.LEFT, Direction.UP, Direction.RIGHT, Direction.DOWN);
    return directions.stream().map(dir -> {
          try {
            final Cell neighbor = cells[cell.pos.row + dir.offsets[0]][cell.pos.col + dir.offsets[1]];
            if (cell.isConnected(neighbor, dir)) {
              return neighbor;
            }
          } catch (ArrayIndexOutOfBoundsException e) {
            // ignore
          }
          return null;
        })
        .filter(Objects::nonNull)
        .toList();
  }

  private Pipe identifyStart(final List<Cell> loop, final Position start) {
    final Position first = loop.get(1).pos;
    final Position last = loop.get(loop.size() - 2).pos;
    if (first.row == last.row) {
      return Pipe.HORIZONTAL;
    } else if (first.col == last.col) {
      return Pipe.VERTICAL;
    } else if (first.row == last.row + 1 && first.col == last.col + 1) {
      return start.col == first.col ? Pipe.SW_BEND : Pipe.NE_BEND;
    } else if (first.row == last.row + 1 && first.col == last.col - 1) {
      return start.col == first.col ? Pipe.SE_BEND : Pipe.NW_BEND;
    } else if (first.row == last.row - 1 && first.col == last.col + 1) {
      return start.col == first.col ? Pipe.NW_BEND : Pipe.SE_BEND;
    } else if (first.row == last.row - 1 && first.col == last.col - 1) {
      return start.col == first.col ? Pipe.NE_BEND : Pipe.SW_BEND;
    } else {
      throw new RuntimeException("Cannot identify start");
    }
  }

  private Set<Cell> findCellsWithinLoop(final Cell[][] cells, final Set<Cell> loopCells) {
    // For each cell, cast a ray upwards, and count intersections with the loop
    final Set<Cell> withinLoop = new HashSet<>();
    for (final Cell[] row : cells) {
      for (final Cell cell : row) {
        if (loopCells.contains(cell)) {
          continue;
        }

        final AtomicInteger intersections = new AtomicInteger();
        final AtomicReference<Pipe> prevCorner = new AtomicReference<>(null);
        for (int i = cell.pos.row - 1; i >= 0; i--) {
          final Cell other = cells[i][cell.pos.col];
          if (!loopCells.contains(other)) {
            continue;
          }
          switch(other.pipe) {
            case HORIZONTAL -> intersections.incrementAndGet();
            case SE_BEND -> {
              if (prevCorner.get() == null) {
                prevCorner.set(Pipe.SE_BEND);
              } else if (prevCorner.get() == Pipe.NE_BEND) {
                prevCorner.set(null);
              } else if (prevCorner.get() == Pipe.NW_BEND) {
                intersections.incrementAndGet();
                prevCorner.set(null);
              }
            }
            case SW_BEND -> {
              if (prevCorner.get() == null) {
                prevCorner.set(Pipe.SW_BEND);
              } else if (prevCorner.get() == Pipe.NW_BEND) {
                prevCorner.set(null);
              } else if (prevCorner.get() == Pipe.NE_BEND) {
                intersections.incrementAndGet();
                prevCorner.set(null);
              }
            }
            case NE_BEND -> {
              if (prevCorner.get() == null) {
                prevCorner.set(Pipe.NE_BEND);
              }
            }
            case NW_BEND -> {
              if (prevCorner.get() == null) {
                prevCorner.set(Pipe.NW_BEND);
              }
            }
          }
        }

        // If within the loop, there is an odd number of intersections
        if (intersections.get() % 2 == 1) {
          withinLoop.add(cell);
        }
      }
    }
    return withinLoop;
  }

  private record Cell(Position pos, Pipe pipe, boolean isGround, boolean isStart) {
    private boolean isConnected(final Cell other, final Direction dir) {
      if (this.isStart || other.isStart) {
        return true;
      }
      if (this.isGround || this.pipe == null || other.pipe == null) {
        return false;
      }
      return CONNECTIONS.get(this.pipe).getOrDefault(dir, List.of()).contains(other.pipe);
    }

    @Override
    public String toString() {
      return "Cell=([%s,%s],%s)".formatted(pos.row, pos.col, Optional.ofNullable(pipe).map(pipe -> pipe.value).orElse(isStart ? 'S' : '.'));
    }
  }

  private record Position(int row, int col) {}

  private enum Pipe {
    VERTICAL('|'),
    HORIZONTAL('-'),
    NE_BEND('L'),
    NW_BEND('J'),
    SE_BEND('F'),
    SW_BEND('7');

    private final char value;

    Pipe(char value) {
      this.value = value;
    }

    private static Pipe from(final char c) {
      return Arrays.stream(Pipe.values())
          .filter(pipe -> pipe.value == c)
          .findFirst()
          .orElseThrow(() -> new RuntimeException("%s is not a known pipe".formatted(c)));
    }
  }

  private enum Direction {
    UP(new int[] {-1, 0}),
    RIGHT(new int[] {0, 1}),
    DOWN(new int[] {1, 0}),
    LEFT(new int[] {0, -1});

    private final int[] offsets;

    Direction(final int[] offsets) {
      this.offsets = offsets;
    }
  }

}
