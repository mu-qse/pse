package de.fhms.mu.pse.examples.sudoku;

import de.fhms.mu.pse.annotations.domain.object.Update;
import de.fhms.mu.pse.annotations.domain.object.ValueObject;
import de.fhms.mu.pse.annotations.domain.relation.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Objects;

@ValueObject
@Data
@AllArgsConstructor
public class Cell {
    private final int row;
    private final int col;
    private final Integer number;

    public Cell(int row, int col) {
        this(row, col, null);
    }

    public Cell(Cell other, Integer number) {
        this(other.row, other.col, number);
    }

    @ManyToOne
    public Integer getNumber() {
        return this.number;
    }

    @Update
    public Cell withNumber(Integer value) {
        return new Cell(this, value);
    }

    public boolean hasNumber() {
        return !this.isEmpty();
    }

    public boolean isEmpty() {
        return this.number == null || this.number == 0;
    }

    public boolean isSameCol(Cell other) {
        return this.col == other.col;
    }

    public boolean isSameRow(Cell other) {
        return this.row == other.row;
    }

    public boolean isSameNumber(Cell other) {
        if (this.number == null || other.number == null) {
            return false;
        }
        return Objects.equals(this.number, other.number);
    }

    public boolean isSameBlock(Cell other, int blockLength) {
        final var aBlockRow = (int) Math.floor((double) this.row / blockLength);
        final var aBlockCol = (int) Math.floor((double) this.col / blockLength);
        final var bBlockRow = (int) Math.floor((double) other.row / blockLength);
        final var bBlockCol = (int) Math.floor((double) other.col / blockLength);

        return aBlockRow == bBlockRow && aBlockCol == bBlockCol;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Cell otherCell
                && this.isSameRow(otherCell)
                && this.isSameCol(otherCell);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.row, this.col);
    }

    @Override
    public String toString() {
        return this.hasNumber()
                ? String.format("(%d, %d): %d", this.row, this.col, this.number)
                : String.format("(%d, %d)", this.row, this.col);
    }

    public static String valueToString(Integer value) {
        return value != null && value != 0 ? value.toString() : "-";
    }

}
