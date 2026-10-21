package de.fhms.mu.pse.model.problem.constraint.relation;

import de.fhms.mu.pse.model.domain.relation.OneToManyRelation;
import de.fhms.mu.pse.model.problem.constraint.CombinatorialProblemConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class OneToManyRelationConstraint<S, T, A, B> extends CombinatorialProblemConstraint<S, T> {
    private final OneToManyRelation<A, B> relation;

    public OneToManyRelationConstraint(String name, OneToManyRelation<A, B> relation, double weight) {
        super(name, weight);

        this.relation = relation;
    }
}
