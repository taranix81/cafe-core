package org.taranix.cafe.beans.reflections;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.reflection.CafeTypesUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CafeTypesUtils focused on generic type resolution and hierarchy traversal.
 */
class CafeTypesUtilsTest {

    // --- Core Functionality Tests ---

    @Test
    @DisplayName("Should return an empty set when input class is null")
    void testNullInput() {
        // Given: null input

        // When
        Set<Type> types = CafeTypesUtils.getAllSuperTypes(null);

        // Then
        assertTrue(types.isEmpty(), "Result should be empty for null input");
    }

    @Test
    @DisplayName("Should fully resolve ArrayList<String> hierarchy and replace all TypeVariables")
    void testArrayListStringResolution() {
        // Given: An anonymous class capturing ArrayList<String> context
        Class<?> clazz = new ArrayList<String>() {
        }.getClass();

        // When
        Set<Type> types = CafeTypesUtils.getAllSuperTypes(clazz);

        // Then
        boolean hasAbstractListString = types.stream()
                .anyMatch(t -> t.toString().equals("java.util.AbstractList<java.lang.String>"));
        boolean hasListString = types.stream()
                .anyMatch(t -> t.toString().equals("java.util.List<java.lang.String>"));

        boolean hasUnresolvedE = types.stream()
                .anyMatch(t -> t.toString().contains("<E>"));

        assertTrue(hasAbstractListString, "Missing AbstractList<String> in hierarchy");
        assertTrue(hasListString, "Missing List<String> in hierarchy");
        assertFalse(hasUnresolvedE, "Found unresolved TypeVariable <E> in the output!");
    }

    @Test
    @DisplayName("Should resolve multi-level custom generic inheritance (Interfaces and Classes)")
    void testCustomGenericHierarchy() {
        // Given: A class implementing multiple nested generic levels
        Class<?> clazz = StringImplementation.class;

        // When
        Set<Type> types = CafeTypesUtils.getAllSuperTypes(clazz);

        // Then: StringImplementation -> BaseClass<String> -> BaseInterface<String>
        assertTrue(types.contains(TypeUtils.parameterize(BaseClass.class, String.class)));
        assertTrue(types.contains(TypeUtils.parameterize(BaseInterface.class, String.class)));
        assertTrue(types.contains(TypeUtils.parameterize(ExtendedInterface.class, String.class)));
    }

    @Test
    @DisplayName("Should handle nested generic types such as List<List<Integer>>")
    void testNestedGenerics() {
        // Given: A class extending a nested generic structure
        Class<?> clazz = NestedList.class;

        // When
        Set<Type> types = CafeTypesUtils.getAllSuperTypes(clazz);

        // Then
        Type inner = TypeUtils.parameterize(List.class, Integer.class);
        Type expected = TypeUtils.parameterize(ArrayList.class, inner);

        assertTrue(types.contains(expected), "Should contain fully resolved ArrayList<List<Integer>>");
    }

    @Test
    @DisplayName("Should include standard non-generic interfaces in the result set")
    void testNonGenericInterfaces() {
        // Given
        Class<?> clazz = ArrayList.class;

        // When
        Set<Type> types = CafeTypesUtils.getAllSuperTypes(clazz);

        // Then
        assertTrue(types.contains(RandomAccess.class));
        assertTrue(types.contains(Cloneable.class));
        assertTrue(types.contains(Serializable.class));
    }

    @Test
    @DisplayName("Should exclude the starting class and Object.class from the results")
    void testExclusions() {
        // Given
        Class<?> clazz = String.class;

        // When
        Set<Type> types = CafeTypesUtils.getAllSuperTypes(clazz);

        // Then
        assertFalse(types.contains(String.class), "Result should not contain the input class itself");
        assertFalse(types.contains(Object.class), "Result should not contain Object.class");
    }

    @Test
    @DisplayName("Should not crash and provide raw types when no generic arguments are provided")
    void testRawClassResolution() {
        // Given: A raw ArrayList without type parameters

        // When & Then
        Set<Type> types = assertDoesNotThrow(() -> CafeTypesUtils.getAllSuperTypes(ArrayList.class));
        assertNotNull(types);
        assertTrue(types.stream().anyMatch(t -> t.toString().contains("AbstractList")), "Should find AbstractList even if raw");
    }

    // --- Universal Resolve Method Tests ---

    @Test
    @DisplayName("Should resolve a standalone TypeVariable T to String.class based on context")
    void testResolveTypeVariable() throws NoSuchMethodException {
        // Given: 'T' return type from Repository interface
        Method method = Repository.class.getMethod("findById", Object.class);
        Type typeToResolve = method.getGenericReturnType();

        // When: Resolving in the context of StringService
        Type resolved = CafeTypesUtils.resolve(StringService.class, typeToResolve);

        // Then
        assertEquals(String.class, resolved, "T should be resolved to String.class");
    }

    @Test
    @DisplayName("Should resolve a ParameterizedType List<T> to List<String>")
    void testResolveParameterizedType() throws NoSuchMethodException {
        // Given: 'List<T>' return type
        Method method = Repository.class.getMethod("findAll");
        Type typeToResolve = method.getGenericReturnType();

        // When
        Type resolved = CafeTypesUtils.resolve(StringService.class, typeToResolve);

        // Then
        Type expected = TypeUtils.parameterize(List.class, String.class);
        assertTrue(TypeUtils.equals(expected, resolved), "Should resolve to List<String>");
    }

