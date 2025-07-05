package org.taranix.cafe.graphics.events.messages;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


public abstract class CafeOldEvent {

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
