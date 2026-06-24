package org.taranix.cafe.desktop.examples.notebook;

import org.taranix.cafe.beans.annotations.classes.CafeSingleton;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.beans.events.EventHub;
import org.taranix.cafe.desktop.components.containers.ctabfolder.CafeTabItemEvent;
import org.taranix.cafe.desktop.components.editors.StyledTextEditorComponent;
import org.taranix.cafe.desktop.components.editors.StyledTextEditorComponentExtension;

import java.nio.file.Path;

@CafeSingleton
public class NotebookEditorExtension implements StyledTextEditorComponentExtension {

    @CafeInject
    private EventHub eventHub;

    @Override
    public void onFileNameChanged(StyledTextEditorComponent component) {
        eventHub.send(buildEvent(component));
    }

    @Override
    public void onContentChanged(StyledTextEditorComponent component) {
        eventHub.send(buildEvent(component));
    }

    private CafeTabItemEvent buildEvent(StyledTextEditorComponent component) {
        Path path = component.getFilePath();
        String name = path != null ? path.getFileName().toString() : "Untitled";
        String title = component.isDirty() ? "* " + name : name;
        return CafeTabItemEvent.of(title, component);
    }
}
