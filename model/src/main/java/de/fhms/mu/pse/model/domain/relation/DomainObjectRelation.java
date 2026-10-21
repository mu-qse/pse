package de.fhms.mu.pse.model.domain.relation;

import de.fhms.mu.pse.model.domain.object.DomainObject;
import lombok.Data;

@Data
public abstract class DomainObjectRelation<A, B> {
    private final String name;
    private final DomainObject<A> a;
    private final DomainObject<B> b;

    @Override
    public String toString() {
        return this.getName();
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof DomainObjectRelation<?, ?> that)) {
            return false;
        }

        return this.name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
}
