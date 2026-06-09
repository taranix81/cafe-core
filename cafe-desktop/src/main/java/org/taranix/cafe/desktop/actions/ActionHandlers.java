package org.taranix.cafe.desktop.actions;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@CafeSingleton
public class ActionHandlers {

    private final Map<Class<? extends Action>, List<HandlerSignature>> handlers = new HashMap<>();

    public void add(Class<? extends Action> actionType, HandlerSignature signature) {
        handlers.computeIfAbsent(actionType, k -> new ArrayList<>()).add(signature);
        log.debug("Registered action handler {} for {}", signature.handlingMethod().getName(), actionType.getSimpleName());
    }

    public List<HandlerSignature> get(Class<? extends Action> actionType) {
        return handlers.getOrDefault(actionType, List.of());
    }
}
