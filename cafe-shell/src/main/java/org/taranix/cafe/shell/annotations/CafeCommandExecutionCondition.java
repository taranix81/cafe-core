package org.taranix.cafe.shell.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CafeCommandExecutionCondition {
    CafeExecutionStrategy strategy() default CafeExecutionStrategy.ALWAYS_RUN;
}
