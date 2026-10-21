package de.fhms.mu.pse.qubo.penalty;

import de.fhms.mu.pse.model.primitive.Pair;
import de.fhms.mu.pse.model.primitive.matrix.MatrixIndex;
import de.fhms.mu.pse.model.problem.ElementCombinationTuple;
import de.fhms.mu.pse.model.problem.constraint.relation.ExactMultiplicityConstraint;
import de.fhms.mu.pse.model.problem.encoding.BinaryVariable;
import de.fhms.mu.pse.model.qubo.IQuboPenalty;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class QuboExactMultiplicityPenalty<T> implements IQuboPenalty<ElementCombinationTuple<T>> {
    private final ExactMultiplicityConstraint<?, T, ?> constraint;
    private final double lambda;

    public QuboExactMultiplicityPenalty(ExactMultiplicityConstraint<?, T, ?> constraint) {
        this(constraint, constraint.getWeight());
    }

    @Override
    public double getLinearCoefficient(int index, BinaryVariable<ElementCombinationTuple<T>> variable) {
        final var k = constraint.getMultiplicity();
        return this.lambda * (1.0 - 2.0 * k);
    }

    @Override
    public double getQuadraticCoefficient(MatrixIndex index, Pair<BinaryVariable<ElementCombinationTuple<T>>> variablePair) {
        final var combinationTupleA = variablePair.getA().getLabel();
        final var combinationTupleB = variablePair.getB().getLabel();
        final var combinationA = combinationTupleA.create();
        final var combinationB = combinationTupleB.create();
        final var relation = this.constraint.getRelation();
        final var elementA = relation.query(combinationA);
        final var elementB = relation.query(combinationB);
        return elementA.equals(elementB)
                ? 2.0 * this.lambda
                : 0.0;
    }

    @Override
    public double getOffsetValue() {
        return 0.0;
    }
}
