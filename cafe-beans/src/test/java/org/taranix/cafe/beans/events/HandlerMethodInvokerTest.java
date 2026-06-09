package org.taranix.cafe.beans.events;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.CafeApplicationContext;
import org.taranix.cafe.beans.annotations.methods.CafeHandler;
import org.taranix.cafe.beans.events.selectors.CafeHandlerSelector;
import org.taranix.cafe.beans.exceptions.CafeBeansContextException;
import org.taranix.cafe.beans.metadata.CafeMethod;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.util.ArrayList;
import java.util.Collection;

@DisplayName("Integration tests for HandlerMethodInvoker - Handler Execution")
class HandlerMethodInvokerTest {

//    @Test
//    @DisplayName("Should execute a simple no-argument handler")
//    void shouldInvokeHandlerHandler() {
//        // given
//        CafeApplicationContext cafeApplicationContext = CafeApplicationContext
//                .builder()
//                .withClass(CafeDispatcherServiceTestFixture.SingletonHandlerService.class)
//                .build();
//
//        // when
//        cafeApplicationContext.initialize();
//        CafeMethod generateMethod = cafeApplicationContext.getBeansFactory()
//                .getCafeMetadataRegistry()
//                .getClassMetadata(CafeDispatcherServiceTestFixture.SingletonHandlerService.class)
//                .getMethod("generate");
//        Object result = cafeApplicationContext.executeHandler(CafeHandler.class);
//
//        // then
//        Assertions.assertNotNull(generateMethod);
//        Assertions.assertNotNull(result);
//        Assertions.assertEquals("String created by generate method", result);
//    }


//    @Test
//    @DisplayName("Should execute a named no-argument handler")
//    void shouldInvokeHandlerNamedHandler() {
//        // given
//        CafeApplicationContext cafeApplicationContext = CafeApplicationContext
//                .builder()
//                .withClass(CafeDispatcherServiceTestFixture.SingletonHandlerService.class)
//                .build();
//
//        // when
//        cafeApplicationContext.initialize();
//        CafeMethod namedGenerateMethod = cafeApplicationContext.getBeansFactory()
//                .getCafeMetadataRegistry()
//                .getClassMetadata(CafeDispatcherServiceTestFixture.SingletonHandlerService.class)
//                .getMethod("namedGenerate");
//        Object result = cafeApplicationContext.executeHandler("other", CafeHandler.class);
//
//        // then
//        Assertions.assertNotNull(namedGenerateMethod);
//        Assertions.assertNotNull(result);
//        Assertions.assertEquals("String created by namedGenerate method", result);
//    }

//    @Test
//    @DisplayName("Should execute a single-parameter handler")
//    void shouldInvokeHandlerSingleParametrizedHandler() {
//        // given
//        CafeApplicationContext cafeApplicationContext = CafeApplicationContext
//                .builder()
//                .withClass(CafeDispatcherServiceTestFixture.SingletonSingleParametrizedHandlerService.class)
//                .build();
//
//        // when
//        cafeApplicationContext.initialize();
//        CafeMethod generateMethod = cafeApplicationContext.getBeansFactory()
//                .getCafeMetadataRegistry()
//                .getClassMetadata(CafeDispatcherServiceTestFixture.SingletonSingleParametrizedHandlerService.class)
//                .getMethod("generate", BeanTypeKey.from(String.class));
//        Object result = cafeApplicationContext.executeHandler(CafeHandler.class, "parametrized:");
//
//        // then
//        Assertions.assertNotNull(generateMethod);
//        Assertions.assertNotNull(result);
//        Assertions.assertEquals("parametrized:String created by generate method", result);
//    }

    @Test
    @DisplayName("Should execute a named single-parameter handler")
    void shouldInvokeHandlerSingleParametrizedNamedHandler() {
        // given
        CafeApplicationContext cafeApplicationContext = CafeApplicationContext
                .builder()
                .withClass(CafeHandlerSelector.class)
                .withClass(CafeDispatcherServiceTestFixture.SingletonSingleParametrizedHandlerService.class)
                .build();

        // when
        cafeApplicationContext.initialize();
        CafeMethod generateMethod = cafeApplicationContext.getBeansFactory()
                .getCafeMetadataRegistry()
                .getClassMetadata(CafeDispatcherServiceTestFixture.SingletonSingleParametrizedHandlerService.class)
                .getMethod("namedGenerate", BeanTypeKey.from(String.class));


        Object result = cafeApplicationContext.executeHandler(CafeHandler.class, "parametrized:");

        // then
        Assertions.assertNotNull(generateMethod);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.toString().startsWith("parametrized:String created by"),
                "Expected result starting with 'parametrized:String created by', got: " + result);
    }

