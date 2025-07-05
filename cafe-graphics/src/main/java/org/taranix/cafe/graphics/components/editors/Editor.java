package org.taranix.cafe.graphics.components.editors;

import org.taranix.cafe.graphics.components.viewers.DataView;

public interface Editor extends DataView {
    boolean isDirty();

    void save();
}
