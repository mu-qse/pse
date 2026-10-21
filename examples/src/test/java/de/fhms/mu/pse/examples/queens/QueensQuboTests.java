package de.fhms.mu.pse.examples.queens;

import de.fhms.mu.pse.ProblemSolvingPipeline;
import de.fhms.mu.pse.solver.SimulatedAnnealingSolver;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static de.fhms.mu.pse.examples.TestUtils.buildSimulatedAnnealingSolver;

public class QueensQuboTests {
    @Test
    void testFeasibility() {
        final var n = 4;
        final var problemConfiguration = new QueensProblem(n);

        final var solverParams = new SimulatedAnnealingSolver.Params(200000, 1000.0, 0.001, 0.99995, 1304L);
        final var solver = buildSimulatedAnnealingSolver(solverParams, Chessboard.class, Queen.class);

        final var result = ProblemSolvingPipeline.create()
                .build(problemConfiguration, Chessboard.class, Queen.class)
                .withName(String.format("%d-queens", n))
                .solve(solver);
        checkResult(result);
    }

    @ParameterizedTest
    @ValueSource(ints  = { 4, 5, 6, 7, 8 })
    void solveSmallN(int n) {
        final var problemConfiguration = new QueensProblem(n);

        final var solverParams = new SimulatedAnnealingSolver.Params(200000, 1000.0, 0.001, 0.99995, 1304L);
        final var solver = buildSimulatedAnnealingSolver(solverParams, Chessboard.class, Queen.class);

        final var result = ProblemSolvingPipeline.create()
                .build(problemConfiguration, Chessboard.class, Queen.class)
                .withName(String.format("%d-queens", n))
                .solve(solver);
        checkResult(result);
    }

    @ParameterizedTest
    @ValueSource(ints  = { 9, 10, 11, 12, 13, 14 })
    @Disabled
    void solveLargeN(int n) {
        final var problemConfiguration = new QueensProblem(n);

        final var solverParams = new SimulatedAnnealingSolver.Params(1000000, 1000.0, 0.001, 0.99999, 1304L);
        final var solver = buildSimulatedAnnealingSolver(solverParams, Chessboard.class, Queen.class);

        final var result = ProblemSolvingPipeline.create()
                .build(problemConfiguration, Chessboard.class, Queen.class)
                .withName(String.format("%d-queens", n))
                .solve(solver);
        checkResult(result);
    }

    public static void checkResult(ProblemSolvingPipeline.Result<Chessboard, Queen> result) {
        final var problem = result.getProblem();

        System.out.println("--- Resulting solutions ---");
        result.getSolutions()
                .forEachOrdered(chessboard -> {
                    System.out.println(chessboard);
                    System.out.println("---");
                    problem.verify(chessboard);
                });
    }
}
