package de.fhms.mu.pse.model.qubo.interpreter;

import de.fhms.mu.pse.model.problem.ElementCombinationTuple;
import de.fhms.mu.pse.model.problem.constraint.logic.BinaryLogicConstraint;
import de.fhms.mu.pse.model.problem.constraint.logic.UnaryLogicConstraint;
import de.fhms.mu.pse.model.problem.constraint.relation.ExactMultiplicityConstraint;
import de.fhms.mu.pse.model.qubo.IQuboPenalty;

public interface IQuboConstraintInterpreter<T> {
    <S> IQuboPenalty<ElementCombinationTuple<T>> interpretConstraint(UnaryLogicConstraint<S, T> constraint);
    <S> IQuboPenalty<ElementCombinationTuple<T>> interpretConstraint(BinaryLogicConstraint<S, T> constraint);

    <S, B> IQuboPenalty<ElementCombinationTuple<T>> interpretConstraint(ExactMultiplicityConstraint<S, T, B> constraint);
}
