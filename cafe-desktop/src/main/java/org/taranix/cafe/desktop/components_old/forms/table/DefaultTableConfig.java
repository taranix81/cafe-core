package org.taranix.cafe.desktop.components_old.forms.table;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

//@CafeService
public class DefaultTableConfig implements TableConfig {
    @Override
    public boolean isHeaderVisible() {
        return true;
    }

    @Override
    public boolean isLinesVisible() {
        return true;
    }

    @Override
    public Color getHeaderBackground() {
        return Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
    }

    @Override
    public int getTableStyle() {
        return SWT.BORDER | SWT.VIRTUAL | SWT.V_SCROLL | SWT.H_SCROLL;
    }

    @Override
    public boolean isHideRowSelection() {
        return true;
    }

    @Override
    public boolean isCellEditable() {
        return true;
    }
}
