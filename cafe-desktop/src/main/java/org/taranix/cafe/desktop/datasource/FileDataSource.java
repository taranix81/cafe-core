package org.taranix.cafe.desktop.datasource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Consumer;

public class FileDataSource<T> implements DataSource<T> {

    private final String id;
    private final DataSerializer<T> serializer;
    private Path path;
    private Consumer<String> onMoved = ignored -> {};

    FileDataSource(Path path, DataSerializer<T> serializer) {
        this.id = UUID.randomUUID().toString();
        this.path = path;
        this.serializer = serializer;
    }

    @Override
    public T load() {
        try {
            byte[] bytes = Files.readAllBytes(path);
            return serializer.deserialize(bytes);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load: " + path, e);
        }
    }

    @Override
    public void save(T data) {
        try {
            Files.write(path, serializer.serialize(data));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to save: " + path, e);
        }
    }

    @Override
    public String getDisplayName() {
        return path.getFileName().toString();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isReadOnly() {
        return !Files.isWritable(path);
    }

    public Path getPath() {
        return path;
    }

    public void moveTo(Path newPath) {
        this.path = newPath;
        onMoved.accept(id); // Phase 7: wired by DataSourceFactory to fire DataSourceMovedEvent
    }

    void setOnMoved(Consumer<String> onMoved) {
        this.onMoved = onMoved;
    }
}
