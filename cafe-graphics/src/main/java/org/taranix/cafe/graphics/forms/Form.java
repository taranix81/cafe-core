package org.taranix.cafe.graphics.forms;

import org.eclipse.swt.widgets.Widget;

public interface Form<T extends Widget> {
    T create(Widget parent);
}
