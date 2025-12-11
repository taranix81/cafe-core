package org.taranix.cafe.desktop.components_old.viewers;

import org.taranix.cafe.desktop.components_old.Component;
import org.taranix.cafe.desktop.components_old.ViewComponent;
import org.taranix.cafe.desktop.components_old.datasource.DataSource;

public interface Viewer<TData> extends ViewComponent, Component {
    void read();

    DataSource<TData> getDataSource();

    void setDataSource(DataSource<TData> dataSource);
}
