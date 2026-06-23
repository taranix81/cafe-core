package org.taranix.cafe.desktop.components.menu.model;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;
import org.taranix.cafe.beans.events.EventHub;
import org.taranix.cafe.desktop.components.menu.MenuBarComponent;
import org.taranix.cafe.desktop.events.CafeMenuEvent;

@CafeSingleton
@Slf4j
public class MenuBuilder {

    private final EventHub eventHub;

    public MenuBuilder(EventHub eventHub) {
        this.eventHub = eventHub;
    }

    public Menu build(Shell shell, MenuModel model) {
        Menu bar = new Menu(shell, SWT.BAR);
        model.getItems().forEach(item -> buildTopLevelItem(item, bar));
        return bar;
    }

    private void buildTopLevelItem(MenuItemModel item, Menu bar) {
        if (item.isSeparator()) return;

        MenuItem cascade = new MenuItem(bar, SWT.CASCADE);
        if (item.getName() != null) cascade.setText(item.getName());

        if (item.getMenu() != null) {
            Menu dropdown = new Menu(bar.getShell(), SWT.DROP_DOWN);
            cascade.setMenu(dropdown);
            item.getMenu().getItems().forEach(sub -> buildMenuItem(sub, dropdown));
        }
    }

    private void buildMenuItem(MenuItemModel item, Menu parent) {
        if (item.isSeparator()) {
            new MenuItem(parent, SWT.SEPARATOR);
            return;
        }

        boolean hasSubmenu = item.getMenu() != null;
        MenuItem menuItem = new MenuItem(parent, hasSubmenu ? SWT.CASCADE : SWT.PUSH);
        if (item.getName() != null) menuItem.setText(item.getName());

        if (hasSubmenu) {
            Menu submenu = new Menu(parent.getShell(), SWT.DROP_DOWN);
            menuItem.setMenu(submenu);
            item.getMenu().getItems().forEach(sub -> buildMenuItem(sub, submenu));
        } else {
            String actionId = item.getAction() != null ? item.getAction() : item.getId();
            String menuId = item.getId();
            menuItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    try {
                        eventHub.send(CafeMenuEvent.of(menuId, e), MenuBarComponent.class);
                    } catch (Exception ex) {
                        log.error("Error dispatching menu event for action '{}'", menuId, ex);
                    }
                }
            });
        }
    }
}
