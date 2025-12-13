package org.taranix.cafe.desktop.components_old.events;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Widget;
import org.taranix.cafe.beans.CafeReflectionUtils;
import org.taranix.cafe.beans.annotations.CafeAnnotationUtils;
import org.taranix.cafe.desktop.components_old.Component;
import org.taranix.cafe.desktop.components_old.ViewComponent;
import org.taranix.cafe.desktop.components_old.events.annotations.CafeEventHandler;
import org.taranix.cafe.desktop.components_old.events.annotations.CafeMessageHandler;
import org.taranix.cafe.desktop.components_old.events.exceptions.CafeEventBusException;
import org.taranix.cafe.desktop.components_old.events.matchers.EventHandlerMatcher;
import org.taranix.cafe.desktop.components_old.events.matchers.MessageHandlerMatcher;
import org.taranix.cafe.desktop.components_old.events.messages.Message;
import org.taranix.cafe.desktop.components_old.forms.WidgetConfig;
import org.taranix.cafe.desktop.components_old.viewers.AbstractViewComponent;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

//@CafeService
@Slf4j
public class EventBus {

    private final Set<EventHandlerMatcher> eventHandlerMatchers;
    private final Set<MessageHandlerMatcher> messageHandlerMatchers;

    private final Set<Component> components = new HashSet<>();

    public EventBus(Set<EventHandlerMatcher> eventHandlerMatchers, Set<MessageHandlerMatcher> messageHandlerMatchers) {
        this.eventHandlerMatchers = eventHandlerMatchers;
        this.messageHandlerMatchers = messageHandlerMatchers;
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

        if (component instanceof ViewComponent viewComponent) {
            register(viewComponent.getWidget());
            WidgetConfig.setComponent(viewComponent.getWidget(), viewComponent);
        }

    }

    public void unregister(Component component) {
        components.remove(component);

        if (component instanceof AbstractViewComponent abstractViewComponent) {
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

    private boolean isTargetMatch(Component component, Message message) {
        if (message.getTarget() != null) {
            return message.getTarget() != null && message.getTarget().equals(component.getId());
        }
        return true;
    }


    private List<Method> getHandlers(Component component, Message message) {
        List<Method> handlerMethods = CafeAnnotationUtils.getClassMethodsAnnotatedBy(component.getClass(), CafeMessageHandler.class);
        return handlerMethods.stream()
                .filter(method -> isAllMatch(message, method))
                .toList();
    }

    private List<Method> getHandlers(Component component, Event event) {
        List<Method> handlerMethods = CafeAnnotationUtils.getClassMethodsAnnotatedBy(component.getClass(), CafeEventHandler.class);
        return handlerMethods.stream()
                .filter(method -> isAllMatch(event, method))
                .toList();
    }

    private boolean isAllMatch(Event event, Method method) {
        return eventHandlerMatchers.stream()
                .allMatch(eventHandlerMatcher -> eventHandlerMatcher.isMatch(method, event));
    }

    private boolean isAllMatch(Message message, Method method) {
        return messageHandlerMatchers.stream()
                .allMatch(eventHandlerMatcher -> eventHandlerMatcher.isMatch(method, message));
    }

    private void executeHandler(Component component, Event event, Method method) {
        if (method.getParameterCount() == 0) {
            CafeReflectionUtils.getMethodValue(method, component);
            return;
        }
        if (method.getParameterCount() == 1 && method.getParameterTypes()[0].equals(Event.class)) {
            CafeReflectionUtils.getMethodValue(method, component, event);
            return;
        }
        throw new CafeEventBusException("Not supported arguments %s.".formatted(StringUtils.join(method.getParameterTypes(), ",")));
    }

    private void executeHandler(Component component, Message message, Method method) {
        if (message.isProcessed()) {
            return;
        }
        CafeReflectionUtils.getMethodValue(method, component, message);
    }


    public void refresh(Component component) {
        unregister(component);
        register(component);
    }

    public Message sendMessage(Message message) {
        if (message.isProcessed()) {
            log.warn("Message already consumed {}", message);
            return message;
        }

        //We must take snapshot of current listeners
        //During processing message a new listener can be added
        Set<Component> currentListeners = new HashSet<>(components)
                .stream().filter(component -> isTargetMatch(component, message))
                .collect(Collectors.toSet());

        for (Component component : currentListeners) {
            List<Method> matchedHandlers = getHandlers(component, message);

            matchedHandlers.forEach(method ->
                    executeHandler(component, message, method)
            );
        }
        return message;
    }
}
