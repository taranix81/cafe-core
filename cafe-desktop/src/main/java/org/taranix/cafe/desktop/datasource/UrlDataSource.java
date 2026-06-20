package org.taranix.cafe.desktop.datasource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;

public class UrlDataSource<T> implements DataSource<T> {

    private final URL url;
    private final DataSerializer<T> serializer;

    UrlDataSource(URL url, DataSerializer<T> serializer) {
        this.url = url;
        this.serializer = serializer;
    }

    @Override
    public T load() {
        try {
            byte[] bytes = url.openStream().readAllBytes();
            return serializer.deserialize(bytes);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load: " + url, e);
        }
    }

    @Override
    public void save(T data) {
        throw new UnsupportedOperationException("URL data sources are read-only");
    }

    @Override
    public String getDisplayName() {
        return url.toString();
    }

    @Override
    public String getId() {
        return url.toString();
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }
}
