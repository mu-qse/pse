package de.fhms.mu.pse.examples.sudoku;

import de.fhms.mu.pse.ProblemSolvingPipeline;
import de.fhms.mu.pse.solver.SimulatedAnnealingSolver;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static de.fhms.mu.pse.examples.TestUtils.buildSimulatedAnnealingSolver;

public class SudokuQuboTests {
    @Test
    void solve2x2() {
        final var n = 2;
        final var problemConfiguration = new SudokuProblem(n);

        final var solverParams = new SimulatedAnnealingSolver.Params(100000, 1000.0, 0.001,0.9999, 1304L);
        final var solver = buildSimulatedAnnealingSolver(solverParams, Sudoku.class, Cell.class);

        final var result = ProblemSolvingPipeline.create()
                .build(problemConfiguration, Sudoku.class, Cell.class)
                .withName(String.format("%dx%d Sudoku", n, n))
                .solve(solver);
        checkResult(result);
    }

    @Test
    @Disabled
    void solve3x3() {
        final var n = 3;
        final var problemConfiguration = new SudokuProblem(n);

        final var solverParams = new SimulatedAnnealingSolver.Params(1000000, 1000.0, 0.001,0.99995, 1304L);
        final var solver = buildSimulatedAnnealingSolver(solverParams, Sudoku.class, Cell.class);

        final var result = ProblemSolvingPipeline.create()
                .build(problemConfiguration, Sudoku.class, Cell.class)
                .withName(String.format("%dx%d Sudoku", n, n))
                .solve(solver);
        checkResult(result);
    }

    public static void checkResult(ProblemSolvingPipeline.Result<Sudoku, Cell> result) {
        final var problem = result.getProblem();

        System.out.println("--- Resulting solutions ---");
        result.getSolutions()
                .forEachOrdered(sudoku -> {
                    System.out.println(sudoku);
                    System.out.println("---");
                    problem.verify(sudoku);
                });
    }
}
