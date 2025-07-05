package org.taranix.cafe.graphics.components.providers;

import org.taranix.cafe.graphics.components.Component;

public interface DataProvider<TData> extends Component {

    String getName();

    boolean write(TData data);

    TData read();
}
