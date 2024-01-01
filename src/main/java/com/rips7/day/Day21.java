package com.rips7.day;

import com.rips7.util.Util;
import com.rips7.util.maths.Maths.Vector2D;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day21 implements Day<Long> {

    @Override
    public Long part1(String input) {
        final StepMap stepMap = StepMap.parse(input);
        return stepMap.walkStepsPart1(64);
    }

    @Override
    public Long part2(String input) {
        final StepMap stepMap = StepMap.parse(input);
        final int steps = 26501365;
        stepMap.verifyAssumptionsPart2(steps);
        return stepMap.walkStepsPart2(steps);
    }

    private record StepMap(Cell[][] cells) {
        private static StepMap parse(final String input) {
            final CellType[][] cellTypes = Arrays.stream(input.split("\n"))
                .map(line -> line.chars()
                    .mapToObj(c -> (char) c)
                    .map(CellType::from)
                    .toArray(CellType[]::new))
                .toArray(CellType[][]::new);
            final Cell[][] cells = IntStream.range(0, cellTypes.length)
                .mapToObj(row -> IntStream.range(0, cellTypes[row].length)
                    .mapToObj(col -> new Cell(Vector2D.of(row, col), cellTypes[row][col]))
                    .toArray(Cell[]::new))
                .toArray(Cell[][]::new);
            return new StepMap(cells);
        }

        private Cell getStartCell() {
            return Arrays.stream(cells)
                .flatMap(Arrays::stream)
                .filter(cell -> cell.cellType == CellType.START)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot find start cell"));
        }

        private Cell getCell(final int row, final int col) {
            return cells[row][col];
        }

        @SuppressWarnings("SameParameterValue")
        private void verifyAssumptionsPart2(final int steps) {
            // Verify grid is square
            assertWithMessage(
                () -> cells.length == cells[0].length,
                "The grid has to be square");

            final Cell start = getStartCell();
            // Verify START is in the middle
            assertWithMessage(
                () -> start.position.equals(Vector2D.of(cells.length / 2, cells[0].length / 2)),
                "The start has to be in the center of the grid");

            // Verify steps is
            assertWithMessage(
                () -> steps % cells.length == cells.length / 2,
                "Step number has to be a multiple of half the grid size");

            // Verify row with START is all empty
            assertWithMessage(
                () -> {
                    for (int col = 0; col < cells[start.position.x()].length; col++) {
                        if (col == start.position.y()) {
                            continue;
                        }
                        if (cells[start.position.x()][col].cellType != CellType.GARDEN) {
                            return false;
                        }
                    }
                    return true;
                },
                "Row with start has to be empty");

            // Verify col with START is all empty
            assertWithMessage(
                () -> {
                    for (int row = 0; row < cells.length; row++) {
                        if (row == start.position.x()) {
                            continue;
                        }
                        if (cells[row][start.position.y()].cellType != CellType.GARDEN) {
                            return false;
                        }
                    }
                    return true;
                },
                "Column with start has to be empty");

        }

        @SuppressWarnings("SameParameterValue")
        private Long walkStepsPart1(final int steps) {
            return (long) walkSteps(getStartCell(), steps);
        }

        @SuppressWarnings("SameParameterValue")
        private long walkStepsPart2(final int steps) {
            final int gridSize = cells.length;
            final long repeatingGridWidth = steps / gridSize - 1;

            final Cell startCell = getStartCell();

            final long oddRepeatingGrids = ((repeatingGridWidth / 2) * 2 + 1) * ((repeatingGridWidth / 2) * 2 + 1);
            final long evenRepeatingGrids = (((repeatingGridWidth + 1) / 2) * 2) * (((repeatingGridWidth + 1) / 2) * 2);

            final long reachablePtsInOddRepeatingGrids = walkSteps(startCell, gridSize * 2 + 1);
            final long reachablePtsInEvenRepeatingGrids = walkSteps(startCell, gridSize * 2);

            final long reachablePtsInTopGrid = walkSteps(getCell(gridSize - 1, startCell.position.y()), gridSize - 1);
            final long reachablePtsInRightGrid = walkSteps(getCell(startCell.position.x(), 0), gridSize - 1);
            final long reachablePtsInBottomGrid = walkSteps(getCell(0, startCell.position.y()), gridSize - 1);
            final long reachablePtsInLeftGrid = walkSteps(getCell(startCell.position.x(), gridSize - 1), gridSize - 1);

            final long reachablePtsInTopRightSmall = walkSteps(getCell(gridSize - 1, 0), gridSize / 2 - 1);
            final long reachablePtsInTopLeftSmall = walkSteps(getCell(gridSize - 1, gridSize - 1), gridSize / 2 - 1);
            final long reachablePtsInBottomRightSmall = walkSteps(getCell(0, 0), gridSize / 2 - 1);
            final long reachablePtsInBottomLeftSmall = walkSteps(getCell(0, gridSize - 1), gridSize / 2 - 1);

            final long reachablePtsInTopRightBig = walkSteps(getCell(gridSize - 1, 0), 3 * gridSize / 2 - 1);
            final long reachablePtsInTopLeftBig = walkSteps(getCell(gridSize - 1, gridSize - 1), 3 * gridSize / 2 - 1);
            final long reachablePtsInBottomRightBig = walkSteps(getCell(0, 0), 3 * gridSize / 2 - 1);
            final long reachablePtsInBottomLeftBig = walkSteps(getCell(0, gridSize - 1), 3 * gridSize / 2 - 1);

            return
                oddRepeatingGrids * reachablePtsInOddRepeatingGrids + evenRepeatingGrids * reachablePtsInEvenRepeatingGrids +
                reachablePtsInTopGrid + reachablePtsInRightGrid + reachablePtsInBottomGrid + reachablePtsInLeftGrid +
                (repeatingGridWidth + 1) *
                    (reachablePtsInTopRightSmall + reachablePtsInTopLeftSmall + reachablePtsInBottomRightSmall + reachablePtsInBottomLeftSmall) +
                (repeatingGridWidth) *
                    (reachablePtsInTopRightBig + reachablePtsInTopLeftBig + reachablePtsInBottomRightBig + reachablePtsInBottomLeftBig);
        }

        private int walkSteps(final Cell start, final int steps) {
            final Queue<CellWithAvailableSteps> frontier = new ArrayDeque<>();
            final Set<Vector2D<Integer>> closedSet = new HashSet<>();
            final Set<Vector2D<Integer>> result = new HashSet<>();
            frontier.add(new CellWithAvailableSteps(start, steps));
            while (!frontier.isEmpty()) {
                final CellWithAvailableSteps current = frontier.poll();
                if (closedSet.contains(current.position)) {
                    continue;
                }
                closedSet.add(current.position);
                if (current.availableSteps % 2 == 0) {
                    result.add(current.position);
                }
                if (current.availableSteps == 0) {
                    continue;
                }
                Stream.of(Vector2D.of(-1, 0), Vector2D.of(1, 0), Vector2D.of(0, -1), Vector2D.of(0, 1))
                    .map(offset -> Vector2D.add(current.position, offset, Integer::sum))
                    .filter(pos -> Util.isWithinGrid(pos, cells))
                    .map(pos -> cells[pos.x()][pos.y()])
                    .filter(cell -> cell.cellType != CellType.ROCK)
                    .map(cell -> new CellWithAvailableSteps(cell, current.availableSteps - 1))
                    .forEach(frontier::add);
            }
            return result.size();
        }

        private record CellWithAvailableSteps(Vector2D<Integer> position, int availableSteps) {
            private CellWithAvailableSteps(final Cell cell, final int availableSteps) {
                this(cell.position, availableSteps);
            }
        }

    }

    private record Cell(Vector2D<Integer> position, CellType cellType) { }

    private enum CellType {
        START('S'),
        GARDEN('.'),
        ROCK('#');

        private final char value;

        CellType(final char value) {
            this.value = value;
        }

        private static CellType from(final char c) {
            return Arrays.stream(CellType.values())
                .filter(ct -> ct.value == c)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot parse CellType from %s".formatted(c)));
        }
    }

    private static void assertWithMessage(final Supplier<Boolean> assertion, final String message) {
        if (!assertion.get()) {
            throw new RuntimeException(message);
        }
    }
}