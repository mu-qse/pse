package de.fhms.mu.pse.qubo.penalty;

import de.fhms.mu.pse.model.primitive.Pair;
import de.fhms.mu.pse.model.primitive.matrix.MatrixIndex;
import de.fhms.mu.pse.model.problem.ElementCombinationTuple;
import de.fhms.mu.pse.model.problem.constraint.logic.BinaryLogicConstraint;
import de.fhms.mu.pse.model.problem.encoding.BinaryVariable;
import de.fhms.mu.pse.model.qubo.IQuboPenalty;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class QuboBinaryLogicPenalty<T> implements IQuboPenalty<ElementCombinationTuple<T>> {
    private final BinaryLogicConstraint<?, T> constraint;
    private final double lambda;

    public QuboBinaryLogicPenalty(BinaryLogicConstraint<?, T> constraint) {
        this(constraint, constraint.getWeight());
    }

    @Override
    public double getLinearCoefficient(int index, BinaryVariable<ElementCombinationTuple<T>> variable) {
        return 0.0;
    }

    @Override
    public double getQuadraticCoefficient(MatrixIndex index, Pair<BinaryVariable<ElementCombinationTuple<T>>> binaryVariablePair) {
        final var combinationTupleA = binaryVariablePair.getA().getLabel();
        final var combinationTupleB = binaryVariablePair.getB().getLabel();
        final var combinationA = combinationTupleA.create();
        final var combinationB = combinationTupleB.create();
        return !this.constraint.eval(combinationA, combinationB)
                ? 2.0 * this.lambda
                : 0.0;
    }

    @Override
    public double getOffsetValue() {
        return 0.0;
    }
}
