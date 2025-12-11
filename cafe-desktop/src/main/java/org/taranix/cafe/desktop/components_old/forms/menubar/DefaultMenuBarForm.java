package org.taranix.cafe.desktop.components_old.forms.menubar;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;
import org.taranix.cafe.desktop.components.menubar.model.MenuItemModel;
import org.taranix.cafe.desktop.components.menubar.model.MenuModel;
import org.taranix.cafe.desktop.components.menubar.model.PropertiesMenuModel;
import org.taranix.cafe.desktop.components_old.forms.WidgetConfig;


//@CafeService
class DefaultMenuBarForm implements MenuBarForm {

    public static final String SEPARATOR_ID = "separatorId";
    private final PropertiesMenuModel configuration;

    DefaultMenuBarForm(PropertiesMenuModel configuration) {
        this.configuration = configuration;
    }

    @Override
    public Menu create(Widget parent) {
        Menu mainMenu = new Menu((Decorations) parent, SWT.BAR);
        ((Decorations) parent).setMenuBar(mainMenu);
        MenuModel menuBarConfig = configuration.getMenuBarModel();
        if (menuBarConfig != null) {
            menuBarConfig.getItems()
                    .forEach(menuItemModel -> buildMenuItem(menuItemModel, mainMenu));
        }
        return mainMenu;
    }

    private int determineStyle(MenuItemModel itemConfig, Menu menu) {
        if (itemConfig.getId().equals(SEPARATOR_ID)) {
            return SWT.SEPARATOR;
        }

        if ((menu.getStyle() & SWT.BAR) == SWT.BAR) {
            return SWT.CASCADE;
        }

        return SWT.PUSH;
    }

    private void buildMenuItem(MenuItemModel itemConfig, Menu menu) {
        final MenuItem menuItem = new MenuItem(menu, determineStyle(itemConfig, menu));

        if (StringUtils.isNoneBlank(itemConfig.getName())) {
            menuItem.setText(itemConfig.getName());
        }
        WidgetConfig.setWidgetId(menuItem, itemConfig.getId());
        WidgetConfig.setWidgetProperty(menuItem, "action", itemConfig.getAction());
        //WidgetConfig.setWidgetSelectionMessage(menuItem, itemConfig.getOnSelection());

        if (itemConfig.getMenu() != null) {
            Menu submenu = buildSubMenu(itemConfig.getMenu(), menu);
            menuItem.setMenu(submenu);
        }
    }

    private Menu buildSubMenu(MenuModel menuModel, Menu menu) {
        Menu result = new Menu(menu.getShell(), SWT.DROP_DOWN);
        menuModel.getItems().forEach(menuItemModel -> buildMenuItem(menuItemModel, result));
        return result;
    }


}
