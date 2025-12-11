package org.taranix.cafe.desktop.components_old.events.messages.components;

import lombok.Getter;
import lombok.Setter;
import org.taranix.cafe.desktop.components_old.ViewComponent;
import org.taranix.cafe.desktop.components_old.events.messages.Message;


public class OpenComponentMessage extends Message {

    @Getter
    @Setter
    private ViewComponent component;
}
