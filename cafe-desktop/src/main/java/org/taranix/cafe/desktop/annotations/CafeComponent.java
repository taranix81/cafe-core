package org.taranix.cafe.desktop.annotations;

import org.taranix.cafe.beans.annotations.classes.CafePrototype;
import org.taranix.cafe.desktop.components.Form;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@CafePrototype
public @interface CafeComponent {
    Class<? extends Form> form() default Form.class;

    Class<?> model() default void.class;
}
