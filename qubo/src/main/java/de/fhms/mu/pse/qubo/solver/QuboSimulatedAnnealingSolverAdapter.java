package de.fhms.mu.pse.qubo.solver;

import de.fhms.mu.pse.model.problem.ElementCombinationTuple;
import de.fhms.mu.pse.model.qubo.IQuboProblem;
import de.fhms.mu.pse.model.qubo.IQuboProblemEncoding;
import de.fhms.mu.pse.model.qubo.QuboMatrix;
import de.fhms.mu.pse.model.solver.SolverResult;
import de.fhms.mu.pse.solver.SimulatedAnnealingSolver;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class QuboSimulatedAnnealingSolverAdapter<S, T> extends QuboSolverAdapter<S, T> {
    private final SimulatedAnnealingSolver solver;

    @Override
    protected SolverResult invoke(IQuboProblem<IQuboProblemEncoding<ElementCombinationTuple<T>>, ElementCombinationTuple<T>> quboProblem, QuboMatrix quboMatrix) {
        var n = quboMatrix.length();
        return this.solver.invoke(n, quboMatrix::apply);
    }
}
