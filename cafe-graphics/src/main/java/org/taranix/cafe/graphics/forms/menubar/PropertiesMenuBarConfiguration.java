package org.taranix.cafe.graphics.forms.menubar;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.annotations.CafePostInit;
import org.taranix.cafe.beans.annotations.CafeProperty;
import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.graphics.model.menubar.MenuItemModel;
import org.taranix.cafe.graphics.model.menubar.MenuModel;

import java.util.*;

@CafeService
@Slf4j
class PropertiesMenuBarConfiguration implements MenuBarConfiguration {

    public static final String ITEMS = "items";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String MENU = "menu";
    public static final String ON_SELECTION = "onSelection";
    @CafeProperty(name = "cafe.application.menu")
    private LinkedHashMap menuMap;

    private MenuModel menuModel;

    @CafePostInit
    private void createStructure() {
        menuModel = buildMenu(menuMap);
        log.debug("Done");
    }

    private MenuModel buildMenu(Map map) {
        if (map == null || map.get(ITEMS) == null) {
            return null;
        }

        Object items = map.get(ITEMS);
        return MenuModel.builder()
                .items(buildMenuItems((List<?>) items))
                .build();
    }

    private String asString(Object object) {
        if (Objects.isNull(object)) {
            return null;
        }
        return object.toString();
    }

    private List<MenuItemModel> buildMenuItems(List<?> items) {
        if (items == null) {
            return List.of();
        }
        List<MenuItemModel> result = new ArrayList<>();
        for (Object item : items) {
            Map<String, Object> menuItem = (Map<String, Object>) item;
            result.add(MenuItemModel.builder()
                    .id(asString(menuItem.get(ID)))
                    .name(asString(menuItem.get(NAME)))
                    .onSelection(asString(menuItem.get(ON_SELECTION)))
                    .menu(buildMenu((Map<String, Object>) menuItem.get(MENU)))
                    .build()
            );
        }
        return result;
    }

    public MenuModel getMenuBarModel() {
        return menuModel;
    }


}
