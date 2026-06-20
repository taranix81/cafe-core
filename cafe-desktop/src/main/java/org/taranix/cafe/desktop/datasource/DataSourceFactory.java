package org.taranix.cafe.desktop.datasource;

import org.taranix.cafe.desktop.annotations.CafeService;

import java.net.URL;
import java.nio.file.Path;

@CafeService
public interface DataSourceFactory {

    <T> FileDataSource<T> fromFile(Path path, DataSerializer<T> serializer);

    <T> UrlDataSource<T> fromUrl(URL url, DataSerializer<T> serializer);

    <T> NewDocumentSource<T> newDocument();
}
