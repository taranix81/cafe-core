package org.taranix.cafe.desktop.components_old.events.messages.components;

import lombok.Getter;
import lombok.Setter;
import org.taranix.cafe.desktop.components_old.events.messages.Message;

@Getter
@Setter

public class UpdateComponentMessage extends Message {

    private String name;

    private boolean isDirty;

}
