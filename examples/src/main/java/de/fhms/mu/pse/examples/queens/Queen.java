package de.fhms.mu.pse.examples.queens;

import de.fhms.mu.pse.annotations.domain.object.Entity;
import de.fhms.mu.pse.annotations.domain.object.Update;
import de.fhms.mu.pse.annotations.domain.relation.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Data
@AllArgsConstructor
public class Queen {
    private final int col;
    private Row row;

    public Queen(int col) {
        this(col, null);
    }

    @OneToOne
    public Row getRow() {
        return this.row;
    }

    @Update
    public Queen setRow(Row row) {
        this.row = row;
        return this;
    }

    public boolean isSafe(Queen other) {
        if (this.row == null || other.row == null) {
            return true;
        }

        return !this.isSameRow(other)
                && !this.isSameCol(other)
                && !this.isSameDiagonal(other);
    }

    public boolean isInCell(int row, int col) {
        if (this.row == null) {
            return false;
        }

        return this.row.index() == row && this.col == col;
    }

    public boolean isSameRow(Queen other) {
        return this.row == other.row;
    }

    public boolean isSameCol(Queen other) {
        return this.col == other.col;
    }

    public boolean isSameDiagonal(Queen other) {
        return Math.abs(this.row.index() - other.row.index()) == Math.abs(this.col - other.col);
    }

    @Override
    public String toString() {
        return String.format("Queen(%d)", this.col);
    }
}
