package org.taranix.cafe.graphics.events.messages.components;

import lombok.Getter;
import lombok.Setter;
import org.taranix.cafe.graphics.components.View;
import org.taranix.cafe.graphics.events.messages.CafeOldEvent;


public class OpenComponentCafeOldEvent extends CafeOldEvent {

    @Getter
    @Setter
    private View component;
}
