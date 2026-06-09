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
public class HandlerMethodInvoker {

    @Getter
    private final Repository<TypeKey, BeanRepositoryEntry> repository;

    public HandlerMethodInvoker(Repository<TypeKey, BeanRepositoryEntry> repository) {
        this.repository = repository;
    }

    protected Collection<HandlerTypekeySelector> getSelectors(Class<? extends Annotation> methodAnnotationType) {
        return repository.getMany(BeanTypeKey.from(HandlerTypekeySelector.class)).stream()
                .map(BeanRepositoryEntry::getValue)
                .filter(HandlerTypekeySelector.class::isInstance)
                .map(HandlerTypekeySelector.class::cast)
                .collect(Collectors.toSet());
    }

    private Collection<HandlerTypeKey> getCandidates(Class<? extends Annotation> annotationType, Object... parameters) {
        Collection<HandlerTypekeySelector> selectors = getSelectors(annotationType);
        return repository.getKeys(HandlerTypeKey.class)
                .filter(key -> selectors.stream()
                        .anyMatch(sel -> sel.isMatch(key, annotationType, parameters)))
                .collect(Collectors.toSet());
    }

    public Object dispatch(Class<? extends Annotation> methodAnnotationType, Object... parameters) {
        for (HandlerTypeKey matchedTypeKey : getCandidates(methodAnnotationType, parameters)) {
            return invokeHandler(matchedTypeKey, null, parameters);
        }
        return null;
    }

    public void dispatchAll(Class<? extends Annotation> annotationType, Object... args) {
        getCandidates(annotationType, args).forEach(key -> invokeHandler(key, null, args));
    }

    public void dispatchTo(Class<? extends Annotation> annotationType, Object target, Object... args) {
        getCandidates(annotationType, args).forEach(key -> invokeHandler(key, target, args));
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

            return CafeReflectionUtils.getMethodValue(handlerMethod, finalInstance, parameters);
        }
        return null;
    }
}
