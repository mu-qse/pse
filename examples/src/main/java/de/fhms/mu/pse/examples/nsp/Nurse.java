package de.fhms.mu.pse.examples.nsp;

import de.fhms.mu.pse.annotations.domain.object.Entity;

@Entity
public record Nurse(String name) {
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Nurse(String otherName))) {
            return false;
        }

        return this.name.equals(otherName);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public String toString() {
        return this.name;
    }
}
