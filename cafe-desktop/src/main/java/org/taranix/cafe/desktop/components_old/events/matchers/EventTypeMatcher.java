package org.taranix.cafe.desktop.components_old.events.matchers;

import org.eclipse.swt.widgets.Event;
import org.taranix.cafe.desktop.components_old.events.annotations.CafeEventHandler;

import java.lang.reflect.Method;

//@CafeService
class EventTypeMatcher implements EventHandlerMatcher {
    @Override
    public boolean isMatch(Method method, Event event) {
        return method.getAnnotation(CafeEventHandler.class).eventType().getSwtEventType() == event.type;
    }
}
