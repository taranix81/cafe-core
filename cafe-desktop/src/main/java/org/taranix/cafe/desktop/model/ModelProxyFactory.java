package org.taranix.cafe.desktop.model;

public interface ModelProxyFactory {

    <T> T createProxy(T model, ModelPropertyChangeListener listener);

    <T> T createProxy(Class<T> modelClass, ModelPropertyChangeListener listener);
}
