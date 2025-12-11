package org.taranix.cafe.desktop.components_old.events.matchers;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.widgets.Event;
import org.taranix.cafe.desktop.components_old.events.annotations.CafeEventHandler;
import org.taranix.cafe.desktop.components_old.forms.WidgetConfig;

import java.lang.reflect.Method;

//@CafeService
class WidgetIdMatcher implements EventHandlerMatcher {
    @Override
    public boolean isMatch(Method method, Event event) {
        String widgetId = WidgetConfig.getWidgetId(event.widget);
        String targetWidget = method.getAnnotation(CafeEventHandler.class).widgetId();
        return StringUtils.isBlank(targetWidget) || targetWidget.equals(widgetId);
    }
}
