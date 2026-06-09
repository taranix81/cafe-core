package org.taranix.cafe.beans.annotations.classes;

import org.taranix.cafe.beans.annotations.base.CafeWiringType;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@CafeWiringType
public @interface CafeApplication {
    String[] packages() default {};

    Class<? extends Annotation>[] annotations() default {};
}
