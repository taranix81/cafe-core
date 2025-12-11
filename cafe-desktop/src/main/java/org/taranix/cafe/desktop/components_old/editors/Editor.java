package org.taranix.cafe.desktop.components_old.editors;

import org.taranix.cafe.desktop.components_old.Component;
import org.taranix.cafe.desktop.components_old.ViewComponent;
import org.taranix.cafe.desktop.components_old.viewers.Viewer;

public interface Editor<TData> extends Component, ViewComponent, Viewer<TData> {
    boolean isDirty();

    void save();
}
