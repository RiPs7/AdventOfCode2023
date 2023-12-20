package com.rips7.day;

import com.rips7.util.algorithms.pathfinding.DFS;
import com.rips7.util.maths.Maths.Vector2D;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.rips7.util.maths.Maths.inBetween;

public class Day16 implements Day<Integer> {

    private static final Map<Character, Map<Direction, List<Direction>>> DIRECTIONS = Map.of(
        '.', Map.of(
            Direction.RIGHT, List.of(Direction.RIGHT),
            Direction.DOWN, List.of(Direction.DOWN),
            Direction.LEFT, List.of(Direction.LEFT),
            Direction.UP, List.of(Direction.UP)),
        '/', Map.of(
            Direction.RIGHT, List.of(Direction.UP),
            Direction.DOWN, List.of(Direction.LEFT),
            Direction.LEFT, List.of(Direction.DOWN),
            Direction.UP, List.of(Direction.RIGHT)),
        '\\', Map.of(
            Direction.RIGHT, List.of(Direction.DOWN),
            Direction.DOWN, List.of(Direction.RIGHT),
            Direction.LEFT, List.of(Direction.UP),
            Direction.UP, List.of(Direction.LEFT)),
        '|', Map.of(
            Direction.RIGHT, List.of(Direction.UP, Direction.DOWN),
            Direction.DOWN, List.of(Direction.DOWN),
            Direction.LEFT, List.of(Direction.UP, Direction.DOWN),
            Direction.UP, List.of(Direction.UP)
        ),
        '-', Map.of(
            Direction.RIGHT, List.of(Direction.RIGHT),
            Direction.DOWN, List.of(Direction.LEFT, Direction.RIGHT),
            Direction.LEFT, List.of(Direction.LEFT),
            Direction.UP, List.of(Direction.LEFT, Direction.RIGHT)));

    @Override
    public Integer part1(String input) {
        final RawCell[][] contraption = parseInput(input);
        final Cell start = new Cell(new RawCell(Vector2D.of(0, -1), '.'), Direction.RIGHT);
        return runAndFindLitCells(start, contraption);
    }

    @Override
    public Integer part2(String input) {
        final RawCell[][] contraption = parseInput(input);

        final List<Cell> possibleTopStarts = IntStream.range(0, contraption[0].length)
            .mapToObj(i -> new Cell(new RawCell(Vector2D.of(-1, i), '.'), Direction.DOWN))
            .toList();
        final List<Cell> possibleRightStarts = IntStream.range(0, contraption.length)
            .mapToObj(i -> new Cell(new RawCell(Vector2D.of(i, contraption[0].length), '.'), Direction.LEFT))
            .toList();
        final List<Cell> possibleBottomStarts = IntStream.range(0, contraption[0].length)
            .mapToObj(i -> new Cell(new RawCell(Vector2D.of(contraption.length, i), '.'), Direction.UP))
            .toList();
        final List<Cell> possibleLeftStarts = IntStream.range(0, contraption[0].length)
            .mapToObj(i -> new Cell(new RawCell(Vector2D.of(i, -1), '.'), Direction.RIGHT))
            .toList();

        final List<Cell> possibleStarts = Stream.of(possibleTopStarts, possibleRightStarts, possibleBottomStarts, possibleLeftStarts)
            .flatMap(Collection::stream)
            .toList();

        int maxNumberOfLitCells = 0;
        for (final Cell start : possibleStarts) {
            final int numberOfLitCells = runAndFindLitCells(start, contraption);
            if (numberOfLitCells > maxNumberOfLitCells) {
                maxNumberOfLitCells = numberOfLitCells;
            }
        }
        return maxNumberOfLitCells;
    }

    private RawCell[][] parseInput(final String input) {
        final String[] lines = input.split("\n");
        return IntStream.range(0, lines.length)
            .mapToObj(r -> IntStream.range(0, lines[r].length())
                .mapToObj(c -> new RawCell(Vector2D.of(r, c), lines[r].charAt(c)))
                .toArray(RawCell[]::new))
            .toArray(RawCell[][]::new);
    }

    private int runAndFindLitCells(final Cell start, final RawCell[][] contraption) {
        final Set<RawCell> litCells = new HashSet<>();
        try {
            new DFS<Cell>().run(start, null, cell -> this.getNeighbors(cell, contraption, litCells));
        } catch (final Exception e) {
            // There is no "solution" for DFS
        }
        return litCells.size();
    }

    private List<Cell> getNeighbors(final Cell cell, final RawCell[][] contraption, final Set<RawCell> litCells) {
        if (inBetween(cell.rawCell.position.x(), 0, contraption.length) &&
            inBetween(cell.rawCell.position.y(), 0, contraption[0].length)) {
                litCells.add(cell.rawCell);
        }
        try {
            final int r = cell.rawCell.position.x();
            final int c = cell.rawCell.position.y();
            final RawCell nextCell = switch (cell.light) {
                case RIGHT -> contraption[r][c + 1];
                case DOWN -> contraption[r + 1][c];
                case LEFT -> contraption[r][c - 1];
                case UP -> contraption[r - 1][c];
            };
            final List<Direction> nextDirections = DIRECTIONS.get(nextCell.value).get(cell.light);
            return nextDirections.stream()
                .map(nextDir -> new Cell(nextCell, nextDir))
                .toList();
        } catch (final ArrayIndexOutOfBoundsException e) {
            // ignore
        }
        return List.of();
    }

    private record RawCell(Vector2D<Integer> position, Character value) {}

    private record Cell(RawCell rawCell, Direction light) {}

    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }
}