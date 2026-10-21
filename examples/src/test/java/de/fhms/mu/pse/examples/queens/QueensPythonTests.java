package de.fhms.mu.pse.examples.queens;

import de.fhms.mu.pse.ProblemSolvingPipeline;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static de.fhms.mu.pse.examples.TestUtils.buildPythonSolver;
import static de.fhms.mu.pse.examples.queens.QueensQuboTests.checkResult;

@Disabled
public class QueensPythonTests {
    @Test
    public void testQuantumAnnealingSolver() throws IOException {
        final var n = 4;
        final var problemConfiguration = new QueensProblem(n);

        final var workingPath = new File(System.getProperty("user.dir"));
        final var solverPath = new File(workingPath, "../solvers/quantum/quantum-annealing");
        final var outputPath = new File(workingPath, "../solvers/quantum/.output/queens_quantum-annealing");
        final var solver = buildPythonSolver(solverPath, outputPath, Chessboard.class, Queen.class);

        final var result = ProblemSolvingPipeline.create()
                .build(problemConfiguration, Chessboard.class, Queen.class)
                .withName(String.format("%d-queens", n))
                .solve(solver);
        checkResult(result);
    }

    @Test
    public void testQaoaSolver() throws IOException {
        final var n = 4;
        final var problemConfiguration = new QueensProblem(n);

        final var workingPath = new File(System.getProperty("user.dir"));
        final var solverPath = new File(workingPath, "../solvers/quantum/qaoa");
        final var outputPath = new File(workingPath, "../solvers/quantum/.output/queens_qaoa");
        final var solver = buildPythonSolver(solverPath, outputPath, Chessboard.class, Queen.class);

        final var result = ProblemSolvingPipeline.create()
                .build(problemConfiguration, Chessboard.class, Queen.class)
                .withName(String.format("%d-queens", n))
                .solve(solver);
        checkResult(result);
    }

}
