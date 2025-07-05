package org.taranix.cafe.graphics.components;

import org.eclipse.swt.widgets.Widget;

public interface View extends Component {


    /**
     * Required by EventBus
     *
     * @return
     */
    Widget getWidget();


    void init(Widget parent);
}
