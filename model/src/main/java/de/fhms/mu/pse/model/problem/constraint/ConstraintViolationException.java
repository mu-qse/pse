package de.fhms.mu.pse.model.problem.constraint;

public class ConstraintViolationException extends RuntimeException {
    public ConstraintViolationException(CombinatorialProblemConstraint<?, ?> constraint) {
        super(String.format("Constraint violation: %s", constraint.toString()));
    }
}
