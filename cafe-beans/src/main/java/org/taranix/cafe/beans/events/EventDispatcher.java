package org.taranix.cafe.beans.events;

import java.lang.annotation.Annotation;

public interface EventDispatcher<A extends Annotation> {
    void addIfRelevant(Object listener);
    void send(Object... args);
    void sendTo(Object target, Object... args);
}
