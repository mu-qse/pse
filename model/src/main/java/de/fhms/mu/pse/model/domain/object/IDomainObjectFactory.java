package de.fhms.mu.pse.model.domain.object;

public interface IDomainObjectFactory<C> {
    Class<C> getType();
    C create(Object... args);
}
