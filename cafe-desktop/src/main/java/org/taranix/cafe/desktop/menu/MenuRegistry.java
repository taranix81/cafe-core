package org.taranix.cafe.desktop.menu;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.desktop.components.menubar.model.MenuItemModel;
import org.taranix.cafe.desktop.components.menubar.model.PropertiesMenuModel;
import org.taranix.cafe.desktop.events.CafeEventHandlerHub;
import org.taranix.cafe.desktop.events.CafeMenuEvent;

import java.util.Optional;

@CafeSingleton
public class MenuRegistry {

    private static final Logger log = LoggerFactory.getLogger(MenuRegistry.class);

    @CafeInject
    private Optional<PropertiesMenuModel> propertiesMenuModel;

    @CafeInject
    private CafeEventHandlerHub eventHub;

    public Menu buildMenuBar(Shell shell) {
        Menu bar = new Menu(shell, SWT.BAR);
        propertiesMenuModel
                .map(PropertiesMenuModel::getMenuBarModel)
                .ifPresent(model -> model.getItems()
                        .forEach(item -> buildTopLevelItem(item, bar)));
        shell.setMenuBar(bar);
        return bar;
    }

    private void buildTopLevelItem(MenuItemModel item, Menu bar) {
        if (item.isSeparator()) return; // separators not valid at menu bar level

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
        int style = hasSubmenu ? SWT.CASCADE : SWT.PUSH;
        MenuItem menuItem = new MenuItem(parent, style);
        if (item.getName() != null) menuItem.setText(item.getName());

        if (hasSubmenu) {
            Menu submenu = new Menu(parent.getShell(), SWT.DROP_DOWN);
            menuItem.setMenu(submenu);
            item.getMenu().getItems().forEach(sub -> buildMenuItem(sub, submenu));
        } else {
            String actionId = item.getAction() != null ? item.getAction() : item.getId();
            String menuId = item.getId();
            menuItem.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                @Override
                public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                    try {
                        eventHub.sendMenuEvent(CafeMenuEvent.of(menuId, actionId, e.widget));
                    } catch (Exception ex) {
                        log.error("Error dispatching menu event for action '{}'", actionId, ex);
                    }
                }
            });
        }
    }

    public void contributeMenu(MenuComponent component) {
        log.debug("Menu contribution registered: {}", component.getMenuId());
    }
}