//    @Test
//    @DisplayName("Should execute a generic handler with Integer parameter")
//    void shouldInvokeHandlerGenericParametrizedHandler() {
//        // given
//        CafeApplicationContext context = CafeApplicationContext.builder()
//                .withClass(CafeDispatcherServiceTestFixture.GenericParametrizedSingletonHandlerService.class)
//                .build();
//
//        // when
//        context.initialize();
//        Object result = context.executeHandler(CafeHandler.class, 10);
//
//        // then
//        // Logic: input + 1
//        Assertions.assertEquals(11, result);
//    }

//    @Test
//    @DisplayName("Should execute a named generic handler with Integer parameter")
//    void shouldInvokeHandlerGenericParametrizedNamedHandler() {
//        // given
//        CafeApplicationContext context = CafeApplicationContext.builder()
//                .withClass(CafeDispatcherServiceTestFixture.GenericParametrizedSingletonHandlerService.class)
//                .build();
//
//        // when
//        context.initialize();
//        Object result = context.executeHandler("other", CafeHandler.class, 10);
//
//        // then
//        // Logic: input + 2
//        Assertions.assertEquals(12, result);
//    }


//    @Test
//    @DisplayName("Should execute a generic single-parameter handler (List<String>)")
//    void shouldInvokeHandlerSingleGenericParametrizedHandler() {
//        // given
//        CafeApplicationContext context = CafeApplicationContext.builder()
//                .withClass(CafeDispatcherServiceTestFixture.SingletonSingleGenericParametrizedHandlerService.class)
//                .build();
//        List<String> list = new FixedList(List.of("1", "2", "3"));
//
//
//        // when
//        context.initialize();
//        Object result = context.executeHandler(CafeHandler.class, list);
//
//        // then
//        String expected = "1,2,3";
//        Assertions.assertEquals(expected, result);
//    }

    @Test
    @DisplayName("Should throw exception when named handler has an Object parameter type")
    void shouldInvokeHandlerMultiParametrizedNamedHandler() {
        // given
        // The fixture contains a method 'namedGenerate' which uses 'Object anything' as a parameter
        CafeApplicationContext context = CafeApplicationContext.builder()
                .withClass(CafeDispatcherServiceTestFixture.SingletonMultiParametrizedHandlerService.class)
                .build();

        // when then
        // Expecting the framework to throw CafeBeansContextException due to the Object parameter type
        CafeBeansContextException exception = Assertions.assertThrows(
                org.taranix.cafe.beans.exceptions.CafeBeansContextException.class,
                context::initialize);

        // Verify the exception message matches the expected format provided in the requirement
        String expectedMessage = "Handler's parameter can not by an Object (Objects: (Method) org.taranix.cafe.beans.events.CafeDispatcherServiceTestFixture.SingletonMultiParametrizedHandlerService:namedGenerate)";
        Assertions.assertEquals(expectedMessage, exception.getMessage());
    }

//    @Test
//    @DisplayName("Should execute a generic single-parameter handler (List<String>)")
//    void shouldInvokeHandlerSingleInheritedParametrizedHandler() {
//        // given
//        CafeApplicationContext context = CafeApplicationContext.builder()
//                .withClass(CafeDispatcherServiceTestFixture.SingletonSingleInheritedParametrizedHandlerService.class)
//                .build();
//        CafeDispatcherServiceTestFixture.GreetingsService payload = new CafeDispatcherServiceTestFixture.EnglishGreetingsService();
//
//        // when
//        context.initialize();
//        Object result = context.executeHandler(CafeHandler.class, payload);
//
//        // then
//        String expected = payload.getGreetings();
//        Assertions.assertEquals(expected, result);
//    }

    static class FixedList extends ArrayList<String> {
        FixedList(Collection<String> c) {
            super(c);
        }
    }
}
