package org.taranix.cafe.graphics.components.viewers;

import org.taranix.cafe.graphics.components.AbstractView;
import org.taranix.cafe.graphics.components.Component;
import org.taranix.cafe.graphics.components.View;

public abstract class AbstractDataView extends AbstractView implements DataView, View, Component {
//    @Getter
//    @Setter
//    private DataProvider<TData> dataProvider;

    @Override
    public void postInit() {
//        if (getDataProvider() != null) {
        //           read();
        //       }
    }

}
