package org.taranix.cafe.beans.annotations.classes;

import org.taranix.cafe.beans.annotations.base.CafeWirerType;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@CafeWirerType
public @interface CafeApplication {
    String[] packages() default {};

    Class<? extends Annotation>[] annotations() default {};
}
