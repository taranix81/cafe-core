package org.taranix.cafe.graphics.forms.shell;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.taranix.cafe.beans.annotations.CafeService;

@CafeService
class DefaultShellForm implements ShellForm {


    @Override
    public Shell create(Widget parent) {
        Shell shell = new Shell(Display.getDefault());
        shell.setLayout(new FillLayout());
        return shell;
    }
}
