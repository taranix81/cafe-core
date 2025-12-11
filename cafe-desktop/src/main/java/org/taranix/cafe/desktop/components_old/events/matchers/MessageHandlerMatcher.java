package org.taranix.cafe.desktop.components_old.events.matchers;

import org.taranix.cafe.desktop.components_old.events.messages.Message;

import java.lang.reflect.Method;

public interface MessageHandlerMatcher {
    boolean isMatch(Method method, Message message);
}
