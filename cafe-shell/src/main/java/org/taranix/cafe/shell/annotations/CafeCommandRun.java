package org.taranix.cafe.shell.annotations;

import org.taranix.cafe.beans.annotations.base.CafeHandlerType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@CafeHandlerType
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CafeCommandRun {
}
