package org.taranix.cafe.beans.metadata;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.annotations.modifiers.CafeName;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.util.List;


class CafeFieldInfoTest {
    @Test
    @DisplayName("Positive: Should create CafeFieldInfo from standard field in basic class")
    void shouldCreateFieldInfo() {
        //given
        CafeField cafeFieldDescriptor = CafeClassFactory.create(CafeFieldInfoTestFixtures.StringClass.class).getField("string");

        //when - then
        Assertions.assertNotNull(cafeFieldDescriptor);
        Assertions.assertTrue(cafeFieldDescriptor.isField());
        Assertions.assertFalse(cafeFieldDescriptor.isConstructor());
        Assertions.assertFalse(cafeFieldDescriptor.isMethod());
        Assertions.assertEquals(String.class, cafeFieldDescriptor.getFieldTypeKey().getType());
        Assertions.assertTrue(cafeFieldDescriptor.getRequiredTypeKeys().contains(BeanTypeKey.from(CafeFieldInfoTestFixtures.StringClass.class)));
        Assertions.assertTrue(cafeFieldDescriptor.getRequiredTypeKeys().contains(BeanTypeKey.from(String.class)));
        Assertions.assertEquals(cafeFieldDescriptor.getField(), cafeFieldDescriptor.getMember());
        Assertions.assertTrue(cafeFieldDescriptor.getProvidedTypeKeys().isEmpty());
    }

    @Test
    @DisplayName("Positive: Should create CafeFieldInfo from generic field in inherited class")
    void shouldCreateFieldInfoForGenericClass() {
        //given
        CafeField genericFieldInfo = CafeClassFactory.create(CafeFieldInfoTestFixtures.IntegerClass.class).getField("unknownField");
        CafeField integerFieldInfo = CafeClassFactory.create(CafeFieldInfoTestFixtures.IntegerClass.class).getField("integer");

        //when - then
        Assertions.assertTrue(genericFieldInfo.isField());
        Assertions.assertFalse(genericFieldInfo.isConstructor());
        Assertions.assertFalse(genericFieldInfo.isMethod());
        Assertions.assertTrue(integerFieldInfo.isField());

        Assertions.assertNotNull(genericFieldInfo);
        Assertions.assertNotNull(integerFieldInfo);
        Assertions.assertEquals(Integer.class, genericFieldInfo.getFieldTypeKey().getType());
        Assertions.assertEquals(Integer.class, integerFieldInfo.getFieldTypeKey().getType());
        Assertions.assertTrue(genericFieldInfo.getRequiredTypeKeys().contains(BeanTypeKey.from(CafeFieldInfoTestFixtures.IntegerClass.class)));
        Assertions.assertTrue(integerFieldInfo.getRequiredTypeKeys().contains(BeanTypeKey.from(CafeFieldInfoTestFixtures.IntegerClass.class)));
        Assertions.assertTrue(genericFieldInfo.getRequiredTypeKeys().contains(BeanTypeKey.from(Integer.class)));
        Assertions.assertTrue(integerFieldInfo.getRequiredTypeKeys().contains(BeanTypeKey.from(Integer.class)));

        Assertions.assertEquals(genericFieldInfo.getField(), genericFieldInfo.getMember());
        Assertions.assertEquals(integerFieldInfo.getField(), integerFieldInfo.getMember());

        Assertions.assertTrue(genericFieldInfo.getProvidedTypeKeys().isEmpty());
        Assertions.assertTrue(integerFieldInfo.getProvidedTypeKeys().isEmpty());

    }

    @Test
    @DisplayName("Positive: Should create CafeFieldInfo from generic parametrized field in inherited class")
    void shouldCreateFieldInfoForGenericCollectionClass() {
        //given
        CafeField genericFieldInfo = CafeClassFactory.create(CafeFieldInfoTestFixtures.CollectionOfIntegersClass.class).getField("unknownField");
        CafeField integerFieldInfo = CafeClassFactory.create(CafeFieldInfoTestFixtures.CollectionOfIntegersClass.class).getField("integer");

        //when - then
        Assertions.assertTrue(genericFieldInfo.isField());
        Assertions.assertFalse(genericFieldInfo.isConstructor());
        Assertions.assertFalse(genericFieldInfo.isMethod());
        Assertions.assertTrue(integerFieldInfo.isField());

        Assertions.assertNotNull(genericFieldInfo);
        Assertions.assertNotNull(integerFieldInfo);
        Assertions.assertEquals(BeanTypeKey.from(List.class, Integer.class).getType(), genericFieldInfo.getFieldTypeKey().getType());
        Assertions.assertEquals(Integer.class, integerFieldInfo.getFieldTypeKey().getType());

        Assertions.assertTrue(genericFieldInfo.getRequiredTypeKeys().contains(BeanTypeKey.from(List.class, Integer.class)));
        Assertions.assertTrue(genericFieldInfo.getRequiredTypeKeys().contains(BeanTypeKey.from(CafeFieldInfoTestFixtures.CollectionOfIntegersClass.class)));

        Assertions.assertTrue(integerFieldInfo.getRequiredTypeKeys().contains(BeanTypeKey.from(Integer.class)));
        Assertions.assertTrue(integerFieldInfo.getRequiredTypeKeys().contains(BeanTypeKey.from(CafeFieldInfoTestFixtures.CollectionOfIntegersClass.class)));

        Assertions.assertEquals(genericFieldInfo.getField(), genericFieldInfo.getMember());
        Assertions.assertEquals(integerFieldInfo.getField(), integerFieldInfo.getMember());

        Assertions.assertTrue(genericFieldInfo.getProvidedTypeKeys().isEmpty());
        Assertions.assertTrue(integerFieldInfo.getProvidedTypeKeys().isEmpty());

    }

    @Test
    @DisplayName("Positive: Should correctly identify 'named' field and check provides/dependencies")
    void shouldIdentifyNamedFieldAndDependencies() {
        //given
        CafeField namedFieldInfo = CafeClassFactory.create(CafeFieldInfoTestFixtures.NamedStringClass.class).getField("string");

        //when - then
        Assertions.assertNotNull(namedFieldInfo);
        Assertions.assertEquals("myString", namedFieldInfo.getAnnotation(CafeName.class).value(), "Field should have the name 'myString'");
        Assertions.assertTrue(namedFieldInfo.getRequiredTypeKeys().contains(BeanTypeKey.from(CafeFieldInfoTestFixtures.NamedStringClass.class)), "Field must depend on its owner class");
        Assertions.assertTrue(namedFieldInfo.getRequiredTypeKeys().contains(BeanTypeKey.from(String.class, "myString")), "Field must depend on its type (String)");
        Assertions.assertTrue(namedFieldInfo.getProvidedTypeKeys().isEmpty(), "Field should not provide a bean");
    }

    @Test
    @DisplayName("Positive: Should handle non-annotated field correctly (only dependency on owner)")
    void shouldHandleNonAnnotatedField() {
        //given
        CafeField nonAnnotatedFieldInfo = CafeClassFactory.create(CafeFieldInfoTestFixtures.NamedStringClass.class).getField("notInjected");

        //when - then
        Assertions.assertNull(nonAnnotatedFieldInfo);
    }
}