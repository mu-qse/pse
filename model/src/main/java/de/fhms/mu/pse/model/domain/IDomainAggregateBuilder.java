package de.fhms.mu.pse.model.domain;

public interface IDomainAggregateBuilder<T, I> {
    DomainAggregate<T> build(I input);
}
