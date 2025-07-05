package org.taranix.cafe.beans.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CafeApplication {
    String[] packages() default {};

    Class<? extends Annotation>[] annotations() default {};
}
