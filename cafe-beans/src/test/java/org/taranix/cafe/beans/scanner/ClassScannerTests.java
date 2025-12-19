package org.taranix.cafe.beans.scanner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.CafeApplicationContext;
import org.taranix.cafe.beans.metadata.CafeMetadataRegistry;
import org.taranix.cafe.beans.reflection.ClassScanner;

public class ClassScannerTests {

    @Test
    void shouldFindAllAnnotatedClasses() {
        //given
        ClassScanner classScanner = ClassScanner.getInstance();
        //when
        CafeMetadataRegistry descriptors = CafeMetadataRegistry.builder()
                .withClasses(classScanner.scan("org.taranix.cafe.beans.scanner"))
                .build();

        //then
        Assertions.assertNotNull(descriptors);
        Assertions.assertNotNull(descriptors.getClassMetadata(ServiceClass.class));
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
        Assertions.assertNotNull(cafeApplicationContext.getBeansFactory().getCafeMetadataRegistry().getClassMetadata(FactoryClass.class));
        Assertions.assertNotNull(cafeApplicationContext.getBeansFactory().getCafeMetadataRegistry().getClassMetadata(ServiceClass.class));

        Assertions.assertNotNull(factory);
        Assertions.assertNotNull(service);
        Assertions.assertEquals(13L, service.getMagicNumber());
    }

}
