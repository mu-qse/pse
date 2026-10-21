package de.fhms.mu.pse.examples.nsp;

import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class NurseService {
    public Collection<Nurse> getAvailableNurses(int n) {
        return List.of(
                new Nurse("Alice"),
                new Nurse("Bob"),
                new Nurse("Clara"),
                new Nurse("Denise"),
                new Nurse("Emily"),
                new Nurse("Foxy"),
                new Nurse("Gandalf"),
                new Nurse("Han"),
                new Nurse("Ising"),
                new Nurse("Jane")
        ).subList(0, n);
    }
}
