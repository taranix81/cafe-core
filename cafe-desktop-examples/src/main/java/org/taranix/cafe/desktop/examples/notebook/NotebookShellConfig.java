package org.taranix.cafe.desktop.examples.notebook;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.desktop.components.ComponentFactory;
import org.taranix.cafe.desktop.components.application.ApplicationComponent;
import org.taranix.cafe.desktop.components.application.extensions.ApplicationComponentConfigure;
import org.taranix.cafe.desktop.components.containers.ctabfolder.CTabFolderContainer;

/**
 * Configures the shell: window size and places the tab container.
 */
@CafeSingleton
public class NotebookShellConfig implements ApplicationComponentConfigure {

    @CafeInject
    private ComponentFactory factory;

    @Override
    public void configure(ApplicationComponent application) {

    }

    @Override
    public void configure(Shell shell) {
        factory.create(CTabFolderContainer.class, shell);
        centerOnPrimaryMonitor(shell, 1024, 720);
    }

    private void centerOnPrimaryMonitor(Shell shell, int width, int height) {
        Monitor primary = shell.getDisplay().getPrimaryMonitor();
        Rectangle screen = primary.getBounds();
        int x = screen.x + (screen.width - width) / 2;
        int y = screen.y + (screen.height - height) / 2;
        shell.setBounds(x, y, width, height);
    }
}
