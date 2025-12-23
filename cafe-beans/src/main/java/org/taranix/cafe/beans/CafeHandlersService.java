package org.taranix.cafe.beans;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.taranix.cafe.beans.reflection.CafeReflectionUtils;
import org.taranix.cafe.beans.reflection.CafeTypesUtils;
import org.taranix.cafe.beans.repositories.Repository;
import org.taranix.cafe.beans.repositories.beans.BeanRepositoryEntry;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.repositories.typekeys.HandlerTypeKey;
import org.taranix.cafe.beans.repositories.typekeys.TypeKey;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class CafeHandlersService {

    @Getter
    private final Repository<TypeKey, BeanRepositoryEntry> repository;

    public CafeHandlersService(Repository<TypeKey, BeanRepositoryEntry> repository) {
        this.repository = repository;
    }

    private static BeanTypeKey @NonNull [] getBeanTypeKeys(Object[] parameters) {
        return Arrays.stream(parameters)
                .map(p -> BeanTypeKey.from(p.getClass()))
                .toArray(BeanTypeKey[]::new);
    }

    public Object executeSingletonHandler(Class<? extends Annotation> annotationType, Object... parameters) {
        return executeSingletonHandler(null, annotationType, parameters);
    }

    private Object executeHandlerFor(HandlerTypeKey handlerTypeKey, Object... parameters) {
        Collection<BeanRepositoryEntry> entries = repository.getMany(handlerTypeKey);
        log.debug("Found handlers : {}", entries);

        for (BeanRepositoryEntry e : entries) {
            Executable handlerMethod = e.getSource();
            Object handlerInstance = e.getValue();
            return CafeReflectionUtils.getMethodValue((Method) handlerMethod, handlerInstance, parameters);
        }
        return null;
    }


    public Object executeSingletonHandler(String handlerId, Class<? extends Annotation> annotationType, Object... parameters) {
        BeanTypeKey[] parameterTypeKeys = getBeanTypeKeys(parameters);
        HandlerTypeKey candidateLookup = HandlerTypeKey.from(annotationType, handlerId, parameterTypeKeys);

        Collection<HandlerTypeKey> matched = repository.getKeys(typeKey -> (typeKey instanceof HandlerTypeKey existingHandler)
                        && isMatched(existingHandler, candidateLookup)
                )
                .stream()
                .map(HandlerTypeKey.class::cast)
                .collect(Collectors.toSet());

        for (HandlerTypeKey matchedTypeKey : matched) {
            return executeHandlerFor(matchedTypeKey, parameters);
        }
        return null;
    }


    private boolean isMatched(HandlerTypeKey declaredHandler, HandlerTypeKey candidateLookup) {
        // 1. Guard Clauses & Basic Identity Check
        if (declaredHandler == null || candidateLookup == null) {
            return false;
        }

        // Check Annotation mismatch using safe Type comparison
        if (!TypeUtils.equals(declaredHandler.getAnnotation(), candidateLookup.getAnnotation())) {
            return false;
        }

        // Check Type Identifier
        if (!Objects.equals(declaredHandler.getTypeIdentifier(), candidateLookup.getTypeIdentifier())) {
            return false;
        }

        // 2. Parameters Length Check
        BeanTypeKey[] declaredParams = declaredHandler.getParameters();
        BeanTypeKey[] candidateLookupParams = candidateLookup.getParameters();

        if (declaredParams.length != candidateLookupParams.length) {
            return false;
        }

        // 3. Detailed Parameters Matching
        for (int i = 0; i < declaredParams.length; i++) {
            if (!isParameterMatched(declaredParams[i], candidateLookupParams[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * Helper method to match individual parameter types considering inheritance and generics.
     */
    private boolean isParameterMatched(BeanTypeKey declaredHandlerParam, BeanTypeKey candidateLookupParam) {
        Type declaredType = declaredHandlerParam.getType();
        Type executorType = candidateLookupParam.getType();

        // Case A: Both are simple classes - check standard assignability
        if (declaredHandlerParam.isClass() && candidateLookupParam.isClass()) {
            return ((Class<?>) declaredType).isAssignableFrom((Class<?>) executorType);
        }

        // Case B: Declared is Generic (ParameterizedType), Candidate is a Concrete Class
        // We check if the executor's hierarchy contains the required generic signature
        if (declaredHandlerParam.isParametrizedType() && candidateLookupParam.isClass()) {
            Set<Type> allSuperTypes = CafeTypesUtils.getAllSuperTypes((Class<?>) executorType); //
            return allSuperTypes.stream()
                    .anyMatch(superType -> TypeUtils.equals(superType, declaredType)); //
        }

        // Case C: Exact match fallback (for TypeVariables or already resolved types)
        return TypeUtils.equals(declaredType, executorType);
    }
}
