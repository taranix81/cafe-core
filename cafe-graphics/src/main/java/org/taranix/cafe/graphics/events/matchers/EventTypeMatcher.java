package org.taranix.cafe.graphics.events.matchers;

import org.eclipse.swt.widgets.Event;
import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.graphics.events.annotations.SwtEventHandler;

import java.lang.reflect.Method;

@CafeService
class EventTypeMatcher implements EventHandlerMatcher {
    @Override
    public boolean isMatch(Method method, Event event) {
        return method.getAnnotation(SwtEventHandler.class).eventType().getSwtEventType() == event.type;
    }
}
