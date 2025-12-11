package org.taranix.cafe.desktop.components_old.forms.tabfolder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;

//@CafeService
class DefaultTabFolderForm implements TabFolderForm {


    @Override
    public CTabFolder create(Widget parent) {
        return new CTabFolder((Composite) parent, SWT.CLOSE | SWT.BORDER);
    }
}
