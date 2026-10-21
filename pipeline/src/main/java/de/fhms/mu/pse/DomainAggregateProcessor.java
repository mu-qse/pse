package de.fhms.mu.pse;

import de.fhms.mu.pse.annotations.domain.object.AggregateRoot;
import de.fhms.mu.pse.annotations.domain.object.Query;
import de.fhms.mu.pse.annotations.domain.object.Update;
import de.fhms.mu.pse.annotations.domain.relation.ManyToOne;
import de.fhms.mu.pse.annotations.domain.relation.OneToMany;
import de.fhms.mu.pse.annotations.domain.relation.OneToOne;
import de.fhms.mu.pse.model.domain.DomainAggregate;
import de.fhms.mu.pse.model.domain.IDomainAggregateBuilder;
import de.fhms.mu.pse.model.domain.object.DomainObject;
import de.fhms.mu.pse.model.domain.object.Entity;
import de.fhms.mu.pse.model.domain.object.IDomainObjectFactory;
import de.fhms.mu.pse.model.domain.object.ValueObject;
import de.fhms.mu.pse.model.domain.relation.DomainObjectRelation;
import de.fhms.mu.pse.model.domain.relation.ManyToOneRelation;
import de.fhms.mu.pse.model.domain.relation.OneToManyRelation;
import de.fhms.mu.pse.model.domain.relation.OneToOneRelation;
import lombok.Data;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Data
public class DomainAggregateProcessor<R> implements IDomainAggregateBuilder<R, IDomainObjectFactory<R>> {
    private final Class<R> aggregateRootType;

    @Override
    public DomainAggregate<R> build(IDomainObjectFactory<R> aggregateFactory) {
        if (!this.aggregateRootType.isAnnotationPresent(AggregateRoot.class)) {
            throw new IllegalArgumentException(String.format("Class must be annotated with @AggregateRoot: %s", this.aggregateRootType.getSimpleName()));
        }

        final var aggregateRoot = new Entity<>(this.aggregateRootType, aggregateFactory::create);
        final var domainAggregate = new DomainAggregate<>(aggregateRoot);

        final var objects = this.createObjects(domainAggregate, aggregateRoot);
        objects.forEach(domainAggregate::add);

        final var relations = this.createRelations(domainAggregate);
        relations.forEach(domainAggregate::add);

        return domainAggregate;
    }

    private Stream<DomainObject<?>> createObjects(DomainAggregate<R> domainAggregate, DomainObject<?> parent) {
        return Stream.of(
                this.createTypesFromOneToManyRelationFields(domainAggregate, parent),
                this.createTypesFromOneToManyRelationMethods(domainAggregate, parent),
                this.createTypesFromManyToOneRelationFields(domainAggregate, parent),
                this.createTypesFromManyToOneRelationMethods(domainAggregate, parent)
        )
                .flatMap(stream -> stream)
                .flatMap(domainObject -> Stream.concat(Stream.of(domainObject), this.createObjects(domainAggregate, domainObject)));
    }

    private Stream<? extends DomainObject<?>> createTypesFromOneToManyRelationFields(DomainAggregate<R> domainAggregate, DomainObject<?> parent) {
        return Arrays.stream(parent.getType().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(OneToMany.class))
                .filter(method -> Collection.class.isAssignableFrom(method.getType()))
                .map(field -> {
                    final var collectionType = (ParameterizedType) field.getGenericType();
                    return (Class<Object>) collectionType.getActualTypeArguments()[0];
                })
                .filter(type -> domainAggregate.getObjectByType(type).isEmpty())
                .map(type -> this.createDomainObject(domainAggregate, type));
    }

    private Stream<? extends DomainObject<?>> createTypesFromOneToManyRelationMethods(DomainAggregate<R> domainAggregate, DomainObject<?> parent) {
        return Arrays.stream(parent.getType().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(OneToMany.class))
                .filter(method -> Collection.class.isAssignableFrom(method.getReturnType()))
                .map(method -> {
                    final var collectionType = (ParameterizedType) method.getGenericReturnType();
                    return (Class<Object>) collectionType.getActualTypeArguments()[0];
                })
                .filter(type -> domainAggregate.getObjectByType(type).isEmpty())
                .map(type -> this.createDomainObject(domainAggregate, type));
    }

    private Stream<? extends DomainObject<?>> createTypesFromManyToOneRelationFields(DomainAggregate<R> domainAggregate, DomainObject<?> parent) {
        return Arrays.stream(parent.getType().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(ManyToOne.class))
                .map(field -> (Class<Object>) field.getType())
                .filter(type -> domainAggregate.getObjectByType(type).isEmpty())
                .map(type -> this.createDomainObject(domainAggregate, type));
    }

