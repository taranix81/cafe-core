package org.taranix.cafe.desktop.components_old.events.annotations;

import org.taranix.cafe.desktop.components_old.events.SWTEventType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CafeEventHandler {
    SWTEventType eventType();

    String widgetId() default "";
}
