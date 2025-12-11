package org.taranix.cafe.desktop.components.menubar;

import lombok.AllArgsConstructor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Menu;
import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.desktop.actions.HandlersService;
import org.taranix.cafe.desktop.components.menubar.model.PropertiesMenuModel;

@CafeService
@AllArgsConstructor
class DefaultApplicationMenuBarComponent implements ApplicationMenuBarComponent {

    private final HandlersService handlersService;

    private PropertiesMenuModel propertiesMenuModel;

    @Override
    public Menu create(Control parent) {
        return MenuBuilder.build((Decorations) parent, SWT.BAR, propertiesMenuModel.getMenuBarModel(), handlersService);
    }


}
