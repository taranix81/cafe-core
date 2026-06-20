package org.taranix.cafe.desktop.menu;

import org.taranix.cafe.desktop.components.menubar.model.MenuModel;

public interface MenuComponent {

    String getMenuId();

    MenuModel getMenuContributions();
}
