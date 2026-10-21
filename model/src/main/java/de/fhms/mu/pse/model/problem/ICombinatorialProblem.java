package de.fhms.mu.pse.model.problem;

import de.fhms.mu.pse.model.domain.DomainAggregate;
import de.fhms.mu.pse.model.domain.object.DomainObject;
import de.fhms.mu.pse.model.domain.object.Entity;
import de.fhms.mu.pse.model.domain.relation.OneToManyRelation;
import de.fhms.mu.pse.model.problem.constraint.CombinatorialProblemConstraint;
import de.fhms.mu.pse.model.problem.constraint.ConstraintViolationException;

import java.util.List;
import java.util.Optional;

public interface ICombinatorialProblem<S, T> {
    String getName();
    DomainAggregate<S> getDomainAggregate();
    Entity<S> getSolutionDomainObject();
    DomainObject<T> getCombinationTupleDomainObject();
    OneToManyRelation<S, T> getSolutionDomainObjectRelation();

    List<IElementSet<?>> getSelectableElements();
    <E> Optional<IElementSet<E>> getSelectableElements(String name);
    <E> Optional<IElementSet<E>> getSelectableElements(Class<E> type);
    <E> Optional<IElementSet<E>> getSelectableElements(DomainObject<E> type);
    List<CombinatorialProblemConstraint<S, T>> getConstraints();

    ICombinatorialProblem<S, T> addSelectableElements(IElementSet<?> elementSet);
    ICombinatorialProblem<S, T> addConstraint(CombinatorialProblemConstraint<S, T> constraint);

    List<ElementCombinationTuple<T>> getPossibleCombinations();

    S createSolution(List<ElementCombinationTuple<T>> combinationTuples);

    boolean isFeasible(S solution);
    void verify(S solution) throws ConstraintViolationException;
}
