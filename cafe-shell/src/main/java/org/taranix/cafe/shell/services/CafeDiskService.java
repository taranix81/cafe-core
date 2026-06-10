package org.taranix.cafe.shell.services;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@CafeSingleton
@Slf4j
public class CafeDiskService {

    public void createDirectories(Path path) {
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            log.error("Failed to create directories: {}", path, e);
        }
    }

    public void createDirectories(String path) {
        createDirectories(Path.of(path));
    }

    public void write(Path path, String content) {
        try {
            Files.writeString(path, content);
        } catch (IOException e) {
            log.error("Failed to write file: {}", path, e);
        }
    }

    public void write(String path, String content) {
        write(Path.of(path), content);
    }

    public void append(Path path, String content) {
        try {
            Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            log.error("Failed to append to file: {}", path, e);
        }
    }

    public void append(String path, String content) {
        append(Path.of(path), content);
    }

    public boolean exists(Path path) {
        return Files.exists(path);
    }

    public boolean exists(String path) {
        return Files.exists(Path.of(path));
    }

    public void delete(Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            log.error("Failed to delete: {}", path, e);
        }
    }

    public void delete(String path) {
        delete(Path.of(path));
    }
}
