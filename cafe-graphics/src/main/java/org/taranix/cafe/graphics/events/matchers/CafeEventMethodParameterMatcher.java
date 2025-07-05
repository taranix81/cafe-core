package org.taranix.cafe.graphics.events.matchers;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.graphics.events.CafeEvent;

import java.lang.reflect.Method;

@CafeService
class CafeEventMethodParameterMatcher implements CafeEventHandlerMatcher {
    @Override
    public boolean isMatch(Method method, CafeEvent cafeOldEvent) {
        if (method.getParameterCount() == 1) {
            return TypeUtils.equals(cafeOldEvent.getClass(), method.getParameterTypes()[0]);

        }
        return false;
    }
}
