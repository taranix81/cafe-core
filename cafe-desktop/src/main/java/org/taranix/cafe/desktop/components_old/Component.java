package org.taranix.cafe.desktop.components_old;

import java.util.UUID;

public interface Component {
    UUID getId();


    void postInit();

    void dispose();

    UUID getParentId();

    void setParentId(UUID parentId);
//    void start(Component parent);


}
