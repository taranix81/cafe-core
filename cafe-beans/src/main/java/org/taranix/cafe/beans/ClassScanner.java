package org.taranix.cafe.beans;

import org.taranix.cafe.beans.annotations.CafeAnnotationUtils;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class ClassScanner {

    private final Set<Class<? extends Annotation>> annotations;

    private ClassScanner(final Set<Class<? extends Annotation>> annotations) {
        this.annotations = annotations;
    }

    public static ClassScanner from(Set<Class<? extends Annotation>> annotations) {
        return new ClassScanner(annotations);
    }

    public Set<Class<?>> scan(String... packages) {
        if (packages == null || packages.length == 0) {
            return Set.of();
        }

        return Arrays.stream(packages)
                .flatMap(pkg -> CafeReflectionUtils.getAllClassesFromPackage(CafeReflectionUtils.getDefault(), pkg))
                .filter(aClass -> CafeAnnotationUtils.containsAnnotation(aClass, annotations))
                .collect(Collectors.toSet());
    }

}
