package org.taranix.cafe.graphics.annotations;

import org.taranix.cafe.graphics.components.providers.DataProvider;
import org.taranix.cafe.graphics.forms.Form;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CafeComponent {
    Class<? extends Form> form() default Form.class;

    Class<? extends DataProvider> dataSource() default DataProvider.class;
}
