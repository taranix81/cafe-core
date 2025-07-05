package org.taranix.cafe.graphics.services.file;

import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.graphics.annotations.CafeComponent;
import org.taranix.cafe.graphics.components.AbstractComponent;

import java.io.File;

@CafeService
@CafeComponent
public
class FileService extends AbstractComponent {

    private final FileReader reader;
    private final FileWriter writer;

    FileService(FileReader reader, FileWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    public String read(File file) {
        return new String(reader.read(file.toPath()));
    }

//    @CafeEventHandler
//    private void onEvent(FileServiceEvent event) {
//        if (event.getType().equals(CafeEventType.Read)) {
//            event.setData(new String(reader.read(event.getFile().toPath())));
//        }
//
//        if (event.getType().equals(CafeEventType.Write)) {
//            writer.write(event.getFile().toPath(), event.getData());
//        }
//    }

    public void write(File file, String text) {
        writer.write(file.toPath(), text);
    }
}
