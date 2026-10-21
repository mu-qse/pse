package de.fhms.mu.pse;

import de.fhms.mu.pse.model.domain.DomainAggregate;
import de.fhms.mu.pse.model.domain.object.IDomainObjectFactory;
import de.fhms.mu.pse.model.domain.relation.ManyToOneRelation;
import de.fhms.mu.pse.model.domain.relation.OneToManyRelation;
import de.fhms.mu.pse.model.problem.DefaultCombinatorialProblem;
import de.fhms.mu.pse.model.problem.ElementSet;
import de.fhms.mu.pse.model.problem.ICombinatorialProblem;
import de.fhms.mu.pse.model.problem.constraint.CombinatorialProblemConstraint;
import de.fhms.mu.pse.model.problem.constraint.logic.BinaryLogicConstraint;
import de.fhms.mu.pse.model.problem.constraint.logic.UnaryLogicConstraint;
import de.fhms.mu.pse.model.problem.constraint.relation.ManyToOneRelationConstraint;
import de.fhms.mu.pse.model.problem.constraint.relation.OneToManyRelationConstraint;
import de.fhms.mu.pse.utils.TypeUtils;
import lombok.Data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Data
public class ProblemConfigurationProcessor implements ICombinatorialProblemFactory<Object> {
    @Override
    public <S, T> ICombinatorialProblem<S, T> build(String name, Object configuration) {
        final var configurationType = configuration.getClass();
        if (!configurationType.isAnnotationPresent(de.fhms.mu.pse.annotations.problem.ProblemConfiguration.class)) {
            throw new IllegalArgumentException("Given type " + configurationType.getSimpleName() + " must provide a @ProblemConfiguration annotation");
        }

        final var configurationAnnotation = configurationType.getAnnotation(de.fhms.mu.pse.annotations.problem.ProblemConfiguration.class);
        final var aggregateRootType = (Class<S>) configurationAnnotation.aggregateRoot();
        final var combinationTupleType = (Class<T>) configurationAnnotation.combinationTuple();
        return this.build(name, configuration, aggregateRootType, combinationTupleType);
    }

    @Override
    public <S, T> ICombinatorialProblem<S, T> build(String name, Object configuration, Class<S> solutionType, Class<T> combinationTupleType) {
        final var aggregateRootFactory = this.createAggregateRootFactory(configuration, solutionType);
        final var domainAggregateProcessor = new DomainAggregateProcessor<>(solutionType);
        final var domainAggregate = domainAggregateProcessor.build(aggregateRootFactory);

        final var combinationTupleDomainObject = domainAggregate.getObjectByType(combinationTupleType).orElseThrow();
        final var problem = new DefaultCombinatorialProblem<>(name, domainAggregate, combinationTupleDomainObject);

        final var selectableElements = this.createSelectableElements(problem, configuration);
        selectableElements.forEach(problem::addSelectableElements);

        final var constraints = this.createConstraints(problem, configuration);
        constraints.forEach(problem::addConstraint);

        return problem;
    }

    private <S, I> IDomainObjectFactory<S> createAggregateRootFactory(I configuration, Class<S> solutionType) {
        final var noArgsConstructorMethod = this.getSolutionConstructorMethod(configuration, solutionType);
        return new IDomainObjectFactory<>() {
            @Override
            public Class<S> getType() {
                return solutionType;
            }

            @Override
            public S create(Object... args) {
                try {
                    if (noArgsConstructorMethod.isPresent()) {
                        noArgsConstructorMethod.get().setAccessible(true);
                        return (S) noArgsConstructorMethod.get().invoke(configuration);
                    }
                    final var noArgsConstructor = solutionType.getConstructor();
                    return noArgsConstructor.newInstance();
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                    throw new RuntimeException(e.getCause());
                } catch (InvocationTargetException e) {
                    if (e.getCause() instanceof RuntimeException cause) {
                        throw cause;
                    }
                    throw new RuntimeException(e.getCause());
                }
            }
        };
    }

