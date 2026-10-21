package de.fhms.mu.pse.qubo.penalty;

import de.fhms.mu.pse.model.primitive.Pair;
import de.fhms.mu.pse.model.primitive.matrix.MatrixIndex;
import de.fhms.mu.pse.model.problem.ElementCombinationTuple;
import de.fhms.mu.pse.model.problem.constraint.logic.UnaryLogicConstraint;
import de.fhms.mu.pse.model.problem.encoding.BinaryVariable;
import de.fhms.mu.pse.model.qubo.IQuboPenalty;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class QuboUnaryLogicPenalty<T> implements IQuboPenalty<ElementCombinationTuple<T>> {
    private final UnaryLogicConstraint<?, T> constraint;
    private final double lambda;

    public QuboUnaryLogicPenalty(UnaryLogicConstraint<?, T> constraint) {
        this(constraint, constraint.getWeight());
    }

    @Override
    public double getLinearCoefficient(int index, BinaryVariable<ElementCombinationTuple<T>> variable) {
        final var combinationTuple = variable.getLabel();
        final var combination = combinationTuple.create();
        return !this.constraint.eval(combination)
                ? this.lambda
                : 0.0;
    }

    @Override
    public double getQuadraticCoefficient(MatrixIndex index, Pair<BinaryVariable<ElementCombinationTuple<T>>> variablePair) {
        return 0.0;
    }

    @Override
    public double getOffsetValue() {
        return 0.0;
    }
}
