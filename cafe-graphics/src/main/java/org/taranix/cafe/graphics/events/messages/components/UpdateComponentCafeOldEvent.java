package org.taranix.cafe.graphics.events.messages.components;

import lombok.Getter;
import lombok.Setter;
import org.taranix.cafe.graphics.events.messages.CafeOldEvent;

@Getter
@Setter

public class UpdateComponentCafeOldEvent extends CafeOldEvent {

    private String name;

    private boolean isDirty;

}
