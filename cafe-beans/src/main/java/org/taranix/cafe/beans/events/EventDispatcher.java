package org.taranix.cafe.beans.events;

import java.lang.annotation.Annotation;

public interface EventDispatcher<A extends Annotation> {
    void register(Object listener);
    void unregister(Object listener);
    void send(Object... args);
    void sendTo(Object target, Object... args);
}
