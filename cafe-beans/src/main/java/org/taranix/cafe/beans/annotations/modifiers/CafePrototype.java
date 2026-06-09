package org.taranix.cafe.beans.annotations.modifiers;

import org.taranix.cafe.beans.annotations.classes.CafeService;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@CafeService
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CafePrototype {
}
