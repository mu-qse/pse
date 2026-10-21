package de.fhms.mu.pse.examples;

import de.fhms.mu.pse.python.PythonEnvironment;
import de.fhms.mu.pse.qubo.solver.QuboBruteForceSolverAdapter;
import de.fhms.mu.pse.qubo.solver.QuboPythonProblemSolverAdapter;
import de.fhms.mu.pse.qubo.solver.QuboSimulatedAnnealingSolverAdapter;
import de.fhms.mu.pse.qubo.solver.QuboSolverAdapter;
import de.fhms.mu.pse.solver.MultiThreadedBruteForceSolver;
import de.fhms.mu.pse.solver.SimulatedAnnealingSolver;

import java.io.File;
import java.io.IOException;

public class TestUtils {
    public static <S, T> QuboSolverAdapter<S, T> buildBruteForceSolver() {
        final var solver = new MultiThreadedBruteForceSolver();
        return new QuboBruteForceSolverAdapter<>(solver);
    }

    public static <S, T> QuboSolverAdapter<S, T> buildSimulatedAnnealingSolver(SimulatedAnnealingSolver.Params solverParams, Class<S> solutionType, Class<T> combinationTupleType) {
        final var solver = new SimulatedAnnealingSolver(solverParams);
        return new QuboSimulatedAnnealingSolverAdapter<>(solver);
    }

    public static <S, T> QuboSolverAdapter<S, T> buildPythonSolver(File solverPath, File outputPath, Class<S> solutionType, Class<T> combinationTupleType) throws IOException {
        final var pythonEnvironment = new PythonEnvironment(solverPath);
        pythonEnvironment.setup();

        final var scriptPath = new File(solverPath, "solver.py");
        return new QuboPythonProblemSolverAdapter<>(pythonEnvironment, scriptPath, outputPath);
    }
}
