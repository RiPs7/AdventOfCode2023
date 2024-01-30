package com.rips7.day;

import com.rips7.util.Util;
import com.rips7.util.Util.Offset;
import com.rips7.util.Util.Position;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day23 implements Day<Integer> {

    @Override
    public Integer part1(String input) {
        final Grid grid = Grid.parse(input);

        final ReducedGraph graph = ReducedGraph.from(grid, true);

        return graph.findLongestPath();
    }

    @Override
    public Integer part2(String input) {
        final Grid grid = Grid.parse(input);

        final ReducedGraph graph = ReducedGraph.from(grid, false);

        return graph.findLongestPath();
    }

    private record ReducedGraph(Cell start, Cell end, Map<Cell, Map<Cell, Integer>> adjacencyMatrix) {
        private static final Map<Spot, List<Offset>> SPOT_DIRECTIONS = Map.of(
            Spot.FOREST, List.of(),
            Spot.SLOPE_UP, List.of(Offset.UP),
            Spot.SLOPE_RIGHT, List.of(Offset.RIGHT),
            Spot.SLOPE_DOWN, List.of(Offset.DOWN),
            Spot.SLOPE_LEFT, List.of(Offset.LEFT),
            Spot.PATH, Offset.getOffsets());

        private record Node(Cell cell, Integer dist) {}

        private static ReducedGraph from(final Grid grid, final boolean withSlopes) {
            final List<Cell> points = findCriticalPoints(grid);
            final Map<Cell, Map<Cell, Integer>> adjacencyMatrix = calculateAdjacencyMatrix(points, grid, withSlopes);
            return new ReducedGraph(grid.getStart(), grid.getEnd(), adjacencyMatrix);
        }

        private static List<Cell> findCriticalPoints(final Grid grid) {
            final List<Cell> points = new ArrayList<>();
            points.add(grid.getStart());
            points.add(grid.getEnd());
            Util.loop2D(grid.cells, cell -> {
                if (cell.spot == Spot.FOREST) {
                    return;
                }
                final int neighbors = (int) Offset.getOffsets().stream()
                    .map(cell.pos::apply)
                    .filter(neighborPos -> Util.isWithinGrid(neighborPos, grid.cells))
                    .map(neighborPos -> grid.cells[neighborPos.x()][neighborPos.y()])
                    .filter(neighbor -> neighbor.spot != Spot.FOREST)
                    .count();
                if (neighbors >= 3) {
                    points.add(cell);
                }
            });
            return points;
        }

        private static Map<Cell, Map<Cell, Integer>> calculateAdjacencyMatrix(final List<Cell> points, final Grid grid,
                                                                              final boolean withSlopes) {
            final Map<Cell, Map<Cell, Integer>> adjacencyMatrix = points.stream()
                .collect(Collectors.toMap(Function.identity(), e -> new HashMap<>()));

            points.forEach(start -> {
                final Stack<Node> stack = new Stack<>();
                stack.add(new Node(start, 0));
                final Set<Cell> seen = new HashSet<>();
                seen.add(start);
                while(!stack.isEmpty()) {
                    final Node current = stack.pop();
                    if (current.dist != 0 && points.contains(current.cell)) {
                        adjacencyMatrix.get(start).put(current.cell, current.dist);
                        continue;
                    }
                    final List<Offset> offsets = withSlopes ? SPOT_DIRECTIONS.get(current.cell.spot) : Offset.getOffsets();
                    offsets.stream()
                        .map(current.cell.pos::apply)
                        .filter(pos -> Util.isWithinGrid(pos, grid.cells))
                        .map(pos -> grid.cells[pos.x()][pos.y()])
                        .filter(neighbor -> neighbor.spot != Spot.FOREST)
                        .filter(neighbor -> !seen.contains(neighbor))
                        .forEach(neighbor -> {
                            stack.push(new Node(neighbor, current.dist + 1));
                            seen.add(neighbor);
                        });
                }
            });

            return adjacencyMatrix;
        }

        private int findLongestPath() {
            return findPathDFS(start, new HashSet<>());
        }

        private int findPathDFS(final Cell cell, final Set<Cell> seen) {
            if (cell.equals(end)) {
                return 0;
            }
            final AtomicInteger len = new AtomicInteger(Integer.MIN_VALUE);
            seen.add(cell);
            adjacencyMatrix.get(cell).keySet().forEach(neighbor -> {
                if (!seen.contains(neighbor)) {
                    len.set(Math.max(len.get(), findPathDFS(neighbor, seen) + adjacencyMatrix.get(cell).get(neighbor)));
                }
            });
            seen.remove(cell);

            return len.get();
        }
    }

    private record Grid(Cell[][] cells) {
        private static Grid parse(final String input) {
            final Spot[][] spots = Arrays.stream(input.split("\n"))
                .map(line -> line.chars().mapToObj(c -> (char) c).map(Spot::from).toArray(Spot[]::new))
                .toArray(Spot[][]::new);
            final Cell[][] cells = IntStream.range(0, spots.length)
                .mapToObj(row -> IntStream.range(0, spots[row].length)
                    .mapToObj(col -> new Cell(Position.of(row, col), spots[row][col]))
                    .toArray(Cell[]::new))
                .toArray(Cell[][]::new);
            return new Grid(cells);
        }

        private Cell getStart() {
            return Arrays.stream(cells[0])
                .filter(cell -> cell.spot == Spot.PATH)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot find start"));
        }

        private Cell getEnd() {
            return Arrays.stream(cells[cells.length - 1])
                .filter(cell -> cell.spot == Spot.PATH)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot find end"));
        }
    }

    private record Cell(Position pos, Spot spot) {}

    private enum Spot {
        PATH('.'),
        FOREST('#'),
        SLOPE_UP('^'),
        SLOPE_LEFT('<'),
        SLOPE_DOWN('v'),
        SLOPE_RIGHT('>');

        private final char value;

        Spot(final char value) {
            this.value = value;
        }

        private static Spot from(final char c) {
            return Arrays.stream(values())
                .filter(spot -> spot.value == c)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unknown value '%s' for spot".formatted(c)));
        }
    }
}