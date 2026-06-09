package org.taranix.cafe.beans.events;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultEventDispatcher<A extends Annotation> implements EventDispatcher<A> {

    private final Class<A> annotationType;
    private final HandlerMethodInvoker invoker;
    private final Set<Object> listeners = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public DefaultEventDispatcher(Class<A> annotationType, HandlerMethodInvoker invoker) {
        this.annotationType = annotationType;
        this.invoker = invoker;
    }

    @Override
    public void register(Object listener) {
        listeners.add(listener);
    }

    @Override
    public void unregister(Object listener) {
        listeners.remove(listener);
    }

    @Override
    public void send(Object... args) {
        invoker.dispatchAll(annotationType, args);
    }

    @Override
    public void sendTo(Object target, Object... args) {
        invoker.dispatchTo(annotationType, target, args);
    }
}
