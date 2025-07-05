package org.taranix.cafe.graphics.events.matchers;

import org.taranix.cafe.graphics.events.messages.CafeOldEvent;

import java.lang.reflect.Method;

public interface MessageHandlerMatcher {
    boolean isMatch(Method method, CafeOldEvent cafeOldEvent);
}
