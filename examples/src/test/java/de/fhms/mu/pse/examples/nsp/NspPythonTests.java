package de.fhms.mu.pse.examples.nsp;

import de.fhms.mu.pse.ProblemSolvingPipeline;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static de.fhms.mu.pse.examples.TestUtils.buildPythonSolver;
import static de.fhms.mu.pse.examples.nsp.NspQuboTests.checkResult;

@Disabled
public class NspPythonTests {
    @Test
    public void testQuantumAnnealingSolver() throws IOException {
        final var shiftService = new ShiftService();
        final var nurseService = new NurseService();

        final var shifts = shiftService.getShiftsToSchedule(5);
        final var nurses = nurseService.getAvailableNurses(2);
        final var numberOfAssignmentsPerShift = 1;
        final var recoveryBetweenShifts = 1;
        final var problemConfiguration = new SimpleNsp(shifts, nurses, numberOfAssignmentsPerShift, recoveryBetweenShifts);

        final var workingPath = new File(System.getProperty("user.dir"));
        final var solverPath = new File(workingPath, "../solvers/quantum/quantum-annealing");
        final var outputPath = new File(workingPath, "../solvers/quantum/.output/nsp_quantum-annealing");
        final var solver = buildPythonSolver(solverPath, outputPath, Schedule.class, Assignment.class);

        final var result = ProblemSolvingPipeline.create()
                .build(problemConfiguration, Schedule.class, Assignment.class)
                .withName("Simple NSP")
                .solve(solver);
        checkResult(result);
    }

    @Test
    public void testQaoaSolver() throws IOException {
        final var shiftService = new ShiftService();
        final var nurseService = new NurseService();

        final var shifts = shiftService.getShiftsToSchedule(5);
        final var nurses = nurseService.getAvailableNurses(2);
        final var numberOfAssignmentsPerShift = 1;
        final var recoveryBetweenShifts = 1;
        final var problemConfiguration = new SimpleNsp(shifts, nurses, numberOfAssignmentsPerShift, recoveryBetweenShifts);

        final var workingPath = new File(System.getProperty("user.dir"));
        final var solverPath = new File(workingPath, "../solvers/quantum/qaoa");
        final var outputPath = new File(workingPath, "../solvers/quantum/.output/nsp_qaoa");
        final var solver = buildPythonSolver(solverPath, outputPath, Schedule.class, Assignment.class);

        final var result = ProblemSolvingPipeline.create()
                .build(problemConfiguration, Schedule.class, Assignment.class)
                .withName("Simple NSP")
                .solve(solver);
        checkResult(result);
    }

}
