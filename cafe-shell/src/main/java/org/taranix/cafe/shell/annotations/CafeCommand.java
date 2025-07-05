package org.taranix.cafe.shell.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This marker is used to trigger command when Apache Options is matched
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CafeCommand {
    Class<?>[] dependsOn() default {};

    String command() default "";

    String longCommand() default "";

    String description() default "";

    int noOfArgs() default 0;

    String argumentName() default "";

    boolean required() default false;

    char valueSeparator() default ',';

    boolean hasOptionalArgument() default false;

}
