package org.taranix.cafe.beans;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.annotations.methods.CafeHandler;
import org.taranix.cafe.beans.exceptions.CafeBeansContextException;
import org.taranix.cafe.beans.metadata.CafeMethod;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@DisplayName("Integration tests for CafeHandlersService - Handler Execution")
class CafeHandlersServiceTest {

    @Test
    @DisplayName("Should execute a simple no-argument handler")
    void shouldExecuteHandler() {
        // given
        CafeApplicationContext cafeApplicationContext = CafeApplicationContext
                .builder()
                .withClass(CafeHandlersServiceTextFixture.SingletonHandlerService.class)
                .build();

        // when
        cafeApplicationContext.initialize();
        CafeMethod generateMethod = cafeApplicationContext.getBeansFactory()
                .getCafeMetadataRegistry()
                .getClassMetadata(CafeHandlersServiceTextFixture.SingletonHandlerService.class)
                .getMethod("generate");
        Object result = cafeApplicationContext.executeHandler(CafeHandler.class);

        // then
        Assertions.assertNotNull(generateMethod);
        Assertions.assertNotNull(result);
        Assertions.assertEquals("String created by generate method", result);
    }

    @Test
    @DisplayName("Should execute a named no-argument handler")
    void shouldExecuteNamedHandler() {
        // given
        CafeApplicationContext cafeApplicationContext = CafeApplicationContext
                .builder()
                .withClass(CafeHandlersServiceTextFixture.SingletonHandlerService.class)
                .build();

        // when
        cafeApplicationContext.initialize();
        CafeMethod namedGenerateMethod = cafeApplicationContext.getBeansFactory()
                .getCafeMetadataRegistry()
                .getClassMetadata(CafeHandlersServiceTextFixture.SingletonHandlerService.class)
                .getMethod("namedGenerate");
        Object result = cafeApplicationContext.executeHandler("other", CafeHandler.class);

        // then
        Assertions.assertNotNull(namedGenerateMethod);
        Assertions.assertNotNull(result);
        Assertions.assertEquals("String created by namedGenerate method", result);
    }

    @Test
    @DisplayName("Should execute a single-parameter handler")
    void shouldExecuteSingleParametrizedHandler() {
        // given
        CafeApplicationContext cafeApplicationContext = CafeApplicationContext
                .builder()
                .withClass(CafeHandlersServiceTextFixture.SingletonSingleParametrizedHandlerService.class)
                .build();

        // when
        cafeApplicationContext.initialize();
        CafeMethod generateMethod = cafeApplicationContext.getBeansFactory()
                .getCafeMetadataRegistry()
                .getClassMetadata(CafeHandlersServiceTextFixture.SingletonSingleParametrizedHandlerService.class)
                .getMethod("generate", BeanTypeKey.from(String.class));
        Object result = cafeApplicationContext.executeHandler(CafeHandler.class, "parametrized:");

        // then
        Assertions.assertNotNull(generateMethod);
        Assertions.assertNotNull(result);
        Assertions.assertEquals("parametrized:String created by generate method", result);
    }

    @Test
    @DisplayName("Should execute a named single-parameter handler")
    void shouldExecuteSingleParametrizedNamedHandler() {
        // given
        CafeApplicationContext cafeApplicationContext = CafeApplicationContext
                .builder()
                .withClass(CafeHandlersServiceTextFixture.SingletonSingleParametrizedHandlerService.class)
                .build();

        // when
        cafeApplicationContext.initialize();
        CafeMethod generateMethod = cafeApplicationContext.getBeansFactory()
                .getCafeMetadataRegistry()
                .getClassMetadata(CafeHandlersServiceTextFixture.SingletonSingleParametrizedHandlerService.class)
                .getMethod("namedGenerate", BeanTypeKey.from(String.class));
        Object result = cafeApplicationContext.executeHandler("other", CafeHandler.class, "parametrized:");

        // then
        Assertions.assertNotNull(generateMethod);
        Assertions.assertNotNull(result);
        Assertions.assertEquals("parametrized:String created by namedGenerate method", result);
    }

