package org.taranix.cafe.beans.annotations.classes;

import org.taranix.cafe.beans.annotations.base.CafeWirerType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@CafeWirerType
public @interface CafeService {
    Scope scope() default Scope.Singleton;
}
