package org.taranix.cafe.desktop.components_old.viewers;

import lombok.Getter;
import lombok.Setter;
import org.taranix.cafe.desktop.components_old.Component;
import org.taranix.cafe.desktop.components_old.ViewComponent;
import org.taranix.cafe.desktop.components_old.datasource.DataSource;

public abstract class AbstractViewer<TData> extends AbstractViewComponent implements Viewer<TData>, ViewComponent, Component {
    @Getter
    @Setter
    private DataSource<TData> dataSource;

    @Override
    public void postInit() {
        if (getDataSource() != null) {
            read();
        }
    }

    public void dispose() {
        if (getDataSource() != null) {
            getDataSource().dispose();
        }
        super.dispose();
    }
}
