package de.fhms.mu.pse.examples.nsp;

import de.fhms.mu.pse.annotations.problem.ProblemConfiguration;
import de.fhms.mu.pse.annotations.problem.ProblemConstraint;
import de.fhms.mu.pse.annotations.problem.SelectableElements;
import de.fhms.mu.pse.model.domain.relation.ManyToOneRelation;
import de.fhms.mu.pse.model.problem.constraint.CombinatorialProblemConstraint;
import de.fhms.mu.pse.model.problem.constraint.relation.ExactMultiplicityConstraint;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

@RequiredArgsConstructor
@ProblemConfiguration(aggregateRoot = Schedule.class, combinationTuple = Assignment.class)
public class SimpleNsp {
    private final Collection<Shift> shifts;
    private final Collection<Nurse> nurses;
    private final int numberOfAssignmentsPerShift;
    private final int recoveryBetweenShifts;

    @SelectableElements
    public Collection<Shift> getShiftsToSchedule() {
        return this.shifts;
    }

    @SelectableElements
    public Collection<Nurse> getAvailableNurses() {
        return this.nurses;
    }

    @ProblemConstraint
    public CombinatorialProblemConstraint<?, ?> enforceAssignmentsPerShift(ManyToOneRelation<Assignment, Shift> relation) {
        return new ExactMultiplicityConstraint<>(relation, this.numberOfAssignmentsPerShift);
    }

    @ProblemConstraint
    boolean enforceRecoveryBetweenShifts(Assignment a, Assignment b) {
        if (!a.getNurse().equals(b.getNurse())) {
            return true;
        }

        final var distance = Math.abs(a.getShift().start() - b.getShift().start());
        return Math.abs(distance) > this.recoveryBetweenShifts;
    }
}
