package org.taranix.cafe.desktop.components_old;

import org.eclipse.swt.widgets.Widget;

public interface ViewComponent extends Component {


    /**
     * Required by EventBus
     *
     * @return
     */
    Widget getWidget();


    void init(Widget parent);
}