    private <S, I> Optional<Method> getSolutionConstructorMethod(I configuration, Class<S> solutionType) {
        final var configurationType = configuration.getClass();

        return Arrays.stream(configurationType.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(de.fhms.mu.pse.annotations.problem.AggregateRootFactory.class)
                        && method.getReturnType().equals(solutionType)
                        && method.getParameterCount() == 0)
                .findFirst();
    }

    private <S, T, I> Stream<ElementSet<?>> createSelectableElements(ICombinatorialProblem<S, T> problem, I configuration) {
        final var configurationType = configuration.getClass();
        final var domainAggregate = problem.getDomainAggregate();

        return Arrays.stream(configurationType.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(de.fhms.mu.pse.annotations.problem.SelectableElements.class))
                .filter(method -> Collection.class.isAssignableFrom(method.getReturnType()))
                .map(method -> this.createSelectableElementsByMethod(domainAggregate, configuration, method));
    }

    private <S, E> ElementSet<E> createSelectableElementsByMethod(DomainAggregate<S> domainAggregate, Object instance, Method method) {
        final var collectionType = (ParameterizedType) method.getGenericReturnType();
        final var collectionTypeClass = (Class<E>) collectionType.getActualTypeArguments()[0];
        final var domainType = domainAggregate.getObjectByType(collectionTypeClass).orElseThrow();

        final var elementSetAnnotation = method.getAnnotation(de.fhms.mu.pse.annotations.problem.SelectableElements.class);
        final var name = !elementSetAnnotation.value().isEmpty() ? elementSetAnnotation.value() : domainType.getName();

        final var parameters = method.getParameters();

        Object rawElements = null;

        if (parameters.length == 0) {
            try {
                method.setAccessible(true);
                rawElements = method.invoke(instance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e.getCause());
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof RuntimeException cause) {
                    throw cause;
                }
                throw new RuntimeException(e.getCause());
            }
        } else if (parameters.length == 1) {
            final var firstParameter = parameters[0];
            if (firstParameter.getType().equals(domainAggregate.getRoot().getType())) {
                try {
                    method.setAccessible(true);
                    final var emptySolution = domainAggregate.getRoot().create();
                    rawElements = method.invoke(instance, emptySolution);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e.getCause());
                } catch (InvocationTargetException e) {
                    if (e.getCause() instanceof RuntimeException cause) {
                        throw cause;
                    }
                    throw new RuntimeException(e.getCause());
                }
            }
        }

        if (rawElements instanceof ElementSet<?>) {
            return (ElementSet<E>) rawElements;
        } else if (rawElements instanceof Set<?>) {
            return new ElementSet<>(name, domainType, (Set<E>) rawElements);
        } else if (rawElements instanceof Collection<?>) {
            return new ElementSet<>(name, domainType, (Collection<E>) rawElements);
        }

        throw new IllegalArgumentException(String.format("Invalid return type for element-set provided by: %s %s", method.getReturnType().getName(), method.getName()));
    }

    private <S, T, I> Stream<? extends CombinatorialProblemConstraint<S, T>> createConstraints(ICombinatorialProblem<S, T> problem, I configuration) {
        final var configurationType = configuration.getClass();

        return Arrays.stream(configurationType.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(de.fhms.mu.pse.annotations.problem.ProblemConstraint.class))
                .map(method -> this.createConstraintByMethod(problem, configuration, method));
    }

    private <S, T, I> CombinatorialProblemConstraint<S, T> createConstraintByMethod(ICombinatorialProblem<S, T> problem, I configuration, Method method) {
        final var domainAggregate = problem.getDomainAggregate();
        final var solutionDomainObject = problem.getSolutionDomainObject();
        final var solutionType = solutionDomainObject.getType();
        final var combinationTupleDomainObject = problem.getCombinationTupleDomainObject();
        final var combinationTupleType = combinationTupleDomainObject.getType();

        final var constraintAnnotation = method.getAnnotation(de.fhms.mu.pse.annotations.problem.ProblemConstraint.class);
        final var annotatedWeight = constraintAnnotation.weight();

        final var parameters = method.getParameters();
        final var returnType = method.getReturnType();
        final var name = method.getName();

        if (CombinatorialProblemConstraint.class.isAssignableFrom(returnType)) {
            if (parameters.length == 0) {
                try {
                    method.setAccessible(true);
                    final var constraint = (CombinatorialProblemConstraint<S, T>) method.invoke(configuration);
                    constraint.setName(name);
                    constraint.setWeight(annotatedWeight);
                    return constraint;
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e.getCause());
                } catch (InvocationTargetException e) {
                    if (e.getCause() instanceof RuntimeException cause) {
                        throw cause;
                    }
                    throw new RuntimeException(e.getCause());
                }
            }

            final var firstParameter = parameters[0];
            if (firstParameter.getType().equals(ICombinatorialProblem.class)) {
                try {
                    method.setAccessible(true);
                    final var constraint = (CombinatorialProblemConstraint<S, T>) method.invoke(configuration, problem);
                    constraint.setName(name);
                    constraint.setWeight(annotatedWeight);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e.getCause());
                } catch (InvocationTargetException e) {
                    if (e.getCause() instanceof RuntimeException cause) {
                        throw cause;
                    }
                    throw new RuntimeException(e.getCause());
                }
            } else if (firstParameter.getType().equals(DomainAggregate.class)) {
                try {
                    method.setAccessible(true);
                    final var constraint = (CombinatorialProblemConstraint<S, T>) method.invoke(configuration, domainAggregate);
                    constraint.setName(name);
                    constraint.setWeight(annotatedWeight);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e.getCause());
                } catch (InvocationTargetException e) {
                    if (e.getCause() instanceof RuntimeException cause) {
                        throw cause;
                    }
                    throw new RuntimeException(e.getCause());
                }
            } else if (firstParameter.getType().equals(OneToManyRelation.class)) {
                final var relationType = (ParameterizedType) firstParameter.getParameterizedType();
                final var a = (Class<?>) relationType.getActualTypeArguments()[0];
                final var b = (Class<?>) relationType.getActualTypeArguments()[1];
                final var owner = domainAggregate.getObjectByType(a).orElseThrow();
                final var relation = owner.getOneToManyRelation(b).orElseThrow();
                try {
                    method.setAccessible(true);
                    final var constraint = (OneToManyRelationConstraint<S, T, ?, ?>) method.invoke(configuration, relation);
                    constraint.setName(name);
                    constraint.setWeight(annotatedWeight);
                    return constraint;
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e.getCause());
                } catch (InvocationTargetException e) {
                    if (e.getCause() instanceof RuntimeException cause) {
                        throw cause;
                    }
                    throw new RuntimeException(e.getCause());
                }
            } else if (firstParameter.getType().equals(ManyToOneRelation.class)) {
                final var relationType = (ParameterizedType) firstParameter.getParameterizedType();
                final var a = (Class<?>) relationType.getActualTypeArguments()[0];
                final var b = (Class<?>) relationType.getActualTypeArguments()[1];
                final var owner = domainAggregate.getObjectByType(a).orElseThrow();
                final var relation = owner.getManyToOneRelation(b).orElseThrow();
                try {
                    method.setAccessible(true);
                    final var constraint = (ManyToOneRelationConstraint<S, T, ?, ?>) method.invoke(configuration, relation);
                    constraint.setName(name);
                    constraint.setWeight(annotatedWeight);
                    return constraint;
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e.getCause());
                } catch (InvocationTargetException e) {
                    if (e.getCause() instanceof RuntimeException cause) {
                        throw cause;
                    }
                    throw new RuntimeException(e.getCause());
                }
            }

            throw new IllegalArgumentException("Unknown parameter type for method annotated with @ProblemConstraint: " + returnType.getSimpleName());
        }

        if (TypeUtils.isBooleanType(returnType)) {
            if (parameters.length == 1) {
                final var parameter = parameters[0];
                if (!parameter.getType().equals(combinationTupleType)) {
                    throw new IllegalArgumentException(String.format("Unknown parameter type for @ProblemConstraint method: %s(%s)", method.getName(), parameter.getType().getSimpleName()));
                }

                final var constraint = new UnaryLogicConstraint<S, T>(name) {
                    @Override
                    public boolean eval(T tuple) {
                        try {
                            method.setAccessible(true);
                            return (boolean) method.invoke(configuration, tuple);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e.getCause());
                        } catch (InvocationTargetException e) {
                            if (e.getCause() instanceof RuntimeException cause) {
                                throw cause;
                            }
                            throw new RuntimeException(e.getCause());
                        }
                    }
                };
                constraint.setName(name);
                constraint.setWeight(annotatedWeight);
                return constraint;
            }

            if (parameters.length == 2) {
                final var firstParameter = parameters[0];
                final var secondParameter = parameters[1];

                if (firstParameter.getType().equals(solutionType) && secondParameter.getType().equals(combinationTupleType)) {
                    final var emptySolution = domainAggregate.getRoot().create();
                    final var constraint = new UnaryLogicConstraint<S, T>(name) {
                        @Override
                        public boolean eval(T tuple) {
                            try {
                                method.setAccessible(true);
                                return (boolean) method.invoke(configuration, emptySolution, tuple);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e.getCause());
                            } catch (InvocationTargetException e) {
                                if (e.getCause() instanceof RuntimeException cause) {
                                    throw cause;
                                }
                                throw new RuntimeException(e.getCause());
                            }
                        }
                    };
                    constraint.setName(name);
                    constraint.setWeight(annotatedWeight);
                    return constraint;
                }

                if (firstParameter.getType().equals(combinationTupleType) && secondParameter.getType().equals(combinationTupleType)) {
                    final var constraint = new BinaryLogicConstraint<S, T>(name) {
                        @Override
                        public boolean eval(T a, T b) {
                            try {
                                method.setAccessible(true);
                                return (boolean) method.invoke(configuration, a, b);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e.getCause());
                            } catch (InvocationTargetException e) {
                                if (e.getCause() instanceof RuntimeException cause) {
                                    throw cause;
                                }
                                throw new RuntimeException(e.getCause());
                            }
                        }
                    };
                    constraint.setName(name);
                    constraint.setWeight(annotatedWeight);
                    return constraint;
                }
            } else if (parameters.length == 3) {
                final var firstParameter = parameters[0];
                final var secondParameter = parameters[1];
                final var thirdParameter = parameters[2];

                if (firstParameter.getType().equals(solutionType)
                        && secondParameter.getType().equals(combinationTupleType)
                        && thirdParameter.getType().equals(combinationTupleType)) {
                    final var emptySolution = domainAggregate.getRoot().create();
                    final var constraint = new BinaryLogicConstraint<S, T>(name) {
                        @Override
                        public boolean eval(T a, T b) {
                            try {
                                method.setAccessible(true);
                                return (boolean) method.invoke(configuration, emptySolution, a, b);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e.getCause());
                            } catch (InvocationTargetException e) {
                                if (e.getCause() instanceof RuntimeException cause) {
                                    throw cause;
                                }
                                throw new RuntimeException(e.getCause());
                            }
                        }
                    };
                    constraint.setName(name);
                    constraint.setWeight(annotatedWeight);
                    return constraint;
                }
            }

            throw new IllegalArgumentException("Illegal number of parameters of method annotated with @ProblemConstraint: " + Arrays.toString(parameters));
        }

        throw new IllegalArgumentException("Unknown return type of method annotated with @ProblemConstraint: " + returnType.getSimpleName());
    }
}