    @Test
    @DisplayName("Should execute a generic handler with Integer parameter")
    void shouldExecuteGenericParametrizedHandler() {
        // given
        CafeApplicationContext context = CafeApplicationContext.builder()
                .withClass(CafeHandlersServiceTextFixture.GenericParametrizedSingletonHandlerService.class)
                .build();

        // when
        context.initialize();
        Object result = context.executeHandler(CafeHandler.class, 10);

        // then
        // Logic: input + 1
        Assertions.assertEquals(11, result);
    }

    @Test
    @DisplayName("Should execute a named generic handler with Integer parameter")
    void shouldExecuteGenericParametrizedNamedHandler() {
        // given
        CafeApplicationContext context = CafeApplicationContext.builder()
                .withClass(CafeHandlersServiceTextFixture.GenericParametrizedSingletonHandlerService.class)
                .build();

        // when
        context.initialize();
        Object result = context.executeHandler("other", CafeHandler.class, 10);

        // then
        // Logic: input + 2
        Assertions.assertEquals(12, result);
    }

    @Test
    @DisplayName("Should execute a multi-parameter handler (String, Date)")
    void shouldExecuteMultiParametrizedHandler() {
        // given
        CafeApplicationContext context = CafeApplicationContext.builder()
                .withClass(CafeHandlersServiceTextFixture.SingletonMultiParametrizedHandlerService.class)
                .build();
        Date now = new Date();

        // when
        context.initialize();
        Object result = context.executeHandler(CafeHandler.class, "Prefix: ", now);

        // then
        String expected = "Prefix: String created by generate method at" + now.getTime();
        Assertions.assertEquals(expected, result);
    }

    @Test
    @DisplayName("Should execute a generic single-parameter handler (List<String>)")
    void shouldExecuteSingleGenericParametrizedHandler() {
        // given
        CafeApplicationContext context = CafeApplicationContext.builder()
                .withClass(CafeHandlersServiceTextFixture.SingletonSingleGenericParametrizedHandlerService.class)
                .build();
        List<String> list = new FixedList(List.of("1", "2", "3"));


        // when
        context.initialize();
        Object result = context.executeHandler(CafeHandler.class, list);

        // then
        String expected = "1,2,3";
        Assertions.assertEquals(expected, result);
    }

    @Test
    @Disabled
    @DisplayName("Should throw exception when named handler has an Object parameter type")
    void shouldExecuteMultiParametrizedNamedHandler() {
        // given
        // The fixture contains a method 'namedGenerate' which uses 'Object anything' as a parameter
        CafeApplicationContext context = CafeApplicationContext.builder()
                .withClass(CafeHandlersServiceTextFixture.SingletonMultiParametrizedHandlerService.class)
                .build();

        // when then
        // Expecting the framework to throw CafeBeansContextException due to the Object parameter type
        CafeBeansContextException exception = Assertions.assertThrows(
                org.taranix.cafe.beans.exceptions.CafeBeansContextException.class,
                context::initialize);

        // Verify the exception message matches the expected format provided in the requirement
        String expectedMessage = "Handler's parameter can not by an Object (Objects: (Method) org.taranix.cafe.beans.CafeHandlersServiceTextFixture.SingletonMultiParametrizedHandlerService:namedGenerate)";
        Assertions.assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @Disabled
    @DisplayName("Should execute a generic single-parameter handler (List<String>)")
    void shouldExecuteSingleInheritedParametrizedHandler() {
        // given
        CafeApplicationContext context = CafeApplicationContext.builder()
                .withClass(CafeHandlersServiceTextFixture.SingletonSingleInheritedParametrizedHandlerService.class)
                .build();
        CafeHandlersServiceTextFixture.GreetingsService payload = new CafeHandlersServiceTextFixture.EnglishGreetingsService();

        // when
        context.initialize();
        Object result = context.executeHandler(CafeHandler.class, payload);

        // then
        String expected = payload.getGreetings();
        Assertions.assertEquals(expected, result);
    }

    static class FixedList extends ArrayList<String> {
        FixedList(Collection<String> c) {
            super(c);
        }
    }
}