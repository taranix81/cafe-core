package org.taranix.cafe.graphics.events.matchers;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.widgets.Event;
import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.graphics.events.annotations.SwtEventHandler;
import org.taranix.cafe.graphics.forms.WidgetConfig;

import java.lang.reflect.Method;

@CafeService
class EventWidgetIdMatcher implements EventHandlerMatcher {
    @Override
    public boolean isMatch(Method method, Event event) {
        String widgetId = WidgetConfig.getWidgetId(event.widget);
        String targetWidget = method.getAnnotation(SwtEventHandler.class).widgetId();
        return StringUtils.isBlank(targetWidget) || targetWidget.equals(widgetId);
    }
}
