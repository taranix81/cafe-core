package org.taranix.cafe.desktop.events;

import java.util.UUID;

public record ComponentDirtyChangedEvent(UUID componentId, boolean dirty, String displayName) {}
