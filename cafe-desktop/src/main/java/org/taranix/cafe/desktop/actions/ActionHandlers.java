package org.taranix.cafe.desktop.actions;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ActionHandlers {

    private Map<Class<? extends Action>, List<Method>> handlers = new HashMap<>();

    public void add(Class<? extends Action> actionType, Method method) {

    }

}
