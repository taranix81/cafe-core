package org.taranix.cafe.desktop.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.beans.annotations.methods.CafePostInit;
import org.taranix.cafe.desktop.annotations.CafeEventHandler;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@CafeSingleton
public class DefaultCafeEventHandlerHub implements CafeEventHandlerHub {

    private static final Logger log = LoggerFactory.getLogger(DefaultCafeEventHandlerHub.class);

    @CafeInject
    private Optional<org.taranix.cafe.desktop.datasource.DataSourceRegistry> dataSourceRegistry;

    private final List<WeakReference<Object>> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void register(Object listener) {
        listeners.add(new WeakReference<>(listener));
    }

    @Override
    public void send(CafeEvent event) {
        dispatchToAll(CafeEventHandler.class, event);
    }

    @Override
    public void sendMenuEvent(CafeMenuEvent event) {
        dispatchToAll(CafeEventHandler.class, event);
    }

    @Override
    public void sendDataSourceMoved(DataSourceMovedEvent event) {
        dispatchToAll(CafeEventHandler.class, event);
    }

    @Override
    public void sendComponentDirtyChanged(ComponentDirtyChangedEvent event) {
        dispatchToAll(CafeEventHandler.class, event);
    }

    private <A extends java.lang.annotation.Annotation> void dispatchToAll(
            Class<A> annotationType, Object event) {
        listeners.removeIf(ref -> ref.get() == null);
        listeners.stream()
                .map(WeakReference::get)
                .filter(Objects::nonNull)
                .forEach(listener -> invokeMatchingHandlers(listener, annotationType, event));
    }

    private <A extends java.lang.annotation.Annotation> void invokeMatchingHandlers(
            Object listener, Class<A> annotationType, Object event) {
        for (Method m : listener.getClass().getMethods()) {
            if (!m.isAnnotationPresent(annotationType)) continue;
            Class<?>[] params = m.getParameterTypes();
            if (params.length == 1 && params[0].isAssignableFrom(event.getClass())) {
                try {
                    m.invoke(listener, event);
                } catch (Exception e) {
                    log.error("Handler {}.{} threw during dispatch",
                            listener.getClass().getSimpleName(), m.getName(), e);
                }
            }
        }
    }

    @CafePostInit
    public void init() {
        dataSourceRegistry.ifPresent(registry ->
                registry.setMovedListener(this::sendDataSourceMoved));
    }
}
