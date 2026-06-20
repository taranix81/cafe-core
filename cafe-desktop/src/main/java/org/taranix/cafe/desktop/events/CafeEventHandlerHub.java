package org.taranix.cafe.desktop.events;

public interface CafeEventHandlerHub {

    void register(Object listener);

    void send(CafeEvent event);

    void sendMenuEvent(CafeMenuEvent event);

    void sendDataSourceMoved(DataSourceMovedEvent event);

    void sendComponentDirtyChanged(ComponentDirtyChangedEvent event);
}
