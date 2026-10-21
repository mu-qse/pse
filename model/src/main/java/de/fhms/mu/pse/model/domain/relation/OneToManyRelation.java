package de.fhms.mu.pse.model.domain.relation;

import de.fhms.mu.pse.model.domain.object.DomainObject;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;

public class OneToManyRelation<A, B> extends DomainObjectRelation<A, B> {
    private final Function<A, Collection<B>> accessor;
    private final BiFunction<A, Collection<B>, A> mutator;

    public OneToManyRelation(DomainObject<A> a, DomainObject<B> b, Function<A, Collection<B>> accessor, BiFunction<A, Collection<B>, A> mutator) {
        super(String.format("%s - %s (1:n)", a.getName(), b.getName()), a, b);

        this.accessor = accessor;
        this.mutator = mutator;
    }

    public OneToManyRelation(String name, DomainObject<A> a, DomainObject<B> b, Function<A, Collection<B>> accessor, BiFunction<A, Collection<B>, A> mutator) {
        super(name, a, b);

        this.accessor = accessor;
        this.mutator = mutator;
    }

    public Collection<B> query(A a) {
        return this.accessor.apply(a);
    }

    public A update(A a, Collection<B> b) {
        return this.mutator.apply(a, b);
    }
}
