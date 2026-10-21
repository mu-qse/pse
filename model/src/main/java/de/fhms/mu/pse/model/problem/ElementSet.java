package de.fhms.mu.pse.model.problem;

import de.fhms.mu.pse.model.domain.object.DomainObject;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor
public class ElementSet<E> implements IElementSet<E> {
    private final String name;
    private final DomainObject<E> domainObject;
    private final Set<E> elements;

    public ElementSet(DomainObject<E> domainObject, Collection<E> elements) {
        this(domainObject, new LinkedHashSet<>(elements));
    }

    public ElementSet(String name, DomainObject<E> domainObject, Collection<E> elements) {
        this(name, domainObject, new LinkedHashSet<>(elements));
    }

    public ElementSet(DomainObject<E> domainObject, Set<E> elements) {
        this(domainObject.getName(), domainObject, elements);
    }

    public void set(List<E> instances) {
        this.elements.clear();
        this.elements.addAll(instances);
    }

    @Override
    public String toString() {
        return String.format("%s: [%s]", this.name, this.elements.stream()
                .map(Objects::toString)
                .collect(Collectors.joining(", ")));
    }
}
