package org.taranix.cafe.desktop.components.menubar.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MenuItemModel {
    private final String id;
    private final String name;
    private MenuModel menu;
    private String action;

}
