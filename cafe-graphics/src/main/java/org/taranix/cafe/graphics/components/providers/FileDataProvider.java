package org.taranix.cafe.graphics.components.providers;

import lombok.Getter;
import org.eclipse.swt.widgets.Display;
import org.taranix.cafe.beans.annotations.CafeInject;
import org.taranix.cafe.beans.annotations.CafeProperty;
import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.beans.annotations.Scope;
import org.taranix.cafe.graphics.components.AbstractComponent;
import org.taranix.cafe.graphics.services.MessageBoxService;
import org.taranix.cafe.graphics.services.file.FileReader;
import org.taranix.cafe.graphics.services.file.FileWriter;

import java.nio.file.Path;

@CafeService(scope = Scope.Prototype)
public class FileDataProvider extends AbstractComponent implements DataProvider<String> {

    private final FileReader reader;
    private final FileWriter writer;
    private Path path;

    @CafeInject
    private MessageBoxService messageBoxService;

    @CafeProperty(name = "cafe.application.container.newPageTitle")
    @Getter
    private String defaultNewTitle;

    public FileDataProvider(FileReader reader, FileWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    public void setPath(Path p) {
        this.path = p;
    }


    public String getName() {
        if (path != null) {
            return path.getFileName().toString();
        }
        return defaultNewTitle;
    }

    @Override
    public boolean write(String data) {
        if (path == null) {
            //rid off swt elements - lets send event and wait for return
            path = messageBoxService.showSaveFileDialog(Display.getCurrent().getActiveShell());
        }

        if (path != null) {
            writer.write(path, data);
            return true;
        }

        return false;
    }

    @Override
    public String read() {
        if (path != null) {
            return new String(reader.read(path));
        }

        return "";
    }


}
