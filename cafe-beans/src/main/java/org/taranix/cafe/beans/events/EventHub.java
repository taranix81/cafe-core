package org.taranix.cafe.beans.events;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public class EventHub {

    private final Map<Class<? extends Annotation>, EventDispatcher<?>> dispatchers = new HashMap<>();

    public <A extends Annotation> void addDispatcher(Class<A> annotationType, EventDispatcher<A> dispatcher) {
        dispatchers.put(annotationType, dispatcher);
    }

    public void register(Object listener) {
        dispatchers.values().forEach(d -> d.addIfRelevant(listener));
    }

    @SuppressWarnings("unchecked")
    public <A extends Annotation> EventDispatcher<A> dispatcher(Class<A> annotationType) {
        return (EventDispatcher<A>) dispatchers.get(annotationType);
    }

    public <A extends Annotation> void send(Class<A> annotationType, Object... args) {
        EventDispatcher<A> dispatcher = dispatcher(annotationType);
        if (dispatcher != null) {
            dispatcher.send(args);
        }
    }

    public <A extends Annotation> void sendTo(Class<A> annotationType, Object target, Object... args) {
        EventDispatcher<A> dispatcher = dispatcher(annotationType);
        if (dispatcher != null) {
            dispatcher.sendTo(target, args);
        }
    }
}
