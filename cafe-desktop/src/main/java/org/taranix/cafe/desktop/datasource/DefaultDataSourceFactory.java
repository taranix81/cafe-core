package org.taranix.cafe.desktop.datasource;

import org.taranix.cafe.beans.annotations.classes.CafeSingleton;
import org.taranix.cafe.beans.annotations.fields.CafeInject;

import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

@CafeSingleton
class DefaultDataSourceFactory implements DataSourceFactory {

    @CafeInject
    private Optional<DataSourceRegistry> registry;

    @Override
    public <T> FileDataSource<T> fromFile(Path path, DataSerializer<T> serializer) {
        FileDataSource<T> source = new FileDataSource<>(path, serializer);
        registry.ifPresent(r -> {
            r.register(source);
            source.setOnMoved(sourceId -> r.notifyMoved(sourceId, source.getDisplayName()));
        });
        return source;
    }

    @Override
    public <T> UrlDataSource<T> fromUrl(URL url, DataSerializer<T> serializer) {
        UrlDataSource<T> source = new UrlDataSource<>(url, serializer);
        registry.ifPresent(r -> r.register(source));
        return source;
    }

    @Override
    public <T> NewDocumentSource<T> newDocument() {
        NewDocumentSource<T> source = new NewDocumentSource<>();
        registry.ifPresent(r -> r.register(source));
        return source;
    }
}
