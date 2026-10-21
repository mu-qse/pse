package de.fhms.mu.pse.examples.queens;

import de.fhms.mu.pse.annotations.domain.object.ValueObject;

@ValueObject
public record Row(int index) {
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Row(int otherIndex))) {
            return false;
        }

        return this.index == otherIndex;
    }

    @Override
    public int hashCode() {
        return this.index;
    }

    @Override
    public String toString() {
        return String.format("%d", this.index);
    }
}
