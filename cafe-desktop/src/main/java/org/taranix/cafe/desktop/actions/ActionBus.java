package org.taranix.cafe.desktop.actions;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.beans.reflection.CafeReflectionUtils;

@Slf4j
@CafeSingleton
public class ActionBus {

    @CafeInject
    private ActionHandlers actionHandlers;

    public <T extends Action> T broadcast(T action) {
        Class<? extends Action> type = action.getClass();
        actionHandlers.get(type).forEach(sig -> invoke(sig, action));
        return action;
    }

    public <T extends Action> T broadcast(T action, Class<?> receiverType) {
        Class<? extends Action> type = action.getClass();
        actionHandlers.get(type).stream()
                .filter(sig -> sig.handlerInstance().getClass().equals(receiverType))
                .forEach(sig -> invoke(sig, action));
        return action;
    }

    private <T extends Action> void invoke(HandlerSignature sig, T action) {
        if (sig.handlingMethod().getParameterCount() == 0) {
            CafeReflectionUtils.getMethodValue(sig.handlingMethod(), sig.handlerInstance());
        } else {
            CafeReflectionUtils.getMethodValue(sig.handlingMethod(), sig.handlerInstance(), action);
        }
    }
}
