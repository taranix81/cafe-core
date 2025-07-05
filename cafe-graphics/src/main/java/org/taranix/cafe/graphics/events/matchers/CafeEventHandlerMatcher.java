package org.taranix.cafe.graphics.events.matchers;

import org.taranix.cafe.graphics.events.CafeEvent;

import java.lang.reflect.Method;

public interface CafeEventHandlerMatcher {
    boolean isMatch(Method method, CafeEvent cafeEvent);
}
