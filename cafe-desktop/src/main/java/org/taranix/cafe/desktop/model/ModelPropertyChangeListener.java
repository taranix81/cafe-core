package org.taranix.cafe.desktop.model;

@FunctionalInterface
public interface ModelPropertyChangeListener {
    void propertyChanged(String propertyName, Object newValue);
}
