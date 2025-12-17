package org.taranix.cafe.beans.metadata;

import org.taranix.cafe.beans.annotations.CafeInject;
import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.beans.annotations.modifiers.CafeName;

import java.util.List;

/**
 * Utility class holding test fixtures (classes) for CafeFieldInfoTest.
 */
final class CafeFieldInfoTestFixtures {

    private CafeFieldInfoTestFixtures() {
        // Utility class
    }

    static class NamedStringClass {
        @CafeInject
        @CafeName("myString")
        private String string; // Używane w pierwszym nowym teście

        private String notInjected; // Używane w drugim nowym teście
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

    @CafeService
    static class IntegerClass extends GenericClass<Integer> {
    }

    @CafeService
    static class CollectionOfIntegersClass extends GenericClass<List<Integer>> {
    }
}