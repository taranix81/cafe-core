package org.taranix.cafe.desktop.components_old.events.matchers;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.taranix.cafe.desktop.components_old.events.messages.Message;

import java.lang.reflect.Method;

//@CafeService
class MessageTypeMatcher implements MessageHandlerMatcher {
    @Override
    public boolean isMatch(Method method, Message message) {
        if (method.getParameterCount() == 1) {
            return TypeUtils.equals(message.getClass(), method.getParameterTypes()[0]);

        }
        return false;
    }
}
