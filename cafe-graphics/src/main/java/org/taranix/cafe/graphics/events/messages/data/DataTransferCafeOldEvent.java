package org.taranix.cafe.graphics.events.messages.data;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.taranix.cafe.graphics.events.messages.CafeOldEvent;

@ToString
@Getter
@Setter
public abstract class DataTransferCafeOldEvent<TData> extends CafeOldEvent {


    private String dataName;

    private TData data;

}
