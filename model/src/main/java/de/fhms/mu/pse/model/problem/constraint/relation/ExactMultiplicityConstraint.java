package de.fhms.mu.pse.model.problem.constraint.relation;

import de.fhms.mu.pse.model.domain.relation.ManyToOneRelation;
import de.fhms.mu.pse.model.problem.ElementCombinationTuple;
import de.fhms.mu.pse.model.problem.ICombinatorialProblem;
import de.fhms.mu.pse.model.qubo.IQuboPenalty;
import de.fhms.mu.pse.model.qubo.interpreter.IQuboConstraintInterpreter;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExactMultiplicityConstraint<S, T, B> extends ManyToOneRelationConstraint<S, T, T, B> {
    private final int multiplicity;

    public ExactMultiplicityConstraint(ManyToOneRelation<T, B> relation, int multiplicity) {
        this(String.format("Multiplicity for %s = %s", relation.getName(), multiplicity), relation, multiplicity, 1);
    }

    public ExactMultiplicityConstraint(String name, ManyToOneRelation<T, B> relation, int multiplicity) {
        this(name, relation, multiplicity, 1);
    }

    public ExactMultiplicityConstraint(String name, ManyToOneRelation<T, B> relation, int multiplicity, double weight) {
        super(name, relation, weight);

        this.multiplicity = multiplicity;
    }

    @Override
    public boolean isSatisfied(ICombinatorialProblem<S, T> problem, S solution) {
        final var relation = this.getRelation();
        final var elementsSet = problem.getSelectableElements(relation.getB()).orElseThrow();
        final var elements = problem.getSolutionDomainObjectRelation().query(solution);
        final var elementOccurrences = elementsSet.getElements().stream()
                .collect(Collectors.toMap(value -> value, value -> elements.stream()
                        .filter(element -> relation.query(element).equals(value))
                        .count()));
        return elementOccurrences.keySet().stream()
                .allMatch(element -> elementOccurrences.get(element) == this.multiplicity);
    }

    @Override
    public IQuboPenalty<ElementCombinationTuple<T>> interpret(IQuboConstraintInterpreter<T> interpreter) {
        return interpreter.interpretConstraint(this);
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
