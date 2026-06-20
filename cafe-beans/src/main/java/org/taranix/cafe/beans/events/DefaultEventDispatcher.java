package org.taranix.cafe.beans.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class DefaultEventDispatcher<A extends Annotation> implements EventDispatcher<A> {

    private static final Logger log = LoggerFactory.getLogger(DefaultEventDispatcher.class);

    private final Class<A> annotationType;
    private final HandlerMethodInvoker invoker;
    private final List<WeakReference<Object>> subscribers = new CopyOnWriteArrayList<>();

    public DefaultEventDispatcher(Class<A> annotationType) {
        this.annotationType = annotationType;
        this.invoker = null;
    }

    public DefaultEventDispatcher(Class<A> annotationType, HandlerMethodInvoker invoker) {
        this.annotationType = annotationType;
        this.invoker = invoker;
    }

    @Override
    public void addIfRelevant(Object listener) {
        boolean hasHandlers = Arrays.stream(listener.getClass().getMethods())
                .anyMatch(m -> m.isAnnotationPresent(annotationType));
        if (hasHandlers) {
            subscribers.add(new WeakReference<>(listener));
        }
    }

    @Override
    public void send(Object... args) {
        subscribers.removeIf(ref -> ref.get() == null);
        subscribers.stream()
                .map(WeakReference::get)
                .filter(Objects::nonNull)
                .forEach(listener -> invokeOnListener(listener, args));
        if (invoker != null) {
            invoker.dispatchAll(annotationType, args);
        }
    }

    @Override
    public void sendTo(Object target, Object... args) {
        invokeOnListener(target, args);
    }

    private void invokeOnListener(Object listener, Object[] args) {
        for (Method m : listener.getClass().getMethods()) {
            if (!m.isAnnotationPresent(annotationType)) continue;
            if (!isCompatible(m, args)) continue;
            try {
                m.invoke(listener, args);
            } catch (InvocationTargetException e) {
                log.error("Handler {}.{} threw", listener.getClass().getSimpleName(), m.getName(), e.getCause());
            } catch (Exception e) {
                log.error("Failed to invoke handler {}.{}", listener.getClass().getSimpleName(), m.getName(), e);
            }
        }
    }

    private boolean isCompatible(Method m, Object[] args) {
        Class<?>[] types = m.getParameterTypes();
        if (types.length == 0) return args.length == 0;
        if (types.length != args.length) return false;
        for (int i = 0; i < types.length; i++) {
            if (args[i] != null && !types[i].isAssignableFrom(args[i].getClass())) return false;
        }
        return true;
    }
}
