package org.taranix.cafe.beans.scanner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.CafeApplicationContext;
import org.taranix.cafe.beans.reflection.ClassScanner;
import org.taranix.cafe.beans.services.CafeBeanDefinitionService;

public class ClassScannerTests {

    @Test
    void shouldFindAllAnnotatedClasses() {
        //given
        ClassScanner classScanner = ClassScanner.getInstance();
        //when
        CafeBeanDefinitionService descriptors = CafeBeanDefinitionService.builder()
                .withClasses(classScanner.scan("org.taranix.cafe.beans.scanner"))
                .build();

        //then
        Assertions.assertNotNull(descriptors);
        Assertions.assertNotNull(descriptors.findClassInfo(FactoryClass.class));
        Assertions.assertNotNull(descriptors.findClassInfo(ServiceClass.class));
    }

    @Test
    void shouldFindAllAnnotatedClassesAndResolveTem() {
        //given
        CafeApplicationContext cafeApplicationContext = CafeApplicationContext
                .builder()
                .withPackageScan("org.taranix.cafe.beans.scanner").build();
        //when
        cafeApplicationContext.initialize();
        FactoryClass factory = cafeApplicationContext.getInstance(FactoryClass.class);
        ServiceClass service = cafeApplicationContext.getInstance(ServiceClass.class);

        //then

        Assertions.assertNotNull(cafeApplicationContext);
        Assertions.assertNotNull(cafeApplicationContext.getClassDescriptor(FactoryClass.class));
        Assertions.assertNotNull(cafeApplicationContext.getClassDescriptor(ServiceClass.class));

        Assertions.assertNotNull(factory);
        Assertions.assertNotNull(service);
        Assertions.assertEquals(13L, service.getMagicNumber());
    }

}
