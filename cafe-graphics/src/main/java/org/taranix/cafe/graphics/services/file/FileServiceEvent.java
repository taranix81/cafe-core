package org.taranix.cafe.graphics.services.file;

import lombok.Getter;
import lombok.Setter;
import org.taranix.cafe.graphics.events.CafeEvent;
import org.taranix.cafe.graphics.events.CafeEventType;

import java.io.File;

@Getter
public class FileServiceEvent extends CafeEvent {

    private final File file;

    @Setter
    private String data;

    protected FileServiceEvent(CafeEventType type, File file, String data) {
        super(FileService.class, type);
        this.data = data;
        this.file = file;
    }

    public static FileServiceEvent write(File file, String data) {
        return new FileServiceEvent(CafeEventType.Write, file, data);
    }

    public static FileServiceEvent read(File file) {
        return new FileServiceEvent(CafeEventType.Read, file, null);
    }
}
