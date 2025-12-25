package org.taranix.cafe.beans.events;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class CafeDispatcherService {

    @Getter
    private final Repository<TypeKey, BeanRepositoryEntry> repository;


    public CafeDispatcherService(Repository<TypeKey, BeanRepositoryEntry> repository) {
        this.repository = repository;
    }

    // --- Public API ---

    private static BeanTypeKey @NonNull [] getBeanTypeKeys(Object[] parameters) {
        return Arrays.stream(parameters)
                .map(p -> BeanTypeKey.from(p.getClass()))
                .toArray(BeanTypeKey[]::new); //
    }

    public Object dispatch(Class<? extends Annotation> annotationType, Object... parameters) {
        return dispatch(null, null, annotationType, parameters);
    }

    public Object dispatch(String handlerId, Class<? extends Annotation> annotationType, Object... parameters) {
        return dispatch(null, handlerId, annotationType, parameters);
    }

    // --- Logika wewnętrzna ---

    /**
     * Główna metoda dispatchująca z weryfikacją kompatybilności.
     */
    public Object dispatch(Object targetInstance, String handlerId, Class<? extends Annotation> annotationType, Object... parameters) {
        Collection<HandlerTypeKey> candidates = findHandlers(handlerId, annotationType, parameters);
        for (HandlerTypeKey matchedTypeKey : candidates) {
            return invokeHandler(matchedTypeKey, targetInstance, parameters);
        }
        return null;
    }

    private @NonNull Set<HandlerTypeKey> findHandlers(String handlerId, Class<? extends Annotation> annotationType, Object[] parameters) {
        BeanTypeKey[] parameterTypeKeys = getBeanTypeKeys(parameters);
        Collection<HandlerTypeKey> handlerTypeKeys = repository
                .getKeys(HandlerTypeKey.class::isInstance)
                .stream().map(HandlerTypeKey.class::cast)
                .collect(Collectors.toSet());

        return handlerTypeKeys.stream()
                .filter(handlerTypeKey -> TypeUtils.equals(handlerTypeKey.getAnnotationType(), annotationType))
                .filter(handlerTypeKey -> StringUtils.equals(handlerTypeKey.getTypeIdentifier(), StringUtils.defaultIfBlank(handlerId, StringUtils.EMPTY)))
                .filter(handlerTypeKey -> areEquals(handlerTypeKey.getParameters(), parameterTypeKeys))
                .collect(Collectors.toSet());
    }

    private Object invokeHandler(HandlerTypeKey handlerTypeKey, Object targetInstance, Object... parameters) {
        Collection<BeanRepositoryEntry> entries = repository.getMany(handlerTypeKey);

        for (BeanRepositoryEntry e : entries) {
            Method handlerMethod = (Method) e.getSource();
            if (handlerMethod == null) {
                log.warn("No method available to invoke, under : {}", handlerTypeKey);
                continue;
            }

            Object finalInstance = (targetInstance != null && isTypeCompatible(handlerMethod.getDeclaringClass(), targetInstance.getClass()))
                    ? targetInstance : e.getValue();

            if (finalInstance == null) {
                log.error("No instance available to invoke method: {}", handlerMethod.getName());
                continue;
            }

            return CafeReflectionUtils.getMethodValue(handlerMethod, finalInstance, parameters); //
        }
        return null;
    }


    private boolean areEquals(BeanTypeKey[] referenceParams, BeanTypeKey[] inputParams) {
        if (referenceParams.length != inputParams.length) {
            return false;
        }

        for (int i = 0; i < referenceParams.length; i++) {
            if (!isTypeCompatible(referenceParams[i].getType(), inputParams[i].getType())) {
                return false;
            }
        }
        return true;
    }

    private boolean isTypeCompatible(Type referenceType, Type inputType) {
        log.debug("Checking compatibility: {} vs. {}", referenceType, inputType);
        if (inputType instanceof Class<?> inputClass) {
            if (referenceType instanceof Class<?> refClass) {
                return refClass.isAssignableFrom(inputClass);
            }

            if (referenceType instanceof ParameterizedType refParameterizedType) {
                Set<Type> allSuperTypes = CafeTypesUtils.getAllSuperTypes(inputClass); //
                return allSuperTypes.stream()
                        .anyMatch(superType -> TypeUtils.equals(superType, refParameterizedType));
            }
        }

        return TypeUtils.equals(referenceType, inputType);
    }
}
