package de.fhms.mu.pse.examples.nsp;

import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@RequiredArgsConstructor
public class ShiftService {
    public Collection<Shift> getShiftsToSchedule(int n) {
        return IntStream.rangeClosed(1, n)
                .mapToObj(Shift::new)
                .collect(Collectors.toList());
    }
}
