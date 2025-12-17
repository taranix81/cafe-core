package org.taranix.cafe.beans.metadata;

import org.apache.commons.lang3.NotImplementedException;
import org.taranix.cafe.beans.annotations.CafeInject;
import org.taranix.cafe.beans.annotations.CafeProvider;
import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.beans.annotations.modifiers.CafeName;
import org.taranix.cafe.beans.annotations.modifiers.CafePrimary;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Klasa przechowująca wszystkie statyczne klasy używane jako dane testowe (fixtures)
 * w testach klasy CafeClassInfo.
 */
final class CafeClassMetadataTestFixtures {

    private CafeClassMetadataTestFixtures() {
        // Utility class
    }

    // ... istniejące klasy testowe ...

    // Klasa testująca adnotacje @CafePrimary i @CafeName
    static class PrimaryAndNamedProvider {
        @CafeProvider
        @CafePrimary
        public String providePrimary() {
            return "primary";
        }

        @CafeProvider
        @CafeName("MyNamedBean")
        public String provideNamed() {
            return "named";
        }
    }

    // Klasa testująca adnotację @CafeOptional
    static class OptionalInjectionService {
        @CafeInject
        private String requiredDependency; // Zwykła, wymagana zależność

        @CafeInject
        @org.taranix.cafe.beans.annotations.modifiers.CafeOptional
        private Integer optionalDependency; // Opcjonalna zależność
    }

    // Klasa testująca zagnieżdżone typy generyczne (np. List<String>)
    static class NestedGenericsProvider {
        @CafeInject
        private java.util.List<String> listOfStrings;

        @CafeProvider
        public java.util.List<String> provideList() {
            throw new NotImplementedException();
        }
    }


    // Klasa generyczna z konstruktorem wymagającym typu generycznego
    static abstract class BaseGenericService<T> {
        private final T dependency;

        // Konstruktor wymaga T
        BaseGenericService(T dependency) {
            this.dependency = dependency;
        }
    }

    // Konkretna klasa dziedzicząca (T = Integer)
    static class GenericConstructorService extends BaseGenericService<Integer> {
        public GenericConstructorService(Integer dependency) {
            super(dependency);
        }
    }

    // Klasa bazowa z konstruktorem
    static class SuperClassWithConstructor {
        private final String required;

        public SuperClassWithConstructor(String required) {
            this.required = required;
        }
    }

    // Klasa potomna bez własnego konstruktora, używająca konstruktora klasy bazowej
    static class SubClassWithInheritedConstructor extends SuperClassWithConstructor {
        public SubClassWithInheritedConstructor(String required) {
            super(required);
        }

    }

    // Klasa z dwoma konstruktorami - test NEGATYWNY
    static class TwoConstructorsClass {
        public TwoConstructorsClass() {
        }

        public TwoConstructorsClass(int x) {
        }
    }

    // Klasa z wieloma adnotacjami - test POZYTYWNY na skanowanie
    static class ManyProvidersAndInjectables {

        @CafeInject
        Double aDouble;

        @CafeInject
        @CafeName("Sample1")
        Double namedDouble;

        @CafeInject
        @CafeName("Sample2")
        Double otherNamedDouble;

        Serializable serializable; // Non-annotated

        ManyProvidersAndInjectables(BigDecimal bigDecimal) {
            // Konstruktor wymaga BigDecimal
        }

        @CafeProvider
        String getString() {
            throw new NotImplementedException();
        }

        @CafeProvider
        String getString(int a) {
            throw new NotImplementedException();
        }


        @CafeProvider
        @CafeName("Sample1")
        String getNamedString() {
            throw new NotImplementedException();
        }

        @CafeProvider
        @CafeName("Sample1")
        String getOtherNamedString() {
            throw new NotImplementedException();
        }

        @CafeProvider
        @CafeName("Sample2")
        String getNamedString(int a) {
            throw new NotImplementedException();
        }

        Runnable getRunnable() { // Non-annotated
            throw new NotImplementedException();
        }
    }

    // Abstrakcyjna klasa generyczna
    static class GenericUProviderAndTInjectable<T, U> {
        @CafeInject
        private T unknown;

        @CafeProvider
        public U getUnknown() {
            throw new NotImplementedException();
        }
    }

    // Dziedzicząca klasa generyczna z konkretnymi typami
    @CafeService
    static class IntegerProviderAndStringInjectable extends GenericUProviderAndTInjectable<String, Integer> {
    }


    // Abstrakcyjna klasa generyczna z parametrem metody
    static class GenericUProviderWithXParameterAndTInjectable<T, U, X> {
        @CafeInject
        private T unknown;

        @CafeProvider
        public U getUnknown(X input) {
            throw new NotImplementedException();
        }
    }

    // Dziedzicząca klasa generyczna z konkretnymi typami
    static class DateProviderWithInstantParameterAndLongInjectable extends GenericUProviderWithXParameterAndTInjectable<Long, Date, java.time.Instant> {
    }

    // Klasa testująca Provider statyczny vs instancyjny
    static class StaticProvider {
        @CafeProvider
        public static String provideStatic() {
            return "static";
        }

        @CafeProvider
        public String provideInstance() {
            return "instance";
        }
    }

    // Klasa bazowa z dziedziczonymi adnotacjami
    @CafeService
    static class SuperWithDeprecated {
        @CafeInject
        public String deprecatedField;

        @CafeProvider
        public String deprecatedMethod() {
            return "";
        }
    }

    // Klasa dziedzicząca
    static class Sub extends SuperWithDeprecated {
    }

    @CafeService
    static class PerformanceTestBean {
        @CafeInject
        String field1;
        @CafeInject
        Integer field2;

        public PerformanceTestBean() {
        }

        @CafeProvider
        Double provideDouble() {
            return 1.0;
        }
    }
}