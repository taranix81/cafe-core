package org.taranix.cafe.desktop.components_old.datasource;

import lombok.Getter;
import org.eclipse.swt.widgets.Display;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.desktop.components_old.AbstractComponent;
import org.taranix.cafe.desktop.services.FileReader;
import org.taranix.cafe.desktop.services.FileWriter;
import org.taranix.cafe.desktop.widgets.MessageBoxService;

import java.nio.file.Path;

//@CafeService(scope = Scope.Prototype)
public class FileDataSource extends AbstractComponent implements DataSource<String> {

    private final FileReader reader;
    private final FileWriter writer;
    private Path path;

    @CafeInject
    private MessageBoxService messageBoxService;

    //    @CafeProperty(actionName = "cafe.application.container.newPageTitle")
    @Getter
    private String defaultNewTitle;

    public FileDataSource(FileReader reader, FileWriter writer) {
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
