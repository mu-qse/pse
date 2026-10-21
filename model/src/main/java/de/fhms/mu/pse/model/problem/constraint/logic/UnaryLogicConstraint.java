package de.fhms.mu.pse.model.problem.constraint.logic;

import de.fhms.mu.pse.model.problem.ElementCombinationTuple;
import de.fhms.mu.pse.model.problem.ICombinatorialProblem;
import de.fhms.mu.pse.model.problem.constraint.CombinatorialProblemConstraint;
import de.fhms.mu.pse.model.qubo.IQuboPenalty;
import de.fhms.mu.pse.model.qubo.interpreter.IQuboConstraintInterpreter;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class UnaryLogicConstraint<S, T> extends CombinatorialProblemConstraint<S, T> {
    public UnaryLogicConstraint(String name) {
        super(name, 1.0);
    }

    @Override
    public boolean isSatisfied(ICombinatorialProblem<S, T> problem, S solution) {
        final var combinations = problem.getSolutionDomainObjectRelation().query(solution);
        for (final var combination : combinations) {
            if (!this.eval(combination)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public IQuboPenalty<ElementCombinationTuple<T>> interpret(IQuboConstraintInterpreter<T> interpreter) {
        return interpreter.interpretConstraint(this);
    }

    public abstract boolean eval(T tuple);
}
