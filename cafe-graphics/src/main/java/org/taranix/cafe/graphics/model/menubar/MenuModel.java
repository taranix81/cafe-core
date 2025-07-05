package org.taranix.cafe.graphics.model.menubar;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class MenuModel {
    private List<MenuItemModel> items;
}
