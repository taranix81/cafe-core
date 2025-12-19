package org.taranix.cafe.desktop.components.containers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;
import org.taranix.cafe.beans.annotations.classes.CafeService;
import org.taranix.cafe.desktop.components.Component;

@CafeService
public class CTabFolderComponent implements Component {


    @Override
    public Widget create(Control parent) {
        return new CTabFolder((Composite) parent, SWT.CLOSE | SWT.BORDER);
    }
}
