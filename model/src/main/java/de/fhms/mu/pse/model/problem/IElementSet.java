package de.fhms.mu.pse.model.problem;

import de.fhms.mu.pse.model.domain.object.DomainObject;

import java.util.Set;

public interface IElementSet<E> {
    String getName();
    DomainObject<E> getDomainObject();
    Set<E> getElements();
}
