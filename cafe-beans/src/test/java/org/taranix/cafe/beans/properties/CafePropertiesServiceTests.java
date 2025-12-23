package org.taranix.cafe.beans.properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.CafeApplicationContext;
import org.taranix.cafe.beans.CafePropertiesService;
import org.taranix.cafe.beans.converters.CafeConverter;
import org.taranix.cafe.beans.repositories.beans.BeansRepository;

import java.nio.file.Path;
import java.util.Properties;

class CafePropertiesServiceTests {

    @Test
    void shouldGetPropertiesFromClassPath() {
        //given
        CafePropertiesService context = CafePropertiesService.load(new BeansRepository());

        //when
        Properties properties = context.getProperties();

        //then
        Assertions.assertEquals("Black Cats", properties.getProperty("test.property"));
        Assertions.assertEquals("Red Cats", properties.getProperty("test.property.2"));
    }

    @Test
    void shouldReturnCurrentPath() {
        //given
        CafePropertiesService context = CafePropertiesService.load(new BeansRepository());

        //when
        Path currentPath = context.getCurrentPath();

        //then
        Assertions.assertNotNull(currentPath);
    }

    @Test
    void shouldInjectPropertyIntoServiceClass() {
        CafeApplicationContext context = CafeApplicationContext.builder()
                .withPackageScan(CafeConverter.class.getPackageName())
                .withClass(ServiceClassWithProperty.class)
                .build();

        context.initialize();

        ServiceClassWithProperty instance = context.getInstance(ServiceClassWithProperty.class);

        Assertions.assertNotNull(instance);
        Assertions.assertEquals("Black Cats", instance.getProperty());
    }

}
