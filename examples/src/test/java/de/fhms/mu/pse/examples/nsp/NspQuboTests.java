package de.fhms.mu.pse.examples.nsp;

import de.fhms.mu.pse.ProblemSolvingPipeline;
import de.fhms.mu.pse.solver.SimulatedAnnealingSolver;
import org.junit.jupiter.api.Test;

import static de.fhms.mu.pse.examples.TestUtils.buildSimulatedAnnealingSolver;

public class NspQuboTests {
    @Test
    void testFeasibility() {
        final var shiftService = new ShiftService();
        final var nurseService = new NurseService();

        final var shifts = shiftService.getShiftsToSchedule(5);
        final var nurses = nurseService.getAvailableNurses(2);
        final var numberOfAssignmentsPerShift = 1;
        final var recoveryBetweenShifts = 1;
        final var problemConfiguration = new SimpleNsp(shifts, nurses, numberOfAssignmentsPerShift, recoveryBetweenShifts);

        final var solverParams = new SimulatedAnnealingSolver.Params(200000, 1000.0, 0.001, 0.99995, 1304L);
        final var solver = buildSimulatedAnnealingSolver(solverParams, Schedule.class, Assignment.class);

        final var result = ProblemSolvingPipeline.create()
                .build(problemConfiguration, Schedule.class, Assignment.class)
                .withName("A simple NSP")
                .solve(solver);
        checkResult(result);
    }

    public static void checkResult(ProblemSolvingPipeline.Result<Schedule, Assignment> result) {
        final var problem = result.getProblem();

        System.out.println("--- Resulting schedules ---");
        result.getSolutions()
                .forEachOrdered(schedule -> {
                    System.out.println(schedule);
                    problem.verify(schedule);
                    System.out.println("---");
                });
    }
}
