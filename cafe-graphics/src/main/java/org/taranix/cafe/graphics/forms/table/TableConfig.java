package org.taranix.cafe.graphics.forms.table;

import org.eclipse.swt.graphics.Color;

public interface TableConfig {
    boolean isHeaderVisible();

    boolean isLinesVisible();

    Color getHeaderBackground();

    int getTableStyle();

    boolean isHideRowSelection();

    boolean isCellEditable();
}
