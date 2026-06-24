package org.taranix.cafe.desktop.examples.notebook;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.desktop.components.containers.ctabfolder.CTabFolderContainer;
import org.taranix.cafe.desktop.components.containers.ctabfolder.CTabFolderFileExtension;
import org.taranix.cafe.desktop.components.editors.StyledTextEditorComponent;
import org.taranix.cafe.desktop.widgets.MessageBoxService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@CafeSingleton
public class NotebookFileExtension implements CTabFolderFileExtension {

    @CafeInject
    private MessageBoxService messageBox;

    private int untitledCount = 0;

    @Override
    public void newFile(CTabFolderContainer container) {
        container.openTab("Untitled-" + (++untitledCount), StyledTextEditorComponent.class);
    }

    @Override
    public void open(CTabFolderContainer container) {
        Display display = Display.getDefault();
        FileDialog dialog = new FileDialog(display.getActiveShell(), SWT.OPEN);
        dialog.setFilterExtensions(new String[]{"*.txt", "*.java", "*.xml", "*.md", "*.*"});
        dialog.setFilterNames(new String[]{
                "Text Files (*.txt)", "Java Files (*.java)",
                "XML Files (*.xml)", "Markdown (*.md)", "All Files (*.*)"
        });
        String chosen = dialog.open();
        if (chosen == null) return;

        Path path = Path.of(chosen);
        try {
            String content = Files.readString(path);
            StyledTextEditorComponent tab = container.openTab(path.getFileName().toString(), StyledTextEditorComponent.class);
            if (tab != null) {
                tab.setFilePath(path);
                tab.setContent(content);
            }
        } catch (IOException ex) {
            messageBox.showWarningDialog(display.getActiveShell(),
                    "Could not open file:\n" + ex.getMessage(), "Open Error");
        }
    }


}