    @Test
    @DisplayName("Should resolve a field type declared in a generic superclass")
    void testResolveFieldType() throws NoSuchFieldException {
        // Given: 'ENTITY' field declared in AbstractService<ENTITY, PK>
        Field field = AbstractService.class.getDeclaredField("mainEntity");
        Type typeToResolve = field.getGenericType();

        // When
        Type resolved = CafeTypesUtils.resolve(StringService.class, typeToResolve);

        // Then
        assertEquals(String.class, resolved, "Field should be resolved to String.class");
    }

    @Test
    @DisplayName("Should return the original class unchanged when no generics are involved")
    void testResolveSimpleClass() {
        // Given
        Type typeToResolve = Integer.class;

        // When
        Type resolved = CafeTypesUtils.resolve(StringService.class, typeToResolve);

        // Then
        assertEquals(Integer.class, resolved, "Simple class should remain unchanged");
    }

    @Test
    @DisplayName("Should resolve Map<K, V> with multiple type variables to concrete Map<String, Integer>")
    void testResolveMapWithMultipleParameters() throws NoSuchMethodException {
        // Given: 'Map<K, V>' return type
        Method method = GeneralRepository.class.getMethod("getItemsMap");
        Type typeToResolve = method.getGenericReturnType();

        // When
        Type resolved = CafeTypesUtils.resolve(StringIntService.class, typeToResolve);

        // Then
        Type expected = TypeUtils.parameterize(Map.class, String.class, Integer.class);
        assertTrue(TypeUtils.equals(expected, resolved), "Should resolve to Map<String, Integer>");
    }

    // --- Tests for isTypeCompatible ---

    @Test
    @DisplayName("Should return true for identical simple classes")
    void testIsTypeCompatibleSimpleIdentical() {
        assertTrue(CafeTypesUtils.isTypeCompatible(String.class, String.class));
    }

    @Test
    @DisplayName("Should return true for assignable simple classes (inheritance)")
    void testIsTypeCompatibleSimpleInheritance() {
        // Declared: Number, Provided: Integer
        assertTrue(CafeTypesUtils.isTypeCompatible(Number.class, Integer.class));
        // Declared: List, Provided: ArrayList
        assertTrue(CafeTypesUtils.isTypeCompatible(List.class, ArrayList.class));
    }

    @Test
    @DisplayName("Should return false for non-assignable simple classes")
    void testIsTypeCompatibleIncompatible() {
        assertFalse(CafeTypesUtils.isTypeCompatible(String.class, Integer.class));
    }

    @Test
    @DisplayName("Should match concrete class against ParameterizedType (Case B)")
    void testIsTypeCompatibleClassAgainstParameterized() {
        // Declared: List<String>, Provided: ArrayList.class
        Type declaredListString = TypeUtils.parameterize(List.class, String.class);
        Class<?> providedArrayList = new ArrayList<String>() {
        }.getClass();

        assertTrue(CafeTypesUtils.isTypeCompatible(declaredListString, providedArrayList));
    }

    @Test
    @DisplayName("Should recursively match nested ParameterizedTypes")
    void testIsTypeCompatibleNestedGenerics() {
        // Declared: List<List<String>>
        Type innerList = TypeUtils.parameterize(List.class, String.class);
        Type declaredNested = TypeUtils.parameterize(List.class, innerList);

        // Provided: ArrayList<ArrayList<String>>
        Type providedInner = TypeUtils.parameterize(ArrayList.class, String.class);
        Type providedNested = TypeUtils.parameterize(ArrayList.class, providedInner);

        assertTrue(CafeTypesUtils.isTypeCompatible(declaredNested, providedNested));
    }

    @Test
    @DisplayName("Should return false when generic arguments do not match")
    void testIsTypeCompatibleIncompatibleGenerics() {
        // Declared: List<String>
        Type declared = TypeUtils.parameterize(List.class, String.class);
        // Provided: List<Integer>
        Type provided = TypeUtils.parameterize(List.class, Integer.class);

        assertFalse(CafeTypesUtils.isTypeCompatible(declared, provided));
    }

    @Test
    @DisplayName("Should handle complex real-world hierarchy with generics")
    void testIsTypeCompatibleComplexHierarchy() {
        Type declared = TypeUtils.parameterize(BaseInterface.class, String.class);
        Class<?> provided = StringImplementation.class;

        assertTrue(CafeTypesUtils.isTypeCompatible(declared, provided));
    }
    // --- Mock Classes and Interfaces for Testing ---

    interface GeneralRepository<K, V> {
        Map<K, V> getItemsMap();
    }

    interface Repository<T, ID> {
        T findById(ID id);

        List<T> findAll();
    }

    interface BaseInterface<T> {
    }

    interface ExtendedInterface<T> extends BaseInterface<T> {
    }

    static class StringIntService implements GeneralRepository<String, Integer> {
        @Override
        public Map<String, Integer> getItemsMap() {
            return null;
        }
    }

    static class AbstractService<ENTITY, PK> {
        ENTITY mainEntity;
    }

    static class StringService extends AbstractService<String, Long> implements Repository<String, Long> {
        @Override
        public String findById(Long id) {
            return null;
        }

        @Override
        public List<String> findAll() {
            return null;
        }
    }

    static class BaseClass<T> implements BaseInterface<T> {
    }

    static class StringImplementation extends BaseClass<String> implements ExtendedInterface<String> {
    }

    static class NestedList extends ArrayList<List<Integer>> {
    }
}