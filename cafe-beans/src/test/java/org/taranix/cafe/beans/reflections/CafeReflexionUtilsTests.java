package org.taranix.cafe.beans.reflections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.reflection.CafeReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;

class CafeReflexionUtilsTests {


    @Test
    void shouldRecognizeClassesAsCollection() {
        Assertions.assertFalse(CafeReflectionUtils.isCollection(Map.class));
        Assertions.assertFalse(CafeReflectionUtils.isCollection(HashMap.class));

        Assertions.assertTrue(CafeReflectionUtils.isCollection(Set.class));
        Assertions.assertTrue(CafeReflectionUtils.isCollection(HashSet.class));

        Assertions.assertTrue(CafeReflectionUtils.isCollection(List.class));
        Assertions.assertTrue(CafeReflectionUtils.isCollection(ArrayList.class));

    }

    @Test
    void shouldRecognizeParameterizedTypeAsCollection() {
        Field[] fields = TestCollection.class.getFields();

        Arrays.stream(fields).forEach(field ->
                Assertions.assertTrue(CafeReflectionUtils.isCollection(field.getGenericType()), "Should be a collection %s".formatted(field))
        );
    }
}
