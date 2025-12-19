package org.taranix.cafe.desktop.actions;

import org.apache.commons.lang3.StringUtils;
import org.taranix.cafe.beans.annotations.classes.CafeService;
import org.taranix.cafe.beans.reflection.CafeAnnotationUtils;
import org.taranix.cafe.beans.reflection.CafeReflectionUtils;
import org.taranix.cafe.desktop.components_old.events.annotations.CafeEventHandler;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

@CafeService
public class ActionBus {

    //  @CafeInject
    private Map<Class<? extends Action>, Set<Object>> handlers;


    private Set<Object> handlers(Class<? extends Action> type) {
        return handlers.getOrDefault(type, Set.of());
    }

    public <T extends Action> T broadcast(T action) {
        //handlers(action.getClass()).forEach(h -> sendTo(action, h));
        return action;
    }

    public <T extends Action> T broadcast(T action, Class<?> receiverType) {
        handlers(action.getClass()).stream()
                .filter(o -> o.getClass().equals(receiverType))
                .forEach(h -> sendTo(action, h));
        return action;
    }

    public <T extends Action> T sendTo(T action, Object receiver) {
        List<Method> handlerMethods = CafeAnnotationUtils.getClassMethodsAnnotatedBy(receiver.getClass(), CafeEventHandler.class);

        for (Method method : handlerMethods) {
            if (method.getParameterCount() == 0) {
                CafeReflectionUtils.getMethodValue(method, receiver);
            } else if (method.getParameterCount() == 1 && method.getParameterTypes()[0].equals(action.getClass())) {
                CafeReflectionUtils.getMethodValue(method, receiver, action);
            } else {
                throw new ActionBusException("Not supported arguments %s.".formatted(StringUtils.join(method.getParameterTypes(), ",")));
            }
        }
        return action;
    }


}
