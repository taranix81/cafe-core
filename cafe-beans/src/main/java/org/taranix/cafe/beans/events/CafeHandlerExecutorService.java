package org.taranix.cafe.beans.events;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.events.selectors.HandlerTypekeySelector;
import org.taranix.cafe.beans.reflection.CafeReflectionUtils;
import org.taranix.cafe.beans.reflection.CafeTypesUtils;
import org.taranix.cafe.beans.repositories.Repository;
import org.taranix.cafe.beans.repositories.beans.BeanRepositoryEntry;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.repositories.typekeys.HandlerTypeKey;
import org.taranix.cafe.beans.repositories.typekeys.TypeKey;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
public class CafeHandlerExecutorService {

    @Getter
    private final Repository<TypeKey, BeanRepositoryEntry> repository;


    public CafeHandlerExecutorService(Repository<TypeKey, BeanRepositoryEntry> repository) {
        this.repository = repository;
    }

    // --- Public API ---
    protected Collection<HandlerTypekeySelector> getSelectors(Class<? extends Annotation> methodAnnotationType) {
        return repository.getMany(BeanTypeKey.from(HandlerTypekeySelector.class)).stream()
                .map(BeanRepositoryEntry::getValue)
                .filter(HandlerTypekeySelector.class::isInstance)
                .map(HandlerTypekeySelector.class::cast)
                .collect(Collectors.toSet());

    }

    public Object dispatch(Class<? extends Annotation> methodAnnotationType, Object... parameters) {
        Collection<HandlerTypekeySelector> selectors = getSelectors(methodAnnotationType);

        Collection<HandlerTypeKey> candidates = repository
                .getKeys(HandlerTypeKey.class)
                .filter(handlerTypeKey -> selectors.stream()
                        .anyMatch(cafeHandlerSelector -> cafeHandlerSelector.isMatch(handlerTypeKey, methodAnnotationType, parameters)))
                .collect(Collectors.toSet());

        for (HandlerTypeKey matchedTypeKey : candidates) {
            return invokeHandler(matchedTypeKey, null, parameters);
        }
        return null;
    }

    private Object invokeHandler(HandlerTypeKey handlerTypeKey, Object targetInstance, Object... parameters) {
        Collection<BeanRepositoryEntry> entries = repository.getMany(handlerTypeKey);

        for (BeanRepositoryEntry e : entries) {
            Method handlerMethod = (Method) e.getSource();
            if (handlerMethod == null) {
                log.warn("No method available to invoke, under : {}", handlerTypeKey);
                continue;
            }

            Object value = e.getValue();
            Object handlerInstance = (value instanceof CafeHandlerSignature signature)
                    ? signature.getInstance().orElse(null)
                    : value;

            Object finalInstance = (targetInstance != null && CafeTypesUtils.isTypeCompatible(handlerMethod.getDeclaringClass(), targetInstance.getClass()))
                    ? targetInstance : handlerInstance;

            if (finalInstance == null) {
                log.error("No instance available to invoke method: {}", handlerMethod.getName());
                continue;
            }

            return CafeReflectionUtils.getMethodValue(handlerMethod, finalInstance, parameters); //
        }
        return null;
    }


}
