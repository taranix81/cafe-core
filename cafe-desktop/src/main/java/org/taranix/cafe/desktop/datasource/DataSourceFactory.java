package org.taranix.cafe.desktop.datasource;

import java.net.URL;
import java.nio.file.Path;

public interface DataSourceFactory {

    <T> FileDataSource<T> fromFile(Path path, DataSerializer<T> serializer);

    <T> UrlDataSource<T> fromUrl(URL url, DataSerializer<T> serializer);

    <T> NewDocumentSource<T> newDocument();
}
