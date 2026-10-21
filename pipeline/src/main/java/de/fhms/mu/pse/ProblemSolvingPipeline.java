package de.fhms.mu.pse;

import de.fhms.mu.pse.model.problem.ICombinatorialProblem;
import de.fhms.mu.pse.model.solver.IProblemSolverAdapter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ProblemSolvingPipeline<I> {
    private final ICombinatorialProblemFactory<I> factory;

    public BuildScope<?, ?> build(I input) {
        return new BuildScope<>(this.factory, input);
    }

    public <S, T> BuildScope<S, T> build(I input, Class<S> solutionType, Class<T> combinationTupleType) {
        return new BuildScope<>(this.factory, input);
    }

    public static ProblemSolvingPipeline<Object> create() {
        return new ProblemSolvingPipeline<>(new ProblemConfigurationProcessor());
    }

    public static <I> ProblemSolvingPipeline<I> create(ICombinatorialProblemFactory<I> factory) {
        return new ProblemSolvingPipeline<>(factory);
    }

    @Data
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public class BuildScope<S, T> {
        private final ICombinatorialProblemFactory<I> factory;
        private final I input;
        private String name;

        public BuildScope(ICombinatorialProblemFactory<I> factory, I input) {
            this(factory, input, UUID.randomUUID().toString());
        }

        public BuildScope<S, T> withName(String name) {
            this.name = name;
            return this;
        }

        public BuildScope<S, T> peek(Consumer<ICombinatorialProblem<S, T>> consumer) {
            final var problem = this.factory.<S, T>build(this.name, this.input);
            consumer.accept(problem);
            return this;
        }

        public Result<S, T> solve(IProblemSolverAdapter<S, T> solverAdapter) {
            final var problem = this.factory.<S, T>build(this.name, this.input);
            final var solutions = solverAdapter.solve(problem);
            return new Result<>(problem, solutions);
        }
    }

    @Data
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Result<S, T> {
        private final ICombinatorialProblem<S, T> problem;
        private final Stream<S> solutions;
    }
}
