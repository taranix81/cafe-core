package org.taranix.cafe.beans.resolvers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.CafeApplicationContext;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;
import org.taranix.cafe.beans.annotations.methods.CafeHandler;
import org.taranix.cafe.beans.annotations.methods.CafeProvider;
import org.taranix.cafe.beans.resolvers.metadata.method.SingletonHandlerMethodResolver;
import org.taranix.cafe.beans.resolvers.metadata.method.SingletonWireMethodResolver;

class MethodResolverTest {

    // --- SingletonHandlerMethodResolver.supports ---

    @Test
    @DisplayName("SingletonHandlerMethodResolver: supports the annotation it was constructed with")
    void handlerResolverSupportsConfiguredAnnotation() {
        SingletonHandlerMethodResolver resolver = new SingletonHandlerMethodResolver(CafeHandler.class);
        Assertions.assertTrue(resolver.supports(CafeHandler.class));
    }

    @Test
    @DisplayName("SingletonHandlerMethodResolver: does not support a different annotation")
    void handlerResolverDoesNotSupportOtherAnnotation() {
        SingletonHandlerMethodResolver resolver = new SingletonHandlerMethodResolver(CafeHandler.class);
        Assertions.assertFalse(resolver.supports(CafeProvider.class));
    }

    // --- SingletonWireMethodResolver.supports ---

    @Test
    @DisplayName("SingletonWireMethodResolver: supports @CafeProvider (marked with @CafeWiringType)")
    void wireMethodResolverSupportsCafeProvider() {
        Assertions.assertTrue(new SingletonWireMethodResolver().supports(CafeProvider.class));
    }

    @Test
    @DisplayName("SingletonWireMethodResolver: does not support @CafeHandler (not @CafeWiringType)")
    void wireMethodResolverDoesNotSupportCafeHandler() {
        Assertions.assertFalse(new SingletonWireMethodResolver().supports(CafeHandler.class));
    }

    // --- SingletonHandlerMethodResolver: bean auto-registered with EventHub ---

    @Test
    @DisplayName("SingletonHandlerMethodResolver: @CafeHandler bean is resolved and context initialises")
    void handlerBeanResolvedSuccessfully() {
        CafeApplicationContext ctx = CafeApplicationContext.builder()
                .withClass(HandlerClass.class)
                .build();
        ctx.initialize();
        Assertions.assertNotNull(ctx.getInstance(HandlerClass.class));
    }

    // --- SingletonWireMethodResolver: @CafeProvider stores result in repository ---

    @Test
    @DisplayName("SingletonWireMethodResolver: @CafeProvider return value is stored and retrievable")
    void providerMethodResultStoredInRepository() {
        CafeApplicationContext ctx = CafeApplicationContext.builder()
                .withClass(ProviderClass.class)
                .build();
        ctx.initialize();
        String value = ctx.getInstance(String.class);
        Assertions.assertEquals("provided", value);
    }

    @Test
    @DisplayName("SingletonWireMethodResolver: @CafeProvider method is only called once (singleton)")
    void providerMethodCalledOnceForSingleton() {
        CafeApplicationContext ctx = CafeApplicationContext.builder()
                .withClass(CountingProvider.class)
                .build();
        ctx.initialize();
        ctx.getInstance(String.class);
        ctx.getInstance(String.class);
        // Counter incremented only on the first call; subsequent calls return cached value
        Assertions.assertEquals(1, CountingProvider.callCount);
    }

    // --- Fixture classes ---

    @CafeSingleton
    static class HandlerClass {
        @CafeHandler
        void onEvent() {}
    }

    @CafeSingleton
    static class ProviderClass {
        @CafeProvider
        String value() { return "provided"; }
    }

    @CafeSingleton
    static class CountingProvider {
        static int callCount = 0;

        @CafeProvider
        String value() {
            callCount++;
            return "counted";
        }
    }
}
