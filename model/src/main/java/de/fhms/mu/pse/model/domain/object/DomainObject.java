package de.fhms.mu.pse.model.domain.object;

import de.fhms.mu.pse.model.domain.relation.DomainObjectRelation;
import de.fhms.mu.pse.model.domain.relation.ManyToOneRelation;
import de.fhms.mu.pse.model.domain.relation.OneToManyRelation;
import de.fhms.mu.pse.model.domain.relation.OneToOneRelation;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.*;

@Data
@RequiredArgsConstructor
public abstract class DomainObject<C> {
    private final Class<C> type;
    private final Set<DomainObjectRelation<C, ?>> relations = new HashSet<>();

    public String getName() {
        return this.type.getSimpleName();
    }

    public <B>Optional<DomainObjectRelation<C, B>> getRelation(DomainObject<B> b) {
        return this.relations.stream()
                .filter(relation -> relation.getB().equals(b))
                .map(relation -> (DomainObjectRelation<C, B>) relation)
                .findFirst();
    }

    public <B>Optional<DomainObjectRelation<C, B>> getRelation(Class<B> b) {
        return this.relations.stream()
                .filter(relation -> relation.getB().getType().equals(b))
                .map(relation -> (DomainObjectRelation<C, B>) relation)
                .findFirst();
    }

    public List<OneToManyRelation<C, Object>> getOneToManyRelations() {
        return this.relations.stream()
                .filter(relation -> relation instanceof OneToManyRelation<?, ?>)
                .map(relation -> (OneToManyRelation<C, Object>) relation)
                .toList();
    }

    public List<ManyToOneRelation<C, Object>> getManyToOneRelations() {
        return this.relations.stream()
                .filter(relation -> relation instanceof ManyToOneRelation<?, ?>)
                .filter(relation -> !(relation instanceof OneToOneRelation<?, ?>))
                .map(relation -> (ManyToOneRelation<C, Object>) relation)
                .toList();
    }

    public List<OneToOneRelation<C, Object>> getOneToOneRelations() {
        return this.relations.stream()
                .filter(relation -> relation instanceof OneToOneRelation<?, ?>)
                .map(relation -> (OneToOneRelation<C, Object>) relation)
                .toList();
    }

    public <B>Optional<OneToManyRelation<C, B>> getOneToManyRelation(DomainObject<B> b) {
        return this.relations.stream()
                .filter(relation -> relation instanceof OneToManyRelation<?, ?>)
                .filter(relation -> relation.getB().equals(b))
                .map(relation -> (OneToManyRelation<C, B>) relation)
                .findFirst();
    }

    public <B>Optional<OneToManyRelation<C, B>> getOneToManyRelation(Class<B> b) {
        return this.relations.stream()
                .filter(relation -> relation instanceof OneToManyRelation<?, ?>)
                .filter(relation -> relation.getB().getType().equals(b))
                .map(relation -> (OneToManyRelation<C, B>) relation)
                .findFirst();
    }

    public <B>Optional<ManyToOneRelation<C, B>> getManyToOneRelation(DomainObject<B> b) {
        return this.relations.stream()
                .filter(relation -> relation instanceof ManyToOneRelation<?, ?> && relation.getB().equals(b))
                .map(relation -> (ManyToOneRelation<C, B>) relation)
                .findFirst();
    }

    public <B>Optional<ManyToOneRelation<C, B>> getManyToOneRelation(Class<B> b) {
        return this.relations.stream()
                .filter(relation -> relation instanceof ManyToOneRelation<?, ?>)
                .filter(relation -> relation.getB().getType().equals(b))
                .map(relation -> (ManyToOneRelation<C, B>) relation)
                .findFirst();
    }

    public DomainObject<C> add(DomainObjectRelation<C, ?> relation) {
        this.relations.add(relation);
        return this;
    }

    public abstract C create(Object... args);

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DomainObject<?> that)) {
            return false;
        }
        return Objects.equals(this.type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.type);
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
