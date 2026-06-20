package org.taranix.cafe.desktop.events;

import org.taranix.cafe.beans.annotations.classes.CafeSingleton;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.desktop.annotations.CafeEventHandler;

import java.lang.reflect.Method;

@CafeSingleton
public class CafeEventHandlerRegistry {

    @CafeInject
    private CafeEventHandlerHub hub;

    public void register(Object handler) {
        if (hasEventHandlerMethods(handler)) {
            hub.register(handler);
        }
    }

    private boolean hasEventHandlerMethods(Object handler) {
        for (Method m : handler.getClass().getMethods()) {
            if (m.isAnnotationPresent(CafeEventHandler.class)) return true;
        }
        return false;
    }
}
