package de.fhms.mu.pse.qubo.interpreter;

import de.fhms.mu.pse.model.problem.ElementCombinationTuple;
import de.fhms.mu.pse.model.problem.ICombinatorialProblem;
import de.fhms.mu.pse.model.problem.encoding.BinaryVariable;
import de.fhms.mu.pse.model.qubo.IQuboPenalty;
import de.fhms.mu.pse.model.qubo.IQuboProblem;
import de.fhms.mu.pse.model.qubo.IQuboProblemEncoding;
import de.fhms.mu.pse.model.qubo.interpreter.IQuboConstraintInterpreter;
import de.fhms.mu.pse.model.qubo.interpreter.IQuboProblemInterpreter;
import de.fhms.mu.pse.qubo.QuboProblem;
import de.fhms.mu.pse.qubo.QuboProblemEncoding;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class DefaultQuboProblemInterpreter<T> implements IQuboProblemInterpreter<T> {
    private final IQuboConstraintInterpreter<T> constraintInterpreter;

    public DefaultQuboProblemInterpreter() {
        this(new DefaultQuboConstraintInterpreter<>());
    }

    @Override
    public <S> IQuboProblem<IQuboProblemEncoding<ElementCombinationTuple<T>>, ElementCombinationTuple<T>> interpretProblem(ICombinatorialProblem<S, T> problem) {
        final var combinationTuples = problem.getPossibleCombinations();
        final var encoding = this.createQuboEncoding(combinationTuples);

        return new QuboProblem<>(encoding)
                .addAll(this.createQuboPenaltiesFromConstraints(problem));
    }

    private IQuboProblemEncoding<ElementCombinationTuple<T>> createQuboEncoding(List<ElementCombinationTuple<T>> combinationTuples) {
        final var quboProblemEncoding = new QuboProblemEncoding<ElementCombinationTuple<T>>();

        combinationTuples.stream()
                .map(combinationTuple -> new BinaryVariable<>(combinationTuple.toString(), combinationTuple))
                .forEach(quboProblemEncoding::add);

        return quboProblemEncoding;
    }

    private <S> List<IQuboPenalty<ElementCombinationTuple<T>>> createQuboPenaltiesFromConstraints(ICombinatorialProblem<S, T> problem) {
        final var constraints = problem.getConstraints();
        return constraints.stream()
                .map(constraint -> constraint.interpret(this.constraintInterpreter))
                .toList();
    }
}
