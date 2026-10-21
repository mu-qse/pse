package de.fhms.mu.pse.examples.nsp;

import de.fhms.mu.pse.annotations.domain.object.Query;
import de.fhms.mu.pse.annotations.domain.object.Update;
import de.fhms.mu.pse.annotations.domain.object.ValueObject;
import de.fhms.mu.pse.annotations.domain.relation.ManyToOne;
import lombok.RequiredArgsConstructor;

@ValueObject
@RequiredArgsConstructor
public class Assignment {
    @ManyToOne
    private final Shift shift;
    @ManyToOne
    private final Nurse nurse;

    public Assignment() {
        this(null, null);
    }

    @Query
    public Shift getShift() {
        return this.shift;
    }

    @Query
    public Nurse getNurse() {
        return this.nurse;
    }

    @Update
    public Assignment withShift(Shift shift) {
        return new Assignment(shift, this.nurse);
    }

    @Update
    public Assignment withNurse(Nurse nurse) {
        return new Assignment(this.shift, nurse);
    }

    @Override
    public String toString() {
        return String.format("(%s: %s)", this.shift, this.nurse);
    }
}
