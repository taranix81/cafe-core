package org.taranix.cafe.beans.metadata;

import org.apache.commons.lang3.NotImplementedException;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.beans.annotations.methods.CafeProvider;
import org.taranix.cafe.beans.annotations.modifiers.CafeName;

/**
 * Utility class holding test fixtures (classes) for CafeMethodInfoTest.
 */
final class CafeMethodMetadataTestFixtures {

    private CafeMethodMetadataTestFixtures() {
        // Utility class
    }

    static class SimpleBeanWithProvider {
        public static final String SOME_EXAMPLE_ID = "some-example-id";

        @CafeInject
        private Integer integer;

        @CafeName(value = SOME_EXAMPLE_ID)
        public String getNamedNonProviderString() {
            throw new NotImplementedException();
        }

        @CafeProvider
        @CafeName(value = SOME_EXAMPLE_ID)
        public String getNamedProvidedString() {
            throw new NotImplementedException();
        }


        // Metoda bez adnotacji @CafeProvider
        public String getNonProviderString(String value) {
            throw new NotImplementedException();
        }
    }

    static class GenericClass<T> {
        public static final String SOME_EXAMPLE_ID = "some-example-id";

        @CafeInject
        private T unknownField;

        @CafeInject
        private Integer integer;

        @CafeName(value = SOME_EXAMPLE_ID)
        public String getString() {
            throw new NotImplementedException();
        }

        @CafeProvider
        public T getUnknownValue() {
            throw new NotImplementedException();
        }

        @CafeProvider
        public String getStringProviderWithParameter(String value) {
            throw new NotImplementedException();
        }
    }

    static class IntegerClass extends GenericClass<Integer> {

    }

    static class StringClass extends GenericClass<Integer> {

    }


    static class StaticMethodProvider {
        @CafeProvider
        public static Integer provideStaticInteger() {
            return 42;
        }

        @CafeProvider
        public String provideInstanceString() {
            return "instance";
        }
    }
}