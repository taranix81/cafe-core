package org.taranix.cafe.desktop.components.menubar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.taranix.cafe.desktop.actions.HandlersService;
import org.taranix.cafe.desktop.components.menubar.model.MenuItemModel;
import org.taranix.cafe.desktop.components.menubar.model.MenuModel;
import org.taranix.cafe.desktop.components_old.forms.WidgetConfig;

import java.util.Optional;

@Deprecated(forRemoval = true, since = "Move into component")
public class MenuBuilder {
    public static final String SEPARATOR_ID = "separatorId";

    public static Menu build(Decorations decorations, int style, MenuModel model, HandlersService handlersService) {
        Menu menu = new Menu(decorations, style);
        if (model != null) {
            model.getItems().forEach(menuItemModel -> buildMenuItem(menuItemModel, menu, handlersService));
        }
        return menu;
    }

    private static void buildMenuItem(MenuItemModel itemModel, Menu menu, HandlersService handlersService) {
        final MenuItem menuItem = new MenuItem(menu, determineStyle(itemModel, menu));
        WidgetConfig.setWidgetId(menuItem, itemModel.getId());
        Optional.ofNullable(itemModel.getName()).ifPresent(menuItem::setText);
        handlersService.bind(menuItem);

        if (itemModel.getMenu() != null) {
            Menu submenu = buildMenu(itemModel.getMenu(), menu, handlersService);
            menuItem.setMenu(submenu);
        }
    }

    private static Menu buildMenu(MenuModel menuModel, Menu menu, HandlersService handlersService) {
        Menu result = new Menu(menu.getShell(), SWT.DROP_DOWN);
        menuModel.getItems().forEach(menuItemModel -> buildMenuItem(menuItemModel, result, handlersService));
        return result;
    }

    private static int determineStyle(MenuItemModel itemModel, Menu menu) {
        if (itemModel.getId().equals(SEPARATOR_ID)) {
            return SWT.SEPARATOR;
        }

        if ((menu.getStyle() & SWT.BAR) == SWT.BAR) {
            return SWT.CASCADE;
        }

        return SWT.PUSH;
    }

}
