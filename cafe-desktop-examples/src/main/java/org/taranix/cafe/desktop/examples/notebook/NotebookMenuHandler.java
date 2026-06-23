package org.taranix.cafe.desktop.examples.notebook;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.beans.annotations.methods.CafeHandler;
import org.taranix.cafe.beans.annotations.methods.CafePostInit;
import org.taranix.cafe.desktop.components.containers.ctabfolder.CTabFolderContainer;
import org.taranix.cafe.desktop.events.CafeMenuEvent;
import org.taranix.cafe.desktop.widgets.MessageBoxService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Handles application-level menu actions: New, Open, Exit.
 * Save/Save-As and edit actions are handled by the active TextEditorTab
 * via CTabFolderContainer's active-child event routing.
 */
//@CafeSingleton
public class NotebookMenuHandler {

    @CafeInject
    private CTabFolderContainer tabContainer;

    @CafeInject
    private Optional<MessageBoxService> messageBox;

    private int untitledCount = 0;

    @CafePostInit
    public void init() {
        newDocument();  // open one blank tab on startup
    }

    @CafeHandler
    public void onMenuEvent(CafeMenuEvent event) {
        switch (event.menuId()) {
            case "new" -> newDocument();
            case "open" -> openDocument();
            case "exit" -> Display.getDefault().getActiveShell().close();
        }
    }

    // ── Actions ───────────────────────────────────────────────────────

    private void newDocument() {
        String title = "Untitled-" + (++untitledCount);
        tabContainer.openTab(title, TextEditorTab.class);
    }

    private void openDocument() {
        Display display = Display.getDefault();
        messageBox.ifPresent(mb -> {
        });  // lazy init guard

        org.eclipse.swt.widgets.FileDialog dialog =
                new org.eclipse.swt.widgets.FileDialog(display.getActiveShell(), SWT.OPEN);
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
            String title = path.getFileName().toString();
            TextEditorTab tab = tabContainer.openTab(title, TextEditorTab.class);
            tab.setFilePath(path);
            tab.setContent(content);
        } catch (IOException ex) {
            messageBox.ifPresent(mb ->
                    mb.showWarningDialog(display.getActiveShell(),
                            "Could not open file:\n" + ex.getMessage(), "Open Error"));
        }
    }
}
