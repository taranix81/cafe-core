package org.taranix.cafe.beans.annotations;

import org.taranix.cafe.beans.annotations.types.CafeInitable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@CafeInitable
public @interface CafeProperty {
    String name();
}
