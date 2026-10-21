package de.fhms.mu.pse.model.problem;

import de.fhms.mu.pse.model.domain.DomainAggregate;
import de.fhms.mu.pse.model.domain.object.DomainObject;
import de.fhms.mu.pse.model.domain.object.Entity;
import de.fhms.mu.pse.model.domain.relation.DomainObjectRelation;
import de.fhms.mu.pse.model.domain.relation.OneToManyRelation;
import de.fhms.mu.pse.model.problem.constraint.CombinatorialProblemConstraint;
import de.fhms.mu.pse.model.problem.constraint.ConstraintViolationException;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class DefaultCombinatorialProblem<S, T> implements ICombinatorialProblem<S, T> {
    private final String name;
    private final DomainAggregate<S> domainAggregate;
    private final DomainObject<T> combinationTupleDomainObject;

    private final List<IElementSet<?>> selectableElements = new ArrayList<>();
    private final List<CombinatorialProblemConstraint<S, T>> constraints = new ArrayList<>();

    @Override
    public Entity<S> getSolutionDomainObject() {
        return this.domainAggregate.getRoot();
    }

    @Override
    public OneToManyRelation<S, T> getSolutionDomainObjectRelation() {
        final var solutionDomainObject = this.getSolutionDomainObject();
        final var combinationTupleDomainObject = this.getCombinationTupleDomainObject();
        return solutionDomainObject.getOneToManyRelation(combinationTupleDomainObject)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Could not find one-to-many relation: %s -> %s", solutionDomainObject, combinationTupleDomainObject)));
    }

    @Override
    public List<IElementSet<?>> getSelectableElements() {
        return Collections.unmodifiableList(this.selectableElements);
    }

    @Override
    public <E> Optional<IElementSet<E>> getSelectableElements(String name) {
        return this.selectableElements.stream()
                .filter(elementSet -> elementSet.getName().equals(name))
                .map(elementSet -> (IElementSet<E>) elementSet)
                .findFirst();
    }

    @Override
    public <E> Optional<IElementSet<E>> getSelectableElements(DomainObject<E> type) {
        return this.selectableElements.stream()
                .filter(elementSet -> elementSet.getDomainObject().equals(type))
                .map(elementSet -> (IElementSet<E>) elementSet)
                .findFirst();
    }

    @Override
    public <E> Optional<IElementSet<E>> getSelectableElements(Class<E> type) {
        return this.selectableElements.stream()
                .filter(elementSet -> elementSet.getDomainObject().getType().equals(type))
                .map(elementSet -> (IElementSet<E>) elementSet)
                .findFirst();
    }

    @Override
    public ICombinatorialProblem<S, T> addSelectableElements(IElementSet<?> elementSet) {
        this.selectableElements.add(elementSet);
        return this;
    }

    @Override
    public ICombinatorialProblem<S, T> addConstraint(CombinatorialProblemConstraint<S, T> constraint) {
        this.constraints.add(constraint);
        return this;
    }

    @Override
    public List<ElementCombinationTuple<T>> getPossibleCombinations() {
        return this.createCombinationTuplesFromRelations();
    }

    @Override
    public S createSolution(List<ElementCombinationTuple<T>> combinationTuples) {
        final var solutionDomainObject = this.getSolutionDomainObject();
        final var solution = solutionDomainObject.create();
        final var solutionDomainObjectRelation = this.getSolutionDomainObjectRelation();
        final var elements = combinationTuples.stream()
                .map(ElementCombinationTuple::create)
                .toList();
        solutionDomainObjectRelation.update(solution, elements);
        return solution;
    }

    @Override
    public boolean isFeasible(S solution) {
        return this.constraints.stream()
                .allMatch(constraint -> constraint.isSatisfied(this, solution));
    }

    @Override
    public void verify(S solution) throws ConstraintViolationException {
        for (final var constraint : this.constraints) {
            if (!constraint.isSatisfied(this, solution)) {
                throw new ConstraintViolationException(constraint);
            }
        }
    }

    @Override
    public String toString() {
        return String.join("\n",
                String.format("Name: %s", this.getName()),
                String.format("%s", this.getDomainAggregate()),
                String.format("Solution aggregate: %s", this.getSolutionDomainObject()),
                String.format("Combination tuple: %s", this.getCombinationTupleDomainObject()),
                !this.selectableElements.isEmpty() ? String.format("Selectable elements: \n%s", this.selectableElements.stream()
                        .map(variable -> String.format("- %s", variable))
                        .collect(Collectors.joining("\n"))) : "Selectable elements: None",
                !this.constraints.isEmpty() ? String.format("Constraints: \n%s", this.constraints.stream()
                        .map(constraint -> String.format("- %s (Weight: %.3f)", constraint.getName(), constraint.getWeight()))
                        .collect(Collectors.joining("\n"))) : "Constraints: None"
        );
    }

    private List<ElementCombinationTuple<T>> createCombinationTuplesFromRelations() {
        final var relations = Stream.of(
                this.combinationTupleDomainObject.getManyToOneRelations(),
                this.combinationTupleDomainObject.getOneToOneRelations()
        ).flatMap(Collection::stream).toList();

        var result = new ArrayList<>(this.createDefaultCombinationTuples());

        for (final var relation : relations) {
            final var variations = this.createCombinationTuplesFromRelation(relation);
            final var newResult = new ArrayList<ElementCombinationTuple<T>>();
            for (final var base : result) {
                for (final var partial : variations) {
                    final var copy = new ElementCombinationTuple<>(base);
                    copy.fillValues(partial);
                    newResult.add(copy);
                }
            }

            result = newResult;
        }

        return result;
    }

    private List<ElementCombinationTuple<T>> createDefaultCombinationTuples() {
        final var combinationTupleDomainObject = this.getCombinationTupleDomainObject();
        final var elementSet = this.getSelectableElements(combinationTupleDomainObject);
        return elementSet.map(s -> s.getElements().stream()
                .map(element -> {
                    final var tuple = new ElementCombinationTuple<>(combinationTupleDomainObject);
                    tuple.setValue(combinationTupleDomainObject, element);
                    return tuple;
                })
                .toList()
        ).orElse(List.of(new ElementCombinationTuple<>(combinationTupleDomainObject)));
    }

    private <B> List<ElementCombinationTuple<T>> createCombinationTuplesFromRelation(DomainObjectRelation<T, B> relation) {
        final var combinationTupleDomainObject = this.getCombinationTupleDomainObject();
        final var elementSet = this.getSelectableElements(relation.getB());
        return elementSet.map(set -> set.getElements().stream()
                .map(b -> {
                    final var tuple = new ElementCombinationTuple<>(combinationTupleDomainObject);
                    tuple.setValue(relation.getB(), b);
                    return tuple;
                })
                .toList()
        ).orElse(List.of());
    }
}
