package org.taranix.cafe.desktop.components.containers;

import java.util.UUID;

public record OpenComponent(UUID id, Class<?> type, String sourceId) {}
