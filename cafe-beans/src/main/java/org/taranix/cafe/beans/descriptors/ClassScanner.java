package org.taranix.cafe.beans.descriptors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.taranix.cafe.beans.CafeReflectionUtils;
import org.taranix.cafe.beans.annotations.CafeAnnotationUtils;
import org.taranix.cafe.beans.annotations.types.CafeType;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassScanner {

    private static final ClassScanner classScanner = new ClassScanner();

    public static ClassScanner getInstance() {
        return classScanner;
    }

    public Set<Class<?>> scan(String... packages) {
        if (packages == null || packages.length == 0) {
            return Set.of();
        }

        return Arrays.stream(packages)
                .flatMap(pkg -> CafeReflectionUtils.getAllClassesFromPackage(CafeReflectionUtils.getDefault(), pkg))
                .filter(aClass -> CafeAnnotationUtils.hasMarker(aClass, CafeType.class))
                .collect(Collectors.toSet());
    }

}
