package org.taranix.cafe.desktop.datasource;

public interface DataSource<T> {

    T load();

    void save(T data);

    String getDisplayName();

    String getId();

    boolean isReadOnly();
}
