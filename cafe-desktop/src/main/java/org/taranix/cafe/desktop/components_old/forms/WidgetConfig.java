package org.taranix.cafe.desktop.components_old.forms;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;
import org.taranix.cafe.desktop.components_old.Component;

import java.util.Objects;
import java.util.Optional;

public class WidgetConfig {

    public static final String WIDGET_EVENT_ID = "CAFE_WIDGET_ID";
    public static final String WIDGET_EVENT_TARGET = "CAFE_WIDGET_EVENT_TARGET";

    public static final String WIDGET_SELECTION_MESSAGE = "CAFE_WIDGET_SELECTION_MESSAGE";

    public static void setComponent(Widget widget, Component component) {
        setWidgetProperty(widget, WIDGET_EVENT_TARGET, component);
    }

    public static void setWidgetProperty(Widget widget, String propertyId, Object value) {
        if (Objects.nonNull(widget) && StringUtils.isNoneBlank(propertyId)) {
            widget.setData(propertyId, value);
        }
    }

    public static Object getWidgetProperty(Widget widget, String propertyId) {
        if (Objects.nonNull(widget) && StringUtils.isNoneBlank(propertyId)) {
            return widget.getData(propertyId);
        }
        return null;
    }

    public static void setWidgetSelectionMessage(Widget widget, String messageId) {
        setWidgetProperty(widget, WIDGET_SELECTION_MESSAGE, messageId);

    }


    public static Component getComponent(Widget widget) {
        return Optional.ofNullable(getWidgetProperty(widget, WIDGET_EVENT_TARGET))
                .filter(Component.class::isInstance)
                .map(Component.class::cast)
                .orElseGet(() -> getWidgetEventTargetFromParent(widget));
    }

    private static Component getWidgetEventTargetFromParent(Menu menu) {
        if (menu.getParentMenu() != null) {
            return getComponent(menu.getParentMenu());
        }
        if (menu.getParentItem() != null) {
            return getComponent(menu.getParentItem());
        }

        if (menu.getParent() != null) {
            return getComponent(menu.getParent());
        }
        return null;
    }

    private static Component getWidgetEventTargetFromParent(Widget widget) {
        if (widget instanceof MenuItem menuItem) {
            return getComponent(menuItem.getParent());
        }

        if (widget instanceof Menu menu) {
            return getWidgetEventTargetFromParent(menu);
        }

        if (widget instanceof CTabItem tabItem) {
            return getComponent(tabItem.getParent());
        }

        return null;
    }

    public static void setWidgetId(Widget widget, String widgetId) {
        widget.setData(WIDGET_EVENT_ID, widgetId);
    }

    public static String getWidgetId(Widget widget) {
        return Optional.ofNullable(getWidgetProperty(widget, WIDGET_EVENT_ID))
                .map(String::valueOf)
                .orElse(null);
    }

}
