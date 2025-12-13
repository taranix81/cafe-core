package org.taranix.cafe.beans.descriptors;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.annotations.CafeInject;
import org.taranix.cafe.beans.annotations.modifiers.CafeName;
import org.taranix.cafe.beans.descriptors.members.CafeFieldInfo;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.util.List;

class CafeFieldInfoTest {
    @Test
    @DisplayName("should create CafeFieldInfo from standard field in basic class")
    void shouldCreateFieldInfo() {
        //given
        CafeFieldInfo cafeFieldDescriptor = CafeClassInfo.from(StringClass.class).getFieldInfo("string");

        //when - then
        Assertions.assertNotNull(cafeFieldDescriptor);
        Assertions.assertTrue(cafeFieldDescriptor.isField());
        Assertions.assertFalse(cafeFieldDescriptor.isConstructor());
        Assertions.assertFalse(cafeFieldDescriptor.isMethod());
        Assertions.assertEquals(String.class, cafeFieldDescriptor.getFieldTypeKey().getType());
        Assertions.assertTrue(cafeFieldDescriptor.dependencies().contains(BeanTypeKey.from(StringClass.class)));
        Assertions.assertTrue(cafeFieldDescriptor.dependencies().contains(BeanTypeKey.from(String.class)));
        Assertions.assertEquals(cafeFieldDescriptor.getField(), cafeFieldDescriptor.getMember());
        Assertions.assertTrue(cafeFieldDescriptor.provides().isEmpty());
    }

    @Test
    @DisplayName("should create CafeFieldInfo from generic field in inherited class")
    void shouldCreateFieldInfoForGenericClass() {
        //given
        CafeFieldInfo genericFieldInfo = CafeClassInfo.from(IntegerClass.class).getFieldInfo("unknownField");
        CafeFieldInfo integerFieldInfo = CafeClassInfo.from(IntegerClass.class).getFieldInfo("integer");

        //when - then
        Assertions.assertTrue(genericFieldInfo.isField());
        Assertions.assertFalse(genericFieldInfo.isConstructor());
        Assertions.assertFalse(genericFieldInfo.isMethod());
        Assertions.assertTrue(integerFieldInfo.isField());

        Assertions.assertNotNull(genericFieldInfo);
        Assertions.assertNotNull(integerFieldInfo);
        Assertions.assertEquals(Integer.class, genericFieldInfo.getFieldTypeKey().getType());
        Assertions.assertEquals(Integer.class, integerFieldInfo.getFieldTypeKey().getType());
        Assertions.assertTrue(genericFieldInfo.dependencies().contains(BeanTypeKey.from(IntegerClass.class)));
        Assertions.assertTrue(integerFieldInfo.dependencies().contains(BeanTypeKey.from(IntegerClass.class)));
        Assertions.assertTrue(genericFieldInfo.dependencies().contains(BeanTypeKey.from(Integer.class)));
        Assertions.assertTrue(integerFieldInfo.dependencies().contains(BeanTypeKey.from(Integer.class)));

        Assertions.assertEquals(genericFieldInfo.getField(), genericFieldInfo.getMember());
        Assertions.assertEquals(integerFieldInfo.getField(), integerFieldInfo.getMember());

        Assertions.assertTrue(genericFieldInfo.provides().isEmpty());
        Assertions.assertTrue(integerFieldInfo.provides().isEmpty());

    }

    @Test
    @DisplayName("should create CafeFieldInfo  from generic parametrized field in inherited class")
    void shouldCreateFieldInfoForGenericCollectionClass() {
        //given
        CafeFieldInfo genericFieldInfo = CafeClassInfo.from(CollectionOfIntegersClass.class).getFieldInfo("unknownField");
        CafeFieldInfo integerFieldInfo = CafeClassInfo.from(CollectionOfIntegersClass.class).getFieldInfo("integer");

        //when - then
        Assertions.assertTrue(genericFieldInfo.isField());
        Assertions.assertFalse(genericFieldInfo.isConstructor());
        Assertions.assertFalse(genericFieldInfo.isMethod());
        Assertions.assertTrue(integerFieldInfo.isField());

        Assertions.assertNotNull(genericFieldInfo);
        Assertions.assertNotNull(integerFieldInfo);
        Assertions.assertEquals(BeanTypeKey.from(List.class, Integer.class).getType(), genericFieldInfo.getFieldTypeKey().getType());
        Assertions.assertEquals(Integer.class, integerFieldInfo.getFieldTypeKey().getType());

        Assertions.assertTrue(genericFieldInfo.dependencies().contains(BeanTypeKey.from(List.class, Integer.class)));
        Assertions.assertTrue(genericFieldInfo.dependencies().contains(BeanTypeKey.from(CollectionOfIntegersClass.class)));

        Assertions.assertTrue(integerFieldInfo.dependencies().contains(BeanTypeKey.from(Integer.class)));
        Assertions.assertTrue(integerFieldInfo.dependencies().contains(BeanTypeKey.from(CollectionOfIntegersClass.class)));

        Assertions.assertEquals(genericFieldInfo.getField(), genericFieldInfo.getMember());
        Assertions.assertEquals(integerFieldInfo.getField(), integerFieldInfo.getMember());

        Assertions.assertTrue(genericFieldInfo.provides().isEmpty());
        Assertions.assertTrue(integerFieldInfo.provides().isEmpty());

    }

    static class NamedStringClass {
        @CafeInject
        @CafeName("myString")
        private String string;
    }

    static class StringClass {
        @CafeInject
        private String string;
    }

    static class GenericClass<T> {

        @CafeInject
        private T unknownField;

        @CafeInject
        private Integer integer;

    }

    static class IntegerClass extends GenericClass<Integer> {
    }

    static class CollectionOfIntegersClass extends GenericClass<List<Integer>> {
    }

}
