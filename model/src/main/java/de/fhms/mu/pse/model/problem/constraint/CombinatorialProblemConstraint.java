package de.fhms.mu.pse.model.problem.constraint;

import de.fhms.mu.pse.model.problem.ElementCombinationTuple;
import de.fhms.mu.pse.model.problem.ICombinatorialProblem;
import de.fhms.mu.pse.model.qubo.IQuboPenalty;
import de.fhms.mu.pse.model.qubo.interpreter.IQuboConstraintInterpreter;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class CombinatorialProblemConstraint<S, T> {
    private String name;
    private double weight;

    @Override
    public String toString() {
        return this.name;
    }

    public abstract boolean isSatisfied(ICombinatorialProblem<S, T> problem, S solution);
    public abstract IQuboPenalty<ElementCombinationTuple<T>> interpret(IQuboConstraintInterpreter<T> interpreter);
}
