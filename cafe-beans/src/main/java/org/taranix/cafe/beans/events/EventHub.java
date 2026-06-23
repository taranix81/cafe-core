package org.taranix.cafe.beans.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;
import org.taranix.cafe.beans.annotations.methods.CafeHandler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Runtime event bus backed by a {@link WeakHashMap} of registered listeners.
 * Listeners are held weakly — they are automatically removed when no strong reference exists.
 *
 * <h2>Registering a listener</h2>
 * Call {@link #register(Object)} on any object. EventHub scans all methods
 * (any visibility, including inherited) annotated with {@link CafeHandler} and stores them as eligible handler methods.
 *
 * <h2>Handler method rules</h2>
 * A method is eligible if it carries {@code @CafeHandler} and has exactly one parameter
 * whose type is a {@link CafeEvent} subclass.
 *
 * <h2>Matching rules — all three conditions must hold</h2>
 * <ol>
 *   <li><b>Exact event type</b> — the method's parameter type must be exactly equal to the
 *       runtime type of the sent event ({@code param.equals(event.getClass())}).
 *       Subtypes of the declared parameter are <em>not</em> matched.</li>
 *   <li><b>Id equality</b> — {@code @CafeHandler.id()} must equal {@link CafeEvent#id()}.
 *       Both default to {@code ""}, so a bare {@code @CafeHandler} on a method and
 *       {@code new MyEvent()} (no explicit id) match each other.</li>
 *   <li><b>Target type</b> (optional) — when using {@link #send(CafeEvent, Class)},
 *       only listeners that are instances of the given {@code targetType} are considered.</li>
 * </ol>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * // event
 * public class OrderPlacedEvent extends CafeEvent {
 *     public static final String ID = "ORDER_PLACED";
 *     public OrderPlacedEvent() { super(ID); }
 * }
 *
 * // listener (registered via eventHub.register(this))
 * @CafeHandler(id = OrderPlacedEvent.ID)
 * public void onOrderPlaced(OrderPlacedEvent event) { ... }
 *
 * // dispatch — matches the handler above
 * eventHub.send(new OrderPlacedEvent());
 *
 * // targeted dispatch — only reaches listeners that are instances of CheckoutService
 * eventHub.send(new OrderPlacedEvent(), CheckoutService.class);
 * }</pre>
 */
@CafeSingleton
public class EventHub {

    private static final Logger log = LoggerFactory.getLogger(EventHub.class);

    private final WeakHashMap<Object, List<Method>> listeners = new WeakHashMap<>();

    public synchronized void register(Object listener) {
        listeners.put(listener, discoverHandlers(listener));
    }

    public synchronized void unregister(Object listener) {
        listeners.remove(listener);
    }

    public void send(CafeEvent event) {
        snapshot().forEach((listener, methods) ->
                methods.forEach(method -> tryInvoke(listener, method, event)));
    }

    public void send(CafeEvent event, Class<?> targetType) {
        snapshot().entrySet().stream()
                .filter(e -> targetType.isInstance(e.getKey()))
                .forEach(e -> e.getValue().forEach(method -> tryInvoke(e.getKey(), method, event)));
    }

    public void send(CafeEvent event, Object targetInstance) {
        List<Method> methods;
        synchronized (this) {
            List<Method> found = listeners.get(targetInstance);
            methods = found != null ? new ArrayList<>(found) : null;
        }
        if (methods != null) {
            methods.forEach(method -> tryInvoke(targetInstance, method, event));
        }
    }

    private synchronized Map<Object, List<Method>> snapshot() {
        return new LinkedHashMap<>(listeners);
    }

    private List<Method> discoverHandlers(Object listener) {
        List<Method> handlers = new ArrayList<>();
        Class<?> clazz = listener.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(CafeHandler.class)) {
                    method.setAccessible(true);
                    handlers.add(method);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return handlers;
    }

    private void tryInvoke(Object listener, Method method, CafeEvent event) {
        if (!matches(method, event)) return;
        try {
            method.invoke(listener, event);
        } catch (Exception ex) {
            log.error("Handler {}.{} failed during dispatch",
                    listener.getClass().getSimpleName(), method.getName(), ex);
        }
    }

    private boolean matches(Method method, CafeEvent event) {
        CafeHandler ann = method.getAnnotation(CafeHandler.class);
        if (ann == null) return false;
        Class<?>[] params = method.getParameterTypes();
        if (params.length != 1 || !params[0].equals(event.getClass())) return false;
        return ann.id().equals(event.id());
    }
}
