package de.fhms.mu.pse.qubo.interpreter;

import de.fhms.mu.pse.model.problem.ElementCombinationTuple;
import de.fhms.mu.pse.model.problem.constraint.logic.BinaryLogicConstraint;
import de.fhms.mu.pse.model.problem.constraint.logic.UnaryLogicConstraint;
import de.fhms.mu.pse.model.problem.constraint.relation.ExactMultiplicityConstraint;
import de.fhms.mu.pse.model.qubo.IQuboPenalty;
import de.fhms.mu.pse.model.qubo.interpreter.IQuboConstraintInterpreter;
import de.fhms.mu.pse.qubo.penalty.QuboBinaryLogicPenalty;
import de.fhms.mu.pse.qubo.penalty.QuboExactMultiplicityPenalty;
import de.fhms.mu.pse.qubo.penalty.QuboUnaryLogicPenalty;


public class DefaultQuboConstraintInterpreter<T> implements IQuboConstraintInterpreter<T> {
    @Override
    public <S> IQuboPenalty<ElementCombinationTuple<T>> interpretConstraint(UnaryLogicConstraint<S, T> constraint) {
        return new QuboUnaryLogicPenalty<>(constraint);
    }

    @Override
    public <S> IQuboPenalty<ElementCombinationTuple<T>> interpretConstraint(BinaryLogicConstraint<S, T> constraint) {
        return new QuboBinaryLogicPenalty<>(constraint);
    }

    @Override
    public <S, B> IQuboPenalty<ElementCombinationTuple<T>> interpretConstraint(ExactMultiplicityConstraint<S, T, B> constraint) {
        return new QuboExactMultiplicityPenalty<>(constraint);
    }
}
