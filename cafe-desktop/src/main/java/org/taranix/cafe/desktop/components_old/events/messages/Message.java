package org.taranix.cafe.desktop.components_old.events.messages;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


public abstract class Message {

    @Getter
    private boolean processed;

    @Getter
    @Setter
    private UUID target;

    @Getter
    @Setter
    private UUID source;


    public void setProcessed() {
        processed = true;
    }

}
