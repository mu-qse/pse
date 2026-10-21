package de.fhms.mu.pse.examples.sudoku;

import de.fhms.mu.pse.annotations.domain.object.AggregateRoot;
import de.fhms.mu.pse.annotations.domain.object.Update;
import de.fhms.mu.pse.annotations.domain.relation.OneToMany;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@AggregateRoot
@Getter
public class Sudoku {
    private final int blockLength;
    private final Cell[][] cells;
    private final Set<Integer> availableNumbers;

    public Sudoku(int blockLength) {
        this.blockLength = blockLength;
        this.cells = generateCells(blockLength);
        this.availableNumbers = generateAvailableNumbers(blockLength);
    }

    @OneToMany
    public Set<Cell> getCells() {
        return Arrays.stream(this.cells)
                .flatMap(Arrays::stream)
                .collect(Collectors.toSet());
    }

    @OneToMany
    public Set<Integer> getAvailableNumbers() {
        return Collections.unmodifiableSet(this.availableNumbers);
    }

    public Cell getCell(int row, int col) {
        return this.cells[row][col];
    }

    @Update
    public Sudoku set(Cell cell) {
        return this.set(cell.getRow(), cell.getCol(), cell.getNumber());
    }

    public Sudoku set(int row, int col, int value) {
        final var cell = new Cell(row, col, value);
        validatePlacement(cell);
        this.cells[row][col] = cell;
        return this;
    }

    @Override
    public String toString() {
        final var stats = Arrays.stream(this.cells)
                .flatMap(Arrays::stream)
                .map(cell -> Cell.valueToString(cell.getNumber()))
                .map(String::length)
                .collect(Collectors.summarizingInt(Integer::intValue));
        final var pad = stats.getMax();
        return Arrays.stream(this.cells)
                .map(row -> "[ " + Arrays.stream(row)
                        .map(cell -> String.format("%" + pad + "s", Cell.valueToString(cell.getNumber())))
                        .collect(Collectors.joining(", ")) + " ]"
                ).collect(Collectors.joining(",\n"));
    }

    private void validatePlacement(Cell cell) {
        if (cell.getNumber() == null) {
            throw new IllegalArgumentException("Cell has no number");
        }

        final var existingCells = this.getCells();
        if (!existingCells.contains(cell)) {
            throw new IllegalArgumentException("Cell is not within board: " + cell.getRow());
        }

        for (final var existingCell : existingCells) {
            if (existingCell.equals(cell)) {
                if (existingCell.hasNumber() && !Objects.equals(existingCell.getNumber(), cell.getNumber())) {
                    throw new IllegalArgumentException("Cell has already a number: " + cell);
                }
                continue;
            }

            if (existingCell.isSameNumber(cell)) {
                if (existingCell.isSameRow(cell)
                        || existingCell.isSameCol(cell)
                        || existingCell.isSameBlock(cell, this.blockLength)) {
                    throw new IllegalStateException("Cell conflicts with: " + cell);
                }
            }
        }
    }

    private static Cell[][] generateCells(int blockLength) {
        if (blockLength < 2) {
            throw new IllegalArgumentException("Length must be at least 2");
        }

        final var gridLength = blockLength * blockLength;
        final var cells = new Cell[gridLength][gridLength];
        for (int row = 0; row < gridLength; row++) {
            for (int col = 0; col < gridLength; col++) {
                cells[row][col] = new Cell(row, col);
            }
        }
        return cells;
    }

    private static Set<Integer> generateAvailableNumbers(int blockLength) {
        return IntStream.rangeClosed(1, blockLength * blockLength).boxed().collect(Collectors.toSet());
    }
}
