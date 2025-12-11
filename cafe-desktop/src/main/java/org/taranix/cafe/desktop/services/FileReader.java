package org.taranix.cafe.desktop.services;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.shell.exceptions.CafeCommandRuntimeException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


@CafeService
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileReader {


    public byte[] read(@NonNull Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new CafeCommandRuntimeException(e);
        }

    }

    public List<String> readLines(final Path path) {
        try {
            return Files.readAllLines(path);
        } catch (IOException e) {
            throw new CafeCommandRuntimeException(e);
        }
    }
}
