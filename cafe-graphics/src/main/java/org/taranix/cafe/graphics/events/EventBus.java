package org.taranix.cafe.graphics.events;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Widget;
import org.taranix.cafe.beans.CafeReflectionUtils;
import org.taranix.cafe.beans.annotations.CafeAnnotationUtils;
import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.graphics.components.AbstractView;
import org.taranix.cafe.graphics.components.Component;
import org.taranix.cafe.graphics.components.View;
import org.taranix.cafe.graphics.events.annotations.CafeEventHandler;
import org.taranix.cafe.graphics.events.annotations.SwtEventHandler;
import org.taranix.cafe.graphics.events.exceptions.CafeEventBusException;
import org.taranix.cafe.graphics.events.matchers.CafeEventHandlerMatcher;
import org.taranix.cafe.graphics.events.matchers.EventHandlerMatcher;
import org.taranix.cafe.graphics.events.matchers.MessageHandlerMatcher;
import org.taranix.cafe.graphics.events.messages.CafeOldEvent;
import org.taranix.cafe.graphics.forms.WidgetConfig;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CafeService
@Slf4j
public class EventBus {

    private final Set<EventHandlerMatcher> eventHandlerMatchers;
    private final Set<MessageHandlerMatcher> messageHandlerMatchers;

    private final Set<CafeEventHandlerMatcher> cafeEventHandlerMatchers;

    private final Set<Component> components = new HashSet<>();

    public EventBus(Set<EventHandlerMatcher> eventHandlerMatchers, Set<MessageHandlerMatcher> messageHandlerMatchers, Set<CafeEventHandlerMatcher> cafeEventHandlerMatchers) {
        this.eventHandlerMatchers = eventHandlerMatchers;
        this.messageHandlerMatchers = messageHandlerMatchers;
        this.cafeEventHandlerMatchers = cafeEventHandlerMatchers;
    }

    private void register(Widget widget) {

        Arrays.stream(SWTEventType.values())
                .forEach(swtEventType -> widget.addListener(swtEventType.getSwtEventType(), this::onEvent));
        if (widget instanceof Menu menu) {
            registerMenuItems(menu);
        }

        if (widget instanceof CTabFolder folder) {
            Arrays.stream(folder.getItems()).forEach(this::register);
        }

    }

    private void registerMenuItems(Menu menu) {
        Arrays.stream(menu.getItems()).forEach(menuItem -> {
            register(menuItem);
            if (menuItem.getMenu() != null) {
                registerMenuItems(menuItem.getMenu());
            }
        });
    }

    private void unregisterMenuItems(Menu menu) {
        Arrays.stream(menu.getItems()).forEach(menuItem -> {
            unregister(menuItem);
            if (menuItem.getMenu() != null) {
                unregisterMenuItems(menuItem.getMenu());
            }
        });
    }

    private void unregister(Widget widget) {
        Arrays.stream(SWTEventType.values())
                .forEach(swtEventType -> widget.removeListener(swtEventType.getSwtEventType(), this::onEvent));

        if (widget instanceof Menu menu) {
            unregisterMenuItems(menu);
        }

        if (widget instanceof CTabFolder tabFolder) {
            Arrays.stream(tabFolder.getItems()).forEach(this::unregister);
        }
    }

    public void register(Component component) {
        components.add(component);

        if (component instanceof View view) {
            register(view.getWidget());
            WidgetConfig.setComponent(view.getWidget(), view);
        }

    }

    public void unregister(Component component) {
        components.remove(component);

        if (component instanceof AbstractView abstractViewComponent) {
            unregister(abstractViewComponent.getWidget());
        }
    }

    private void onEvent(Event event) {
        Widget widget = event.widget;
        if (widget.isDisposed()) {
            event.doit = false;
            return;
        }
        Component component = WidgetConfig.getComponent(widget);
        if (component != null) {
            List<Method> handlers = getHandlers(component, event);
            if (!handlers.isEmpty()) {
                handlers.forEach(method -> executeHandler(component, event, method));
                log.debug("Processed :{}", event);
            }
        }
    }

    private boolean isTargetMatch(Component component, CafeOldEvent cafeOldEvent) {
        if (cafeOldEvent.getTarget() != null) {
            return cafeOldEvent.getTarget() != null && cafeOldEvent.getTarget().equals(component.getId());
        }
        return true;
    }


