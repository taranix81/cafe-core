package org.taranix.cafe.desktop.components_old.events.messages.data;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.taranix.cafe.desktop.components_old.events.messages.Message;

@ToString
@Getter
@Setter
public abstract class DataTransferMessage<TData> extends Message {


    private String dataName;

    private TData data;

}
