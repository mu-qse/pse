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
public abstract class BinaryLogicConstraint<S, T> extends CombinatorialProblemConstraint<S, T> {
    public BinaryLogicConstraint(String name) {
        super(name, 1.0);
    }

    @Override
    public boolean isSatisfied(ICombinatorialProblem<S, T> problem, S solution) {
        final var combinations = problem.getSolutionDomainObjectRelation().query(solution);
        for (var a : combinations) {
            for (var b : combinations) {
                if (a == b) {
                    continue;
                }

                if (!this.eval(a, b)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public IQuboPenalty<ElementCombinationTuple<T>> interpret(IQuboConstraintInterpreter<T> interpreter) {
        return interpreter.interpretConstraint(this);
    }

    public abstract boolean eval(T a, T b);
}
