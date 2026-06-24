package org.taranix.cafe.desktop.components.editors;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.*;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.beans.annotations.methods.CafeHandler;
import org.taranix.cafe.desktop.annotations.CafeComponent;
import org.taranix.cafe.desktop.components.Component;
import org.taranix.cafe.desktop.components.Form;
import org.taranix.cafe.desktop.events.CafeMenuEvent;
import org.taranix.cafe.desktop.widgets.MessageBoxService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Per-tab form for the Notebook MDI editor.
 * Each tab gets its own instance with independent state (StyledText, file path, dirty flag).
 * <p>
 * Edit actions (cut/copy/paste/select-all) are routed here from CTabFolderContainer.onMenuEvent
 * because the container applies active-child-only routing for CafeMenuEvent.
 * <p>
 * Undo/redo are handled natively by StyledText keyboard bindings (Ctrl+Z / Ctrl+Y).
 */
@Slf4j
@CafeComponent
public class StyledTextEditorComponent implements Form, Component {

    private StyledText textWidget;
    @Getter
    private Path filePath;
    @Getter
    private boolean dirty;
    @CafeInject
    private MessageBoxService messageBoxService;
    @CafeInject
    private Optional<StyledTextEditorComponentExtension> extension;

    @Override
    public Widget create(Composite parent) {
        textWidget = new StyledText(parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        textWidget.setLeftMargin(4);
        textWidget.setRightMargin(4);
        applyMonospaceFont(parent);
        textWidget.addModifyListener(e -> onModified());
        return textWidget;
    }

    @CafeHandler
    public void onMenuEvent(CafeMenuEvent event) {
        if (textWidget == null || textWidget.isDisposed()) return;
        switch (event.getMenuId()) {
            case "file.save" -> save();
            case "file.save-as" -> saveAs();
            case "edit.cut" -> textWidget.invokeAction(ST.CUT);
            case "edit.copy" -> textWidget.invokeAction(ST.COPY);
            case "edit.paste" -> textWidget.invokeAction(ST.PASTE);
            case "edit.select-all" -> textWidget.selectAll();
        }
    }

    public void setContent(String content) {
        if (textWidget != null && !textWidget.isDisposed()) {
            textWidget.setText(content != null ? content : "");
            dirty = false;
        }
    }

    // ── Internals ──────────────────────────────────────────────────────

    public void setFilePath(Path path) {
        this.filePath = path;
        extension.ifPresent(e -> e.onFileNameChanged(this));
    }

    private void onModified() {
        if (!dirty) {
            dirty = true;
        }
        extension.ifPresent(e -> e.onContentChanged(this));
    }

    void save() {
        if (filePath == null) {
            saveAs();
            return;
        }
        writeToFile(filePath);
        extension.ifPresent(e -> e.onContentChanged(this));
    }

    void saveAs() {
        FileDialog dialog = new FileDialog(textWidget.getShell(), SWT.SAVE);
        dialog.setFilterExtensions(new String[]{"*.txt", "*.java", "*.xml", "*.*"});
        dialog.setFilterNames(new String[]{"Text Files", "Java Files", "XML Files", "All Files"});
        if (filePath != null) dialog.setFileName(filePath.getFileName().toString());
        String chosen = dialog.open();
        if (chosen != null) {
            setFilePath(Path.of(chosen));
            writeToFile(filePath);
            extension.ifPresent(e -> e.onContentChanged(this));
        }
    }

    private void writeToFile(Path path) {
        try {
            Files.writeString(path, textWidget.getText());
            dirty = false;
        } catch (IOException ex) {
            MessageBox mb = new MessageBox(textWidget.getShell(), SWT.ICON_ERROR | SWT.OK);
            mb.setText("Save Error");
            mb.setMessage("Could not save file:\n" + ex.getMessage());
            mb.open();
        }
    }

    private void applyMonospaceFont(Composite parent) {
        FontData[] current = parent.getDisplay().getSystemFont().getFontData();
        int height = current.length > 0 ? current[0].getHeight() : 10;
        Font mono = new Font(parent.getDisplay(), "Consolas", height, SWT.NORMAL);
        textWidget.setFont(mono);
        textWidget.addDisposeListener(e -> mono.dispose());
        textWidget.setLeftMargin(3);
        textWidget.setRightMargin(3);

    }


    @Override
    public void dispose() {
        if (dirty) {
            Shell shell = textWidget.getShell();
            if (shell == null || shell.isDisposed()) {
                return;
            }

            if (messageBoxService.showYesNoDialog(shell, "Do you want to save file?", "File not saved")) {
                save();
            }
        }
    }
}
