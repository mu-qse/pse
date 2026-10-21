package de.fhms.mu.pse.model.domain;

import de.fhms.mu.pse.model.domain.object.DomainObject;
import de.fhms.mu.pse.model.domain.object.Entity;
import de.fhms.mu.pse.model.domain.relation.DomainObjectRelation;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class DomainAggregate<R>  {
    private final Entity<R> root;
    private final Map<Class<?>, DomainObject<?>> objects = new HashMap<>();

    public DomainAggregate(Entity<R> root) {
        this.root = root;
        this.add(root);
    }

    public DomainAggregate<R> add(DomainObject<?> type) {
        if (this.objects.containsKey(type.getType())) {
            //throw new IllegalArgumentException(type + " already exists");
            return this;
        }

        this.objects.put(type.getType(), type);
        return this;
    }

    public <A> DomainAggregate<R> add(DomainObjectRelation<A, ?> relation) {
        final var owningType = relation.getA();
        if (!this.objects.containsKey(owningType.getType())) {
            throw new IllegalArgumentException("Object does not exist in aggregate: "  + owningType.getType());
        }

        owningType.add(relation);

        final var b = relation.getB();
        if (!this.objects.containsKey(b.getType())) {
            this.add(b);
        }

        return this;
    }

    public List<DomainObject<?>> getObjects() {
        return new ArrayList<>(this.objects.values());
    }

    public <T> Optional<DomainObject<T>> getObjectByType(Class<T> type) {
        return Optional.ofNullable((DomainObject<T>) this.objects.get(type));
    }

    public List<DomainObjectRelation<?, ?>> getRelations() {
        return this.objects.values().stream()
                .map(DomainObject::getRelations)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        final var objects = this.objects.values();
        final var relations = this.getRelations();
        return String.join("\n",
                !this.objects.isEmpty() ? String.format("Domain objects: \n%s", objects.stream()
                        .map(type -> String.format("- %s", type.getName()))
                        .collect(Collectors.joining("\n"))) : "Domain objects: None",
                !relations.isEmpty() ? String.format("Relations: \n%s", relations.stream()
                        .map(relation -> String.format("- %s", relation))
                        .collect(Collectors.joining("\n"))) : "Relations: None"
        );
    }
}
