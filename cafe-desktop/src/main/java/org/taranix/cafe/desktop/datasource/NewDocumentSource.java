package org.taranix.cafe.desktop.datasource;

import java.util.UUID;

public class NewDocumentSource<T> implements DataSource<T> {

    private final String id;
    private T data;

    NewDocumentSource() {
        this.id = UUID.randomUUID().toString();
    }

    @Override
    public T load() {
        return data;
    }

    @Override
    public void save(T data) {
        this.data = data;
    }

    @Override
    public String getDisplayName() {
        return "Untitled";
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }
}
