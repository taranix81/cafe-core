package org.taranix.cafe.desktop.components.containers;

import org.taranix.cafe.beans.annotations.classes.CafeSingleton;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@CafeSingleton
public class CafeComponentRegistry {

    private final Set<Class<?>> knownComponentClasses = ConcurrentHashMap.newKeySet();
    private final Map<UUID, OpenComponent> openComponents = new ConcurrentHashMap<>();
    private final Map<String, UUID> sourceIndex = new ConcurrentHashMap<>();
    private volatile UUID activeComponentId;

    public void registerComponentClass(Class<?> componentClass) {
        knownComponentClasses.add(componentClass);
    }

    public boolean isKnown(Class<?> componentClass) {
        return knownComponentClasses.contains(componentClass);
    }

    public UUID open(Class<?> type, String sourceId) {
        UUID id = UUID.randomUUID();
        openComponents.put(id, new OpenComponent(id, type, sourceId));
        if (sourceId != null) sourceIndex.put(sourceId, id);
        return id;
    }

    public void close(UUID componentId) {
        OpenComponent removed = openComponents.remove(componentId);
        if (removed != null && removed.sourceId() != null) {
            sourceIndex.remove(removed.sourceId());
        }
        if (componentId.equals(activeComponentId)) activeComponentId = null;
    }

    public boolean isOpen(String sourceId) {
        UUID id = sourceIndex.get(sourceId);
        return id != null && openComponents.containsKey(id);
    }

    public Optional<UUID> findBySourceId(String sourceId) {
        return Optional.ofNullable(sourceIndex.get(sourceId));
    }

    public void setActive(String sourceId) {
        UUID id = sourceIndex.get(sourceId);
        if (id != null) activeComponentId = id;
    }

    public void setActive(UUID componentId) {
        if (openComponents.containsKey(componentId)) activeComponentId = componentId;
    }

    public <T> java.util.List<OpenComponent> getOpen(Class<T> type) {
        return openComponents.values().stream()
                .filter(c -> type.isAssignableFrom(c.type()))
                .collect(Collectors.toList());
    }

    public Optional<OpenComponent> getActive() {
        if (activeComponentId == null) return Optional.empty();
        return Optional.ofNullable(openComponents.get(activeComponentId));
    }

    public <T> Optional<OpenComponent> getActive(Class<T> type) {
        return getActive()
                .filter(c -> type.isAssignableFrom(c.type()));
    }
}
