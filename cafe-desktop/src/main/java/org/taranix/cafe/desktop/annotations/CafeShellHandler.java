package org.taranix.cafe.desktop.annotations;



import org.taranix.cafe.beans.annotations.types.CafeTaskable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@CafeTaskable
public @interface CafeShellHandler {
    ShellHandlerType type();

    boolean primary() default false;
}
