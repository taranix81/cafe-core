package org.taranix.cafe.desktop.components_old.datasource;

import org.taranix.cafe.desktop.components_old.Component;

public interface DataSource<TData> extends Component {

    String getName();

    boolean write(TData data);

    TData read();
}
