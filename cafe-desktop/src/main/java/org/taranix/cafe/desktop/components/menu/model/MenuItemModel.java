package org.taranix.cafe.desktop.components.menu.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MenuItemModel {
    private final String id;
    private final String name;
    @Builder.Default
    private final boolean separator = false;
    private MenuModel menu;
    private String action;

}
