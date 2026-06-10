package org.taranix.cafe.beans.properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.CafeApplicationContext;
import org.taranix.cafe.beans.converters.CafeConverter;
import org.taranix.cafe.beans.repositories.beans.BeansRepository;
import org.taranix.cafe.beans.resolvers.CafePropertiesService;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Enumeration;
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
    void shouldLoadWithNoClasspathFilesWithoutException() {
        ClassLoader emptyLoader = new ClassLoader(null) {
            @Override
            public InputStream getResourceAsStream(String name) { return null; }
            @Override
            public URL getResource(String name) { return null; }
            @Override
            public Enumeration<URL> getResources(String name) { return java.util.Collections.emptyEnumeration(); }
        };
        CafePropertiesService context = CafePropertiesService.load(new BeansRepository(), emptyLoader);
        Assertions.assertNotNull(context.getProperties());
    }

    @Test
    void shouldResolveNestedYamlKeyThreeLevelsDeep() {
        CafePropertiesService context = CafePropertiesService.load(new BeansRepository());
        Properties properties = context.getProperties();
        Assertions.assertEquals("value", properties.getProperty("edge.a.b.c"));
    }

    @Test
    void shouldLoadNumericYamlValueAsNonNull() {
        CafePropertiesService context = CafePropertiesService.load(new BeansRepository());
        Properties properties = context.getProperties();
        Object port = properties.get("edge.port");
        Assertions.assertNotNull(port);
        Assertions.assertEquals(8080, port);
    }

    @Test
    void shouldLoadBooleanYamlValueAsNonNull() {
        CafePropertiesService context = CafePropertiesService.load(new BeansRepository());
        Properties properties = context.getProperties();
        Object debug = properties.get("edge.debug");
        Assertions.assertNotNull(debug);
        Assertions.assertEquals(true, debug);
    }

    @Test
    void shouldMergePropertiesFileAndYmlWhenBothPresent() {
        CafePropertiesService context = CafePropertiesService.load(new BeansRepository());
        Properties properties = context.getProperties();
        // from application.properties
        Assertions.assertNotNull(properties.getProperty("test.property"));
        // from application.yml
        Assertions.assertNotNull(properties.get("family.dad"));
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
