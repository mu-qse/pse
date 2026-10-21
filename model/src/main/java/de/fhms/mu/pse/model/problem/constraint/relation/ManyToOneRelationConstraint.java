package de.fhms.mu.pse.model.problem.constraint.relation;

import de.fhms.mu.pse.model.domain.relation.ManyToOneRelation;
import de.fhms.mu.pse.model.problem.constraint.CombinatorialProblemConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class ManyToOneRelationConstraint<S, T, A, B> extends CombinatorialProblemConstraint<S, T> {
    private final ManyToOneRelation<A, B> relation;

    public ManyToOneRelationConstraint(String name, ManyToOneRelation<A, B> relation, double weight) {
        super(name, weight);

        this.relation = relation;
    }
}
