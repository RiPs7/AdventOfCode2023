package com.rips7.day;

import com.rips7.util.Util;
import com.rips7.util.algorithms.pathfinding.Dijkstra;
import com.rips7.util.maths.Maths.Vector2D;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Day17 implements Day<Integer> {

    @Override
    public Integer part1(String input) {
        final Integer[][] grid = parseInput(input);

        return new Dijkstra<Cell>().run(
                new Cell(Vector2D.of(0, 0), null, 0),
                cell -> cell.position().equals(Vector2D.of(grid.length - 1, grid[0].length - 1)),
                (cell, currentCost) -> getNeighbors(cell, currentCost.intValue(), grid, true))
            .right()
            .intValue();
    }

    @Override
    public Integer part2(String input) {
        final Integer[][] grid = parseInput(input);

        return new Dijkstra<Cell>().run(
                new Cell(Vector2D.of(0, 0), null, 0),
                cell -> cell.position().equals(Vector2D.of(grid.length - 1, grid[0].length - 1)) && cell.sameDirection >= 4,
                (cell, currentCost) -> getNeighbors(cell, currentCost.intValue(), grid, false))
            .right()
            .intValue();
    }

    private Integer[][] parseInput(final String input) {
        final String[] lines = input.split("\n");
        return Arrays.stream(lines)
            .map(line -> line.chars()
                .mapToObj(c -> (char) c)
                .map(String::valueOf)
                .map(Integer::parseInt)
                .toArray(Integer[]::new))
            .toArray(Integer[][]::new);
    }

    private Map<Cell, Double> getNeighbors(final Cell current, final int currentCost, final Integer[][] grid, boolean part1) {
        final int minSameDir = part1 ? 0 : 4;
        final int maxSameDir = part1 ? 3 : 10;
        final Map<Cell, Double> neighbors = new HashMap<>();
        if (current.sameDirection < maxSameDir && !current.position.equals(Vector2D.of(0, 0))) {
            final Vector2D<Integer> nextPosition = current.direction.apply(current.position);
            if (Util.isWithinGrid(nextPosition, grid)) {
                final int nextCost = currentCost + grid[nextPosition.x()][nextPosition.y()];
                neighbors.put(new Cell(nextPosition, current.direction, current.sameDirection + 1), (double) nextCost);
            }
        }
        if (current.sameDirection >= minSameDir || current.position.equals(Vector2D.of(0, 0))) {
            Arrays.stream(Direction.values())
                .filter(dir -> !dir.equals(current.direction))
                .filter(dir -> dir.isNotOpposite(current.direction))
                .forEach(dir -> {
                    final Vector2D<Integer> nextPosition = dir.apply(current.position);
                    if (Util.isWithinGrid(nextPosition, grid)) {
                        final int nextCost = currentCost + grid[nextPosition.x()][nextPosition.y()];
                        neighbors.put(new Cell(nextPosition, dir, 1), (double) nextCost);
                    }
                });
        }
        return neighbors;
    }

    private record Cell(Vector2D<Integer> position, Direction direction, int sameDirection) {
    }

    private enum Direction {
        UP(Vector2D.of(-1, 0)),
        DOWN(Vector2D.of(1, 0)),
        RIGHT(Vector2D.of(0, 1)),
        LEFT(Vector2D.of(0, -1));

        private final Vector2D<Integer> offset;

        Direction(final Vector2D<Integer> offset) {
            this.offset = offset;
        }

        private Vector2D<Integer> apply(final Vector2D<Integer> position) {
            return Vector2D.add(position, offset, Integer::sum);
        }

        private boolean isNotOpposite(final Direction direction) {
            return direction == null || switch (this) {
                case LEFT -> direction != RIGHT;
                case RIGHT -> direction != LEFT;
                case UP -> direction != DOWN;
                case DOWN -> direction != UP;
            };
        }
    }
}