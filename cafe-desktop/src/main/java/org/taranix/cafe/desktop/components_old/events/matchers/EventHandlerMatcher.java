package org.taranix.cafe.desktop.components_old.events.matchers;

import org.eclipse.swt.widgets.Event;

import java.lang.reflect.Method;

public interface EventHandlerMatcher {

    boolean isMatch(Method method, Event event);
}
