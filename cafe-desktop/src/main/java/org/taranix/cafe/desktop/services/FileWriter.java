package org.taranix.cafe.desktop.services;

import lombok.NonNull;
import org.taranix.cafe.beans.annotations.classes.CafeService;
import org.taranix.cafe.shell.exceptions.CafeCommandRuntimeException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

@CafeService
public class FileWriter {

    public void write(@NonNull Path path, @NonNull String content) {
        try {
            Files.write(path, content.getBytes());
        } catch (IOException e) {
            throw new CafeCommandRuntimeException(e);
        }
    }

    public void write(@NonNull Path path, @NonNull byte[] content) {
        try {
            Files.write(path, content);
        } catch (IOException e) {
            throw new CafeCommandRuntimeException(e);
        }
    }

    public void customWrite(@NonNull Path path, @NonNull byte[] content, OpenOption... options) {
        try {
            Files.write(path, content, options);
        } catch (IOException e) {
            throw new CafeCommandRuntimeException(e);
        }
    }


}
