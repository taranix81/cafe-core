package org.taranix.cafe.desktop.model;

public record CafePropertyDescriptor(String name, Class<?> type, boolean readOnly) {}
