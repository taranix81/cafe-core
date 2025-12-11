package org.taranix.cafe.desktop.components_old.forms.table;

import org.eclipse.swt.graphics.Color;

public interface TableConfig {
    boolean isHeaderVisible();

    boolean isLinesVisible();

    Color getHeaderBackground();

    int getTableStyle();

    boolean isHideRowSelection();

    boolean isCellEditable();
}
