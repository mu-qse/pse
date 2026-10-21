package de.fhms.mu.pse.model.qubo.interpreter;

import de.fhms.mu.pse.model.problem.ElementCombinationTuple;
import de.fhms.mu.pse.model.problem.ICombinatorialProblem;
import de.fhms.mu.pse.model.qubo.IQuboProblem;
import de.fhms.mu.pse.model.qubo.IQuboProblemEncoding;

public interface IQuboProblemInterpreter<T> {
    <S> IQuboProblem<IQuboProblemEncoding<ElementCombinationTuple<T>>, ElementCombinationTuple<T>> interpretProblem(ICombinatorialProblem<S, T> problem);
}
