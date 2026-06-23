package org.taranix.cafe.desktop.components.application.extensions;

import org.eclipse.swt.widgets.Shell;
import org.taranix.cafe.desktop.components.application.ApplicationComponent;

public interface ApplicationComponentConfigure {
    void configure(ApplicationComponent application);

    void configure(Shell shell);
}
