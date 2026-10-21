package de.fhms.mu.pse.model.domain.object;

import lombok.Data;

@Data
public abstract class AbstractObjectFactory<T> implements IDomainObjectFactory<T> {
    private final Class<T> type;
}