    private Stream<? extends DomainObject<?>> createTypesFromManyToOneRelationMethods(DomainAggregate<R> domainAggregate, DomainObject<?> parent) {
        return Arrays.stream(parent.getType().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(ManyToOne.class))
                .map(method -> (Class<Object>) method.getReturnType())
                .filter(type -> domainAggregate.getObjectByType(type).isEmpty())
                .map(type -> this.createDomainObject(domainAggregate, type));
    }

    private DomainObject<?> createDomainObject(DomainAggregate<R> domainAggregate, Class<Object> type) {
        if (type.isAnnotationPresent(de.fhms.mu.pse.annotations.domain.object.ValueObject.class)) {
            return this.createValueObjectFromType(type);
        }

        if (type.isAnnotationPresent(de.fhms.mu.pse.annotations.domain.object.Entity.class)) {
            return this.createEntityFromType(type);
        }

        return this.createDomainObjectFromPrimitiveType(type);
        // throw new IllegalArgumentException("Domain type must be annotated: " + type.getSimpleName());
    }

    private <T> ValueObject<T> createValueObjectFromType(Class<T> type) {
        final var defaultConstructor = Arrays.stream(type.getDeclaredConstructors())
                .filter(constructor -> type.isRecord() || constructor.getParameterCount() == 0)
                .findFirst()
                .map(constructor -> (Constructor<T>) constructor);

        return new ValueObject<>(type, args -> {
            if (defaultConstructor.isEmpty()) {
                throw new IllegalArgumentException("Missing default constructor in class for value object: " + type.getSimpleName());
            }

            try {
                return defaultConstructor.get().newInstance(args);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof RuntimeException cause) {
                    throw cause;
                }
                throw new RuntimeException(e.getCause());
            }
        });
    }

    private <T> Entity<T> createEntityFromType(Class<T> type) {
        final var defaultConstructor = Arrays.stream(type.getDeclaredConstructors())
                .filter(constructor -> constructor.getParameterCount() == 0)
                .findFirst()
                .map(constructor -> (Constructor<T>) constructor);

        return new Entity<>(type, args -> {
            if (defaultConstructor.isEmpty()) {
                throw new IllegalArgumentException("Missing default constructor in class for entity: " + type.getSimpleName());
            }

            try {
                return defaultConstructor.get().newInstance(args);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof RuntimeException cause) {
                    throw cause;
                }
                throw new RuntimeException(e.getCause());
            }
        });
    }

    private DomainObject<Object> createDomainObjectFromPrimitiveType(Class<Object> type) {
        final var defaultConstructor = Arrays.stream(type.getDeclaredConstructors())
                .filter(constructor -> constructor.getParameterCount() == 0)
                .findFirst()
                .map(constructor -> (Constructor<Object>) constructor);

        return new DomainObject<>(type) {
            @Override
            public String getName() {
                return type.getSimpleName();
            }

            @Override
            public Class<Object> getType() {
                return type;
            }

            @Override
            public Object create(Object... args) {
                if (defaultConstructor.isEmpty()) {
                    throw new IllegalArgumentException("Missing default constructor in class for entity: " + type.getSimpleName());
                }

                try {
                    return defaultConstructor.get().newInstance(args);
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    if (e.getCause() instanceof RuntimeException cause) {
                        throw cause;
                    }
                    throw new RuntimeException(e.getCause());
                }
            }
        };
    }

    private List<? extends DomainObjectRelation<?, ?>> createRelations(DomainAggregate<R> domainAggregate) {
        final var domainObjects = domainAggregate.getObjects();
        return domainObjects.stream()
                .flatMap(domainObject -> {
                    final var type = domainObject.getType();
                    return Stream.of(
                            this.createOneToManyRelationsByFields(domainAggregate, domainObject, type),
                            this.createOneToManyRelationsByMethods(domainAggregate, domainObject, type),
                            this.createManyToOneRelationsByFields(domainAggregate, domainObject, type),
                            this.createManyToOneRelationsByMethods(domainAggregate, domainObject, type),
                            this.createOneToOneRelationsByFields(domainAggregate, domainObject, type),
                            this.createOneToOneRelationsByMethods(domainAggregate, domainObject, type)
                    ).flatMap(stream -> stream);
                })
                .toList();
    }

    private Stream<? extends OneToManyRelation<?, Object>> createOneToManyRelationsByFields(DomainAggregate<R> domainAggregate, DomainObject<?> domainObject, Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(OneToMany.class))
                .filter(field -> Collection.class.isAssignableFrom(field.getType()))
                .map(field -> this.getOneToManyRelationByField(domainAggregate, domainObject, field));
    }

    private <A, B> OneToManyRelation<A, B> getOneToManyRelationByField(DomainAggregate<R> domainAggregate, DomainObject<A> domainObject, Field field) {
        final var collectionType = (ParameterizedType) field.getGenericType();
        final var collectionTypeClass = (Class<?>) collectionType.getActualTypeArguments()[0];
        final var b = domainAggregate.getObjectByType((Class<B>) collectionTypeClass)
                .orElseThrow(() -> new IllegalArgumentException("Could not find domain-object: " + collectionTypeClass.getSimpleName()));

        final var accessor = this.getOneToManyQueryMethodForType(domainObject, b.getType())
                .orElseThrow(() -> new IllegalArgumentException(String.format("Missing @Query for type: %s <> %s", domainObject.getType().getSimpleName(), b.getType().getSimpleName())));
        final var mutator = this.getOneToManyUpdateMethodForType(domainObject, b.getType());
                //.orElseThrow(() -> new IllegalArgumentException(String.format("Missing @Update for type: %s <> %s", domainObject.getType().getSimpleName(), b.getType().getSimpleName())));

        return new OneToManyRelation<>(domainObject, b, instance -> {
            try {
                return (Collection<B>) accessor.invoke(instance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e.getCause());
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof RuntimeException cause) {
                    throw cause;
                }
                throw new RuntimeException(e.getCause());
            }
        }, (instance, values) -> {
            if (mutator.isEmpty()) {
                return instance;
            }

            final var parameters = mutator.get().getParameters();
            final var firstParameter = parameters[0];
            final var parameterType = firstParameter.getType();

            try {
                if (Collection.class.isAssignableFrom(parameterType)) {
                    return (A) mutator.get().invoke(instance, values);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e.getCause());
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof RuntimeException cause) {
                    throw cause;
                }
                throw new RuntimeException(e.getCause());
            }

            return values.stream().map(value -> {
                try {
                    return (A) mutator.get().invoke(instance, value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e.getCause());
                } catch (InvocationTargetException e) {
                    if (e.getCause() instanceof RuntimeException cause) {
                        throw cause;
                    }
                    throw new RuntimeException(e.getCause());
                }
            }).reduce(instance, (u, v) -> u);
        });
    }

    private Stream<? extends OneToManyRelation<?, Object>> createOneToManyRelationsByMethods(DomainAggregate<R> domainAggregate, DomainObject<?> domainObject, Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(OneToMany.class))
                .filter(method -> Collection.class.isAssignableFrom(method.getReturnType()))
                .map(method -> this.getOneToManyRelationByMethod(domainAggregate, domainObject, method));
    }

    private <A, B> OneToManyRelation<A, B> getOneToManyRelationByMethod(DomainAggregate<R> domainAggregate, DomainObject<A> domainObject, Method method) {
        final var collectionType = (ParameterizedType) method.getGenericReturnType();
        final var collectionTypeClass = (Class<?>) collectionType.getActualTypeArguments()[0];
        final var b = domainAggregate.getObjectByType((Class<B>) collectionTypeClass)
                .orElseThrow(() -> new IllegalArgumentException("Could not find domain-object: " + collectionTypeClass.getSimpleName()));

        final var mutator = this.getOneToManyUpdateMethodForType(domainObject, b.getType());
                //.orElseThrow(() -> new IllegalArgumentException(String.format("Missing @Update for relation: %s <> %s", domainObject.getType().getSimpleName(), b.getType().getSimpleName())));

        return new OneToManyRelation<>(domainObject, b, instance -> {
            try {
                return (Collection<B>) method.invoke(instance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e.getCause());
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof RuntimeException cause) {
                    throw cause;
                }
                throw new RuntimeException(e.getCause());
            }
        }, (instance, values) -> {
            if (mutator.isEmpty()) {
                return instance;
            }

            final var parameters = mutator.get().getParameters();
            final var firstParameter = parameters[0];
            final var parameterType = firstParameter.getType();

            try {
                if (Collection.class.isAssignableFrom(parameterType)) {
                    return (A) mutator.get().invoke(instance, values);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e.getCause());
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof RuntimeException cause) {
                    throw cause;
                }
                throw new RuntimeException(e.getCause());
            }

            return values.stream().map(value -> {
                try {
                    return (A) mutator.get().invoke(instance, value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    if (e.getCause() instanceof RuntimeException cause) {
                        throw cause;
                    }
                    throw new RuntimeException(e.getCause());
                }
            }).reduce(instance, (u, v) -> u);
        });
    }

    private <T, B> Optional<Method> getOneToManyQueryMethodForType(DomainObject<T> domainObject, Class<B> b) {
        final var type = domainObject.getType();
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Query.class))
                .filter(method -> {
                    final var collectionType = (ParameterizedType) method.getGenericReturnType();
                    final var collectionTypeClass = (Class<?>) collectionType.getActualTypeArguments()[0];
                    return collectionTypeClass.equals(b);
                })
                .findFirst();
    }

    private <T, B> Optional<Method> getOneToManyUpdateMethodForType(DomainObject<T> domainObject, Class<B> b) {
        final var type = domainObject.getType();
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Update.class))
                .filter(method -> {
                    final var returnType = method.getReturnType();
                    if (!returnType.equals(type)) {
                        throw new IllegalArgumentException(String.format("Invalid return type of method: %s %s()", returnType.getSimpleName(), method.getName()));
                    }

                    final var parameters = method.getParameters();
                    final var firstParameter = parameters[0];
                    final var parameterType = firstParameter.getType();

                    if (Collection.class.isAssignableFrom(parameterType)) {
                        final var collectionType = (ParameterizedType) firstParameter.getParameterizedType();
                        final var collectionTypeClass = (Class<?>) collectionType.getActualTypeArguments()[0];
                        return collectionTypeClass.equals(b);
                    }

                    return parameterType.equals(b);
                })
                .findFirst();
    }

    private Stream<? extends ManyToOneRelation<?, Object>> createManyToOneRelationsByFields(DomainAggregate<R> domainAggregate, DomainObject<?> domainObject, Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(ManyToOne.class))
                .map(field -> this.getManyToOneRelationByField(domainAggregate, domainObject, field));
    }

    private <A, B> ManyToOneRelation<A, B> getManyToOneRelationByField(DomainAggregate<R> domainAggregate, DomainObject<A> domainObject, Field field) {
        final var fieldType = field.getType();
        final var b = domainAggregate.getObjectByType((Class<B>) fieldType)
                .orElseThrow(() -> new IllegalArgumentException("Could not find domain-object: " + fieldType.getSimpleName()));
        final var accessor = this.getManyToOneQueryMethodForType(domainObject, fieldType)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Missing @Query for field: %s %s", fieldType.getSimpleName(), field.getName())));
        final var mutator = this.getManyToOneUpdateMethodForType(domainObject, fieldType)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Missing @Update for field: %s %s", fieldType.getSimpleName(), field.getName())));

        return new ManyToOneRelation<>(domainObject, b, instance -> {
            try {
                return (B) accessor.invoke(instance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof RuntimeException cause) {
                    throw cause;
                }
                throw new RuntimeException(e.getCause());
            }
        }, (instance, value) -> {
            try {
                return (A) mutator.invoke(instance, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof RuntimeException cause) {
                    throw cause;
                }
                throw new RuntimeException(e.getCause());
            }
        });
    }

    private Stream<? extends ManyToOneRelation<?, Object>> createManyToOneRelationsByMethods(DomainAggregate<R> domainAggregate, DomainObject<?> domainObject, Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(ManyToOne.class))
                .map(method -> this.getManyToOneRelationByMethod(domainAggregate, domainObject, method));
    }

    private <A, B> ManyToOneRelation<A, B> getManyToOneRelationByMethod(DomainAggregate<R> domainAggregate, DomainObject<A> domainObject, Method method) {
        final var returnType = method.getReturnType();
        final var b = domainAggregate.getObjectByType((Class<B>) returnType)
                .orElseThrow(() -> new IllegalArgumentException("Could not find domain-object: " + returnType.getSimpleName()));
        final var mutator = this.getManyToOneUpdateMethodForType(domainObject, returnType)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Missing @Update for type: %s", domainObject.getType().getSimpleName())));

        return new ManyToOneRelation<>(domainObject, b, instance -> {
            try {
                return (B) method.invoke(instance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof RuntimeException cause) {
                    throw cause;
                }
                throw new RuntimeException(e.getCause());
            }
        }, (instance, value) -> {
            try {
                return (A) mutator.invoke(instance, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof RuntimeException cause) {
                    throw cause;
                }
                throw new RuntimeException(e.getCause());
            }
        });
    }

    private <T, B> Optional<Method> getManyToOneQueryMethodForType(DomainObject<T> domainObject, Class<B> b) {
        final var type = domainObject.getType();
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Query.class)
                        && method.getReturnType().equals(b))
                .findFirst();
    }

    private <T, B> Optional<Method> getManyToOneUpdateMethodForType(DomainObject<T> domainObject, Class<B> b) {
        final var type = domainObject.getType();
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> {
                    if (!method.isAnnotationPresent(Update.class)) {
                        return false;
                    }

                    final var returnType = method.getReturnType();
                    final var parameters = method.getParameters();
                    final var firstParameter = parameters[0];
                    final var parameterType = firstParameter.getType();
                    if (!parameterType.equals(b)) {
                        return false;
                    }
                    if (!returnType.equals(type)) {
                        throw new IllegalArgumentException(String.format("Invalid return type of method: %s %s(%s) - should be %s %s(%s)", returnType.getSimpleName(), method.getName(), parameterType.getSimpleName(), type.getSimpleName(), method.getName(), parameterType.getSimpleName()));
                    }

                    return true;
                })
                .findFirst();
    }

    private Stream<? extends OneToOneRelation<?, Object>> createOneToOneRelationsByFields(DomainAggregate<R> domainAggregate, DomainObject<?> domainObject, Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(OneToOne.class))
                .map(field -> this.getOneToOneRelationByField(domainAggregate, domainObject, field));
    }

    private <A, B> OneToOneRelation<A, B> getOneToOneRelationByField(DomainAggregate<R> domainAggregate, DomainObject<A> domainObject, Field field) {
        final var fieldType = field.getType();
        final var b = domainAggregate.getObjectByType((Class<B>) fieldType)
                .orElseThrow(() -> new IllegalArgumentException("Could not find domain-object: " + fieldType.getSimpleName()));
        final var accessor = this.getOneToOneQueryMethodForType(domainObject, fieldType)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Missing @Query for field: %s %s", fieldType.getSimpleName(), field.getName())));
        final var mutator = this.getOneToOneUpdateMethodForType(domainObject, fieldType)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Missing @Update for field: %s %s", fieldType.getSimpleName(), field.getName())));

        return new OneToOneRelation<>(domainObject, b, instance -> {
            try {
                return (B) accessor.invoke(instance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof RuntimeException cause) {
                    throw cause;
                }
                throw new RuntimeException(e.getCause());
            }
        }, (instance, value) -> {
            try {
                return (A) mutator.invoke(instance, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof RuntimeException cause) {
                    throw cause;
                }
                throw new RuntimeException(e.getCause());
            }
        });
    }

    private Stream<? extends OneToOneRelation<?, Object>> createOneToOneRelationsByMethods(DomainAggregate<R> domainAggregate, DomainObject<?> domainObject, Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(OneToOne.class))
                .map(method -> this.getOneToOneRelationByMethod(domainAggregate, domainObject, method));
    }

    private <A, B> OneToOneRelation<A, B> getOneToOneRelationByMethod(DomainAggregate<R> domainAggregate, DomainObject<A> domainObject, Method method) {
        final var returnType = method.getReturnType();
        final var b = domainAggregate.getObjectByType((Class<B>) returnType)
                .orElseThrow(() -> new IllegalArgumentException("Could not find domain-object: " + returnType.getSimpleName()));
        final var mutator = this.getOneToOneUpdateMethodForType(domainObject, returnType)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Missing @Update for type: %s", domainObject.getType().getSimpleName())));

        return new OneToOneRelation<>(domainObject, b, instance -> {
            try {
                return (B) method.invoke(instance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof RuntimeException cause) {
                    throw cause;
                }
                throw new RuntimeException(e.getCause());
            }
        }, (instance, value) -> {
            try {
                return (A) mutator.invoke(instance, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof RuntimeException cause) {
                    throw cause;
                }
                throw new RuntimeException(e.getCause());
            }
        });
    }

    private <T, B> Optional<Method> getOneToOneQueryMethodForType(DomainObject<T> domainObject, Class<B> b) {
        final var type = domainObject.getType();
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Query.class)
                        && method.getReturnType().equals(b))
                .findFirst();
    }

    private <T, B> Optional<Method> getOneToOneUpdateMethodForType(DomainObject<T> domainObject, Class<B> b) {
        final var type = domainObject.getType();
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> {
                    if (!method.isAnnotationPresent(Update.class)) {
                        return false;
                    }

                    final var returnType = method.getReturnType();
                    final var parameters = method.getParameters();
                    final var firstParameter = parameters[0];
                    final var parameterType = firstParameter.getType();
                    if (!parameterType.equals(b)) {
                        return false;
                    }
                    if (!returnType.equals(type)) {
                        throw new IllegalArgumentException(String.format("Invalid return type of method: %s %s(%s) - should be %s %s(%s)", returnType.getSimpleName(), method.getName(), parameterType.getSimpleName(), type.getSimpleName(), method.getName(), parameterType.getSimpleName()));
                    }

                    return true;
                })
                .findFirst();
    }
}
