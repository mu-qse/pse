package de.fhms.mu.pse.model.problem;

import de.fhms.mu.pse.model.domain.object.DomainObject;
import lombok.Data;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class ElementCombinationTuple<T> {
    private final Map<DomainObject<?>, Object> values = new HashMap<>();
    private final DomainObject<T> domainObject;

    public ElementCombinationTuple(ElementCombinationTuple<T> other) {
        this.domainObject = other.getDomainObject();
        this.fillValues(other);
    }

    public ElementCombinationTuple(DomainObject<T> domainObject) {
        this.domainObject = domainObject;
    }

    public void fillValues(ElementCombinationTuple<T> other) {
        this.values.putAll(other.getValues());
    }

    public <V> void setValue(DomainObject<?> type, V value) {
        this.values.put(type, value);
    }

    public <V> Optional<V> getValue(DomainObject<V> type) {
        if (!this.values.containsKey(type)) {
            return Optional.empty();
        }

        return Optional.of((V) this.values.get(type));
    }

    public T create() {
        final var relations = Stream.of(
                this.domainObject.getManyToOneRelations(),
                this.domainObject.getOneToOneRelations()
        ).flatMap(Collection::stream).toList();

        var element = this.getValue(this.domainObject).orElseGet(() -> {
            try {
                return this.domainObject.getType().getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });

        for (final var relation : relations) {
            final var value = this.getValue(relation.getB());
            if (value.isEmpty()) {
                continue;
            }

            element = relation.update(element, value.get());
        }

        return element;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ElementCombinationTuple<?> that)) {
            return false;
        }
        return Objects.equals(this.domainObject, that.domainObject);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.domainObject);
    }

    @Override
    public String toString() {
        if (this.values.isEmpty()) {
            return this.domainObject.getName();
        }

        return String.format("{%s}", this.values.values().stream()
                .map(Object::toString)
                .collect(Collectors.joining(", ")));
    }
}
