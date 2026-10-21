package de.fhms.mu.pse.model.qubo;

import de.fhms.mu.pse.model.primitive.Bitstring;
import de.fhms.mu.pse.model.problem.encoding.EncodingVariable;
import lombok.RequiredArgsConstructor;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class QuboSolution<T> implements IQuboSolution<T> {
    private final IQuboProblem<IQuboProblemEncoding<T>, T> quboProblem;
    private final Bitstring bitstring;

    @Override
    public Stream<T> getElements() {
        final var encoding = this.quboProblem.getEncoding();
        return encoding.getActivatedVariables(bitstring)
                .map(EncodingVariable::getLabel);
    }

    @Override
    public Bitstring getBitstring() {
        return this.bitstring;
    }

    @Override
    public String toString() {
        return String.format("[%s]", this.getElements()
                .map(Object::toString)
                .collect(Collectors.joining(", ")));
    }
}
