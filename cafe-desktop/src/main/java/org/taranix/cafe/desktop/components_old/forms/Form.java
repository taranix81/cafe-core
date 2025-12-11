package org.taranix.cafe.desktop.components_old.forms;

import org.eclipse.swt.widgets.Widget;

public interface Form<T extends Widget> {
    T create(Widget parent);
}
