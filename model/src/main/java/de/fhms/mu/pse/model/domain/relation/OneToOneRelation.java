package de.fhms.mu.pse.model.domain.relation;

import de.fhms.mu.pse.model.domain.object.DomainObject;

import java.util.function.BiFunction;
import java.util.function.Function;

public class OneToOneRelation<A, B> extends ManyToOneRelation<A, B> {
    public OneToOneRelation(DomainObject<A> a, DomainObject<B> b, Function<A, B> accessor, BiFunction<A, B, A> mutator) {
        super(String.format("%s - %s (1:1)", a.getName(), b.getName()), a, b, accessor, mutator);
    }

    public OneToOneRelation(String name, DomainObject<A> a, DomainObject<B> b, Function<A, B> accessor, BiFunction<A, B, A> mutator) {
        super(name, a, b, accessor, mutator);
    }
}
