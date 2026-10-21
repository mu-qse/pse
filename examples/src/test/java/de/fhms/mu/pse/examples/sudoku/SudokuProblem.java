package de.fhms.mu.pse.examples.sudoku;

import de.fhms.mu.pse.annotations.problem.AggregateRootFactory;
import de.fhms.mu.pse.annotations.problem.ProblemConfiguration;
import de.fhms.mu.pse.annotations.problem.ProblemConstraint;
import de.fhms.mu.pse.annotations.problem.SelectableElements;
import de.fhms.mu.pse.model.primitive.Pair;
import de.fhms.mu.pse.model.primitive.matrix.MatrixIndex;
import de.fhms.mu.pse.model.problem.ElementCombinationTuple;
import de.fhms.mu.pse.model.problem.ICombinatorialProblem;
import de.fhms.mu.pse.model.problem.constraint.CombinatorialProblemConstraint;
import de.fhms.mu.pse.model.problem.encoding.BinaryVariable;
import de.fhms.mu.pse.model.qubo.IQuboPenalty;
import de.fhms.mu.pse.model.qubo.interpreter.IQuboConstraintInterpreter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
@ProblemConfiguration(aggregateRoot = Sudoku.class, combinationTuple = Cell.class)
public class SudokuProblem {
    private final int n;

    @AggregateRootFactory
    Sudoku createSudoku() {
        return new Sudoku(this.n)
                .set(0, 2, 1)
                .set(1, 1, 3)
                .set(2, 2, 2)
                .set(3, 1, 1)
                ;
    }

    @SelectableElements
    Set<Cell> getCells(Sudoku sudoku) {
        return sudoku.getCells();
    }

    @SelectableElements
    Set<Integer> getAvailableNumbers(Sudoku sudoku) {
        return sudoku.getAvailableNumbers();
    }

    @ProblemConstraint(weight = 1)
    CombinatorialProblemConstraint<Sudoku, Cell> enforceOneCellPerNumber() {
        return new CombinatorialProblemConstraint<>("sudoku", 1.0) {
            @Override
            public boolean isSatisfied(ICombinatorialProblem<Sudoku, Cell> problem, Sudoku solution) {
                return true;
            }

            @Override
            public IQuboPenalty<ElementCombinationTuple<Cell>> interpret(IQuboConstraintInterpreter<Cell> interpreter) {
                final var lambda = this.getWeight();
                return new IQuboPenalty<>() {
                    @Override
                    public double getLinearCoefficient(int index, BinaryVariable<ElementCombinationTuple<Cell>> variable) {
                        return -1.0;
                    }

                    @Override
                    public double getQuadraticCoefficient(MatrixIndex index, Pair<BinaryVariable<ElementCombinationTuple<Cell>>> binaryVariablePair) {
                        final var a = binaryVariablePair.getA().getLabel().create();
                        final var b = binaryVariablePair.getB().getLabel().create();
                        if (a.equals(b)) {
                            return 2.0;
                        }

                        return 0.0;
                    }
                };
            }
        };
    }

    @ProblemConstraint(weight = 6)
    boolean isPrefilledWithNumber(Sudoku sudoku, Cell cell) {
        final var existingCell = sudoku.getCell(cell.getRow(), cell.getCol());
        if (!existingCell.hasNumber()) {
            return true;
        }
        return existingCell.isSameNumber(cell);
    }

    @ProblemConstraint
    boolean isNotSameNumberInSameRow(Cell a, Cell b) {
        return a.isSameNumber(b) && !a.isSameRow(b) || !a.isSameNumber(b);
    }

    @ProblemConstraint
    boolean isNotSameNumberInSameCol(Cell a, Cell b) {
        return a.isSameNumber(b) && !a.isSameCol(b) || !a.isSameNumber(b);
    }

    @ProblemConstraint
    boolean isNotSameNumberInSameBlock(Cell a, Cell b) {
        return a.isSameNumber(b) && !a.isSameBlock(b, this.n) || !a.isSameNumber(b);
    }
}
