package org.taranix.cafe.beans.annotations.methods;

import org.taranix.cafe.beans.annotations.base.CafeWiringType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@CafeWiringType
public @interface CafeProvider {
}
