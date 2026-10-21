package de.fhms.mu.pse.model.domain.object;

import java.util.function.Function;

public class Entity<C> extends DomainObject<C> {
    private final Function<Object[], C> createFunction;

    public Entity(Class<C> type, Function<Object[], C> createFunction) {
        super(type);

        this.createFunction = createFunction;
    }

    @Override
    public C create(Object... args) {
        return this.createFunction.apply(args);
    }
}
