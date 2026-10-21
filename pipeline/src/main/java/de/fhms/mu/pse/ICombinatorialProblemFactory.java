package de.fhms.mu.pse;

import de.fhms.mu.pse.model.problem.ICombinatorialProblem;

public interface ICombinatorialProblemFactory<I> {
    <S, T> ICombinatorialProblem<S, T> build(String name, I input);
    <S, T> ICombinatorialProblem<S, T> build(String name, I input, Class<S> solutionType, Class<T> combinationTupleType);
}
