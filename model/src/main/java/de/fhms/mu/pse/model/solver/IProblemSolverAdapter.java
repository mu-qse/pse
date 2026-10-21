package de.fhms.mu.pse.model.solver;

import de.fhms.mu.pse.model.problem.ICombinatorialProblem;

import java.util.stream.Stream;

public interface IProblemSolverAdapter<S, T> {
    Stream<S> solve(ICombinatorialProblem<S, T> problem);
}
