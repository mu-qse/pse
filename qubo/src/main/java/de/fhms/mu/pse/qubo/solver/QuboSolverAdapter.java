package de.fhms.mu.pse.qubo.solver;

import de.fhms.mu.pse.model.primitive.BitstringWithCost;
import de.fhms.mu.pse.model.problem.ElementCombinationTuple;
import de.fhms.mu.pse.model.problem.ICombinatorialProblem;
import de.fhms.mu.pse.model.qubo.*;
import de.fhms.mu.pse.model.qubo.interpreter.IQuboProblemInterpreter;
import de.fhms.mu.pse.model.solver.ProblemSolverAdapter;
import de.fhms.mu.pse.model.solver.SolverResult;
import de.fhms.mu.pse.qubo.interpreter.DefaultQuboProblemInterpreter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@RequiredArgsConstructor
public abstract class QuboSolverAdapter<S, T> extends ProblemSolverAdapter<S, T> implements IQuboSolver<ElementCombinationTuple<T>> {
    private final IQuboProblemInterpreter<T> problemInterpreter;

    public QuboSolverAdapter() {
        this(new DefaultQuboProblemInterpreter<>());
    }

    @Override
    protected Stream<S> invoke(ICombinatorialProblem<S, T> problem) {
        final var quboProblem = this.problemInterpreter.interpretProblem(problem);
        final var quboSolutions = this.solve(quboProblem);
        if (quboSolutions.isEmpty()) {
            throw new IllegalStateException("No solution found.");
        }

        return this.mapSolutions(problem, quboSolutions);
    }

    @Override
    public List<IQuboSolution<ElementCombinationTuple<T>>> solve(IQuboProblem<IQuboProblemEncoding<ElementCombinationTuple<T>>, ElementCombinationTuple<T>> quboProblem) {
        final var encoding = quboProblem.getEncoding();
        System.out.println("Encoding: " + encoding.getName());

        final var quboMatrix = quboProblem.buildQuboMatrix();
        System.out.printf("QUBO matrix: %d x %d (%d)%n", quboMatrix.length(), quboMatrix.length(), quboMatrix.entriesCount());
        System.out.println(quboMatrix);
        final var offsetValue = quboProblem.getOffsetValue();
        System.out.printf("Offset: %.3f %n", offsetValue);

        final var result = this.invoke(quboProblem, quboMatrix);

        System.out.printf("Found bitstrings: %d%n", result.getSolutionBitstringsCount());
        System.out.println(IntStream.range(0, result.getSolutionBitstringsCount())
                .mapToObj(i -> {
                    final var bitstringWithCost = result.getSolutionBitstring(i);
                    final var bitstring = bitstringWithCost.getBitstring();
                    final var solution = encoding.mapBitstringToSolution(quboProblem, bitstring);
                    return String.format("%d, %d: %s\n\t=> %s\n\t=> %.2f", i, bitstring.getNumericValue(), bitstring, solution, bitstringWithCost.getCost() + offsetValue);
                })
                .collect(Collectors.joining("\n")));

        final var solutionBitstrings = result.getSolutionBitstrings()
                .map(BitstringWithCost::getBitstring)
                .toList();
        return encoding.getSolutions(quboProblem, solutionBitstrings).toList();
    }

    private Stream<S> mapSolutions(ICombinatorialProblem<S, T> problem, List<IQuboSolution<ElementCombinationTuple<T>>> quboSolutions) {
        return quboSolutions.stream()
                .map(solution -> solution.getElements().toList())
                .map(problem::createSolution);
    }

    protected abstract SolverResult invoke(IQuboProblem<IQuboProblemEncoding<ElementCombinationTuple<T>>, ElementCombinationTuple<T>> quboProblem, QuboMatrix quboMatrix);
}