    private boolean isTargetMatch(Component component, CafeEvent cafeEvent) {
        return cafeEvent.getTarget().isAssignableFrom(component.getClass()) &&
                (cafeEvent.getTargetId() == null || cafeEvent.getTargetId().equals(component.getId()));
    }

    private List<Method> getHandlers(Component component, CafeOldEvent cafeOldEvent) {
        List<Method> handlerMethods = CafeAnnotationUtils.getMethodsAnnotatedBy(component.getClass(), CafeEventHandler.class);
        return handlerMethods.stream()
                .filter(method -> isAllMatch(cafeOldEvent, method))
                .toList();
    }

    private List<Method> getHandlers(Component component, org.eclipse.swt.widgets.Event event) {
        List<Method> handlerMethods = CafeAnnotationUtils.getMethodsAnnotatedBy(component.getClass(), SwtEventHandler.class);
        return handlerMethods.stream()
                .filter(method -> isAllMatch(event, method))
                .toList();
    }

    private List<Method> getHandlers(Component component, CafeEvent event) {
        List<Method> handlerMethods = CafeAnnotationUtils.getMethodsAnnotatedBy(component.getClass(), CafeEventHandler.class);
        return handlerMethods.stream()
                .filter(method -> isAllMatch(event, method))
                .toList();
    }


    private boolean isAllMatch(org.eclipse.swt.widgets.Event event, Method method) {
        return eventHandlerMatchers.stream()
                .allMatch(eventHandlerMatcher -> eventHandlerMatcher.isMatch(method, event));
    }

    private boolean isAllMatch(CafeOldEvent cafeOldEvent, Method method) {
        return messageHandlerMatchers.stream()
                .allMatch(eventHandlerMatcher -> eventHandlerMatcher.isMatch(method, cafeOldEvent));
    }

    private boolean isAllMatch(CafeEvent cafeEvent, Method method) {
        return cafeEventHandlerMatchers.stream()
                .allMatch(eventHandlerMatcher -> eventHandlerMatcher.isMatch(method, cafeEvent));
    }

    private void executeHandler(Component component, org.eclipse.swt.widgets.Event event, Method method) {
        if (method.getParameterCount() == 0) {
            CafeReflectionUtils.getMethodValue(method, component);
            return;
        }
        if (method.getParameterCount() == 1 && method.getParameterTypes()[0].equals(org.eclipse.swt.widgets.Event.class)) {
            CafeReflectionUtils.getMethodValue(method, component, event);
            return;
        }
        throw new CafeEventBusException("Not supported arguments %s.".formatted(StringUtils.join(method.getParameterTypes(), ",")));
    }

    private void executeHandler(Component component, CafeOldEvent cafeOldEvent, Method method) {
        if (cafeOldEvent.isProcessed()) {
            return;
        }
        CafeReflectionUtils.getMethodValue(method, component, cafeOldEvent);
    }

    private void executeHandler(Component component, CafeEvent cafeEvent, Method method) {
        CafeReflectionUtils.getMethodValue(method, component, cafeEvent);
    }


    public void refresh(Component component) {
        unregister(component);
        register(component);
    }

    public CafeEvent sendMessage(CafeEvent event) {

        //We must take snapshot of current listeners
        //During processing message a new listener can be added
        Set<Component> currentListeners = new HashSet<>(components)
                .stream()
                .filter(component -> isTargetMatch(component, event))
                .collect(Collectors.toSet());

        for (Component component : currentListeners) {
            List<Method> matchedHandlers = getHandlers(component, event);

            matchedHandlers.forEach(method ->
                    executeHandler(component, event, method)
            );
        }
        return event;
    }

    public CafeOldEvent sendMessage(CafeOldEvent cafeOldEvent) {
        if (cafeOldEvent.isProcessed()) {
            log.warn("Message already consumed {}", cafeOldEvent);
            return cafeOldEvent;
        }

        //We must take snapshot of current listeners
        //During processing message a new listener can be added
        Set<Component> currentListeners = new HashSet<>(components)
                .stream().filter(component -> isTargetMatch(component, cafeOldEvent))
                .collect(Collectors.toSet());

        for (Component component : currentListeners) {
            List<Method> matchedHandlers = getHandlers(component, cafeOldEvent);

            matchedHandlers.forEach(method ->
                    executeHandler(component, cafeOldEvent, method)
            );
        }
        return cafeOldEvent;
    }
}
