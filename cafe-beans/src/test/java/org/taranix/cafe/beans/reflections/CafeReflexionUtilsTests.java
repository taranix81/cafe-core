package org.taranix.cafe.beans.reflections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.metadata.CafeClassFactory;
import org.taranix.cafe.beans.metadata.CafeConstructor;
import org.taranix.cafe.beans.reflection.CafeReflectionUtils;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
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

    @Test
    void test1() {
        List<Type> types = CafeReflectionUtils.getAllSuperTypes(theClass.class).stream().toList();
        CafeConstructor constructor = CafeClassFactory.create(theClass.class).getConstructor();
        List<BeanTypeKey> providedTypes = constructor.getProvidedTypeKeys().stream().toList();

        Assertions.assertNotNull(types);
    }

    static interface interC<T> {

    }

    static interface interB<T> {

    }

    static interface interA {

    }

    static abstract class theSuperSuperClass<T2> {

    }

    static class theSuperClass<T1> extends theSuperSuperClass<Long> implements interC<Boolean> {

    }

    static class theClass extends theSuperClass<Integer> implements interA, interB<String> {
    }
}
