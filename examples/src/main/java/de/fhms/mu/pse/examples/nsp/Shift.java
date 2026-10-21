package de.fhms.mu.pse.examples.nsp;

import de.fhms.mu.pse.annotations.domain.object.Entity;

@Entity
public record Shift(int start) {
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Shift(int otherStart))) {
            return false;
        }

        return this.start == otherStart;
    }

    @Override
    public int hashCode() {
        return this.start;
    }

    @Override
    public String toString() {
        return String.valueOf(this.start);
    }
}
