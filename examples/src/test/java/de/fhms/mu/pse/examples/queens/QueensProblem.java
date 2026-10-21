package de.fhms.mu.pse.examples.queens;

import de.fhms.mu.pse.annotations.problem.AggregateRootFactory;
import de.fhms.mu.pse.annotations.problem.ProblemConfiguration;
import de.fhms.mu.pse.annotations.problem.ProblemConstraint;
import de.fhms.mu.pse.annotations.problem.SelectableElements;
import de.fhms.mu.pse.model.domain.relation.ManyToOneRelation;
import de.fhms.mu.pse.model.problem.constraint.CombinatorialProblemConstraint;
import de.fhms.mu.pse.model.problem.constraint.relation.ExactMultiplicityConstraint;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@ProblemConfiguration(aggregateRoot = Chessboard.class, combinationTuple = Queen.class)
public class QueensProblem {
    private final int n;

    @AggregateRootFactory
    Chessboard createChessboard() {
        return new Chessboard(this.n);
    }

    @SelectableElements
    List<Queen> getQueens(Chessboard chessboard) {
        return chessboard.getAvailableQueens();
    }

    @SelectableElements
    List<Row> getAvailableRows(Chessboard chessboard) {
        return chessboard.getRowsAsList();
    }

    @ProblemConstraint
    CombinatorialProblemConstraint<?, ?> enforceOneQueenPerRow(ManyToOneRelation<Queen, Row> relation) {
        return new ExactMultiplicityConstraint<>(relation, 1);
    }

    @ProblemConstraint
    boolean enforceSafeQueenPlacement(Queen a, Queen b) {
        return a.isSafe(b);
    }
}
