package org.taranix.cafe.desktop.components.menu;

import org.taranix.cafe.desktop.events.CafeMenuEvent;

//TODO : this is POC
public interface MenuEventListenerExtension {
    boolean supports(String menuId);


    boolean accept(CafeMenuEvent cafeMenuEvent);

}
