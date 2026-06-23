package org.taranix.cafe.desktop.datasource;

import org.taranix.cafe.beans.annotations.classes.CafeSingleton;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.beans.annotations.methods.CafePostInit;
import org.taranix.cafe.beans.events.EventHub;
import org.taranix.cafe.desktop.events.DataSourceMovedEvent;

import java.util.Optional;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@CafeSingleton
public class DataSourceRegistry {

    private final Map<String, WeakReference<DataSource<?>>> registry = new ConcurrentHashMap<>();
    private Consumer<DataSourceMovedEvent> movedListener = ignored -> {};

    @CafeInject
    private Optional<EventHub> eventHub;

    @CafePostInit
    public void init() {
        eventHub.ifPresent(hub -> setMovedListener(hub::send));
    }

    public DataSource<?> register(DataSource<?> source) {
        registry.put(source.getId(), new WeakReference<>(source));
        return source;
    }

    public DataSource<?> get(String sourceId) {
        WeakReference<DataSource<?>> ref = registry.get(sourceId);
        if (ref == null) return null;
        DataSource<?> source = ref.get();
        if (source == null) registry.remove(sourceId);
        return source;
    }

    public void remove(String sourceId) {
        registry.remove(sourceId);
    }

    public boolean isOpen(String sourceId) {
        return get(sourceId) != null;
    }

    public Collection<DataSource<?>> all() {
        return registry.values().stream()
                .map(WeakReference::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public void setMovedListener(Consumer<DataSourceMovedEvent> listener) {
        this.movedListener = listener;
    }

    void notifyMoved(String sourceId, String newDisplayName) {
        movedListener.accept(new DataSourceMovedEvent(sourceId, newDisplayName));
    }
}
