package de.fhms.mu.pse.examples.nsp;

import de.fhms.mu.pse.annotations.domain.object.AggregateRoot;
import de.fhms.mu.pse.annotations.domain.object.Query;
import de.fhms.mu.pse.annotations.domain.object.Update;
import de.fhms.mu.pse.annotations.domain.relation.OneToMany;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;

@AggregateRoot
@RequiredArgsConstructor
public class Schedule {
    @OneToMany
    private final Collection<Assignment> assignments = new ArrayList<>();

    @Query
    public Collection<Assignment> getAssignments() {
        return this.assignments;
    }

    @Update
    public Schedule add(Assignment assignment) {
        this.assignments.add(assignment);
        return this;
    }

    @Override
    public String toString() {
        return this.assignments.toString();
    }
}
