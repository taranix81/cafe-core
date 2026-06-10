package org.taranix.cafe.beans.resolvers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.CafeApplicationContext;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.beans.annotations.fields.CafeProperty;
import org.taranix.cafe.beans.annotations.methods.CafeProvider;
import org.taranix.cafe.beans.converters.CafeConverter;
import org.taranix.cafe.beans.resolvers.metadata.field.PropertyResolver;
import org.taranix.cafe.beans.resolvers.metadata.field.WireFieldResolver;

import java.util.Optional;

class FieldResolverTest {

    // --- WireFieldResolver.supports ---

    @Test
    @DisplayName("WireFieldResolver: supports @CafeInject")
    void wireFieldResolverSupportsCafeInject() {
        Assertions.assertTrue(new WireFieldResolver().supports(CafeInject.class));
    }

    @Test
    @DisplayName("WireFieldResolver: does not support @CafeProperty")
    void wireFieldResolverDoesNotSupportCafeProperty() {
        Assertions.assertFalse(new WireFieldResolver().supports(CafeProperty.class));
    }

    // --- PropertyResolver.supports ---

    @Test
    @DisplayName("PropertyResolver: supports @CafeProperty")
    void propertyResolverSupportsCafeProperty() {
        Assertions.assertTrue(new PropertyResolver().supports(CafeProperty.class));
    }

    @Test
    @DisplayName("PropertyResolver: does not support @CafeInject")
    void propertyResolverDoesNotSupportCafeInject() {
        Assertions.assertFalse(new PropertyResolver().supports(CafeInject.class));
    }

    // --- WireFieldResolver: injection via context ---

    @Test
    @DisplayName("WireFieldResolver: injects bean value into field")
    void wireFieldResolverInjectsBeanIntoField() {
        CafeApplicationContext ctx = CafeApplicationContext.builder()
                .withClass(StringProvider.class)
                .withClass(StringConsumer.class)
                .build();
        ctx.initialize();
        StringConsumer instance = ctx.getInstance(StringConsumer.class);
        Assertions.assertEquals("injected", instance.value);
    }

    @Test
    @DisplayName("WireFieldResolver: injects Optional<T> field")
    void wireFieldResolverInjectsOptionalField() {
        CafeApplicationContext ctx = CafeApplicationContext.builder()
                .withClass(StringProvider.class)
                .withClass(OptionalConsumer.class)
                .build();
        ctx.initialize();
        OptionalConsumer instance = ctx.getInstance(OptionalConsumer.class);
        Assertions.assertTrue(instance.value.isPresent());
        Assertions.assertEquals("injected", instance.value.get());
    }

    // --- PropertyResolver: injection via context ---

    @Test
    @DisplayName("PropertyResolver: injects String property by name")
    void propertyResolverInjectsStringField() {
        CafeApplicationContext ctx = CafeApplicationContext.builder()
                .withPackageScan(CafeConverter.class.getPackageName())
                .withClass(StringPropertyConsumer.class)
                .build();
        ctx.initialize();
        StringPropertyConsumer instance = ctx.getInstance(StringPropertyConsumer.class);
        Assertions.assertEquals("Black Cats", instance.value);
    }

    @Test
    @DisplayName("PropertyResolver: converts Integer property")
    void propertyResolverConvertsIntegerField() {
        CafeApplicationContext ctx = CafeApplicationContext.builder()
                .withPackageScan(CafeConverter.class.getPackageName())
                .withClass(IntegerPropertyConsumer.class)
                .build();
        ctx.initialize();
        IntegerPropertyConsumer instance = ctx.getInstance(IntegerPropertyConsumer.class);
        Assertions.assertEquals(14, instance.value);
    }

    // --- Fixture classes ---

    @CafeSingleton
    static class StringProvider {
        @CafeProvider
        String get() { return "injected"; }
    }

    @CafeSingleton
    static class StringConsumer {
        @CafeInject
        String value;
    }

    @CafeSingleton
    static class OptionalConsumer {
        @CafeInject
        Optional<String> value;
    }

    @CafeSingleton
    static class StringPropertyConsumer {
        @CafeProperty(name = "test.property")
        String value;
    }

    @CafeSingleton
    static class IntegerPropertyConsumer {
        @CafeProperty(name = "test.integer")
        Integer value;
    }
}
