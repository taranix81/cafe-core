package org.taranix.cafe.desktop.components.menubar.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class MenuModel {
    private List<MenuItemModel> items;


}
