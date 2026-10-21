package de.fhms.mu.pse.model.domain.relation;

import de.fhms.mu.pse.model.domain.object.DomainObject;

import java.util.function.BiFunction;
import java.util.function.Function;

public class ManyToOneRelation<A, B> extends DomainObjectRelation<A, B> {
    private final Function<A, B> accessor;
    private final BiFunction<A, B, A> mutator;

    public ManyToOneRelation(DomainObject<A> a, DomainObject<B> b, Function<A, B> accessor, BiFunction<A, B, A> mutator) {
        super(String.format("%s - %s (n:1)", a.getName(), b.getName()), a, b);

        this.accessor = accessor;
        this.mutator = mutator;
    }

    public ManyToOneRelation(String name, DomainObject<A> a, DomainObject<B> b, Function<A, B> accessor, BiFunction<A, B, A> mutator) {
        super(name, a, b);

        this.accessor = accessor;
        this.mutator = mutator;
    }

    public B query(A a) {
        return this.accessor.apply(a);
    }

    public A update(A a, B b) {
        return this.mutator.apply(a, b);
    }
}
