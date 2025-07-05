package org.taranix.cafe.beans.scanner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.CafeApplicationContext;
import org.taranix.cafe.beans.ClassScanner;
import org.taranix.cafe.beans.annotations.CafeAnnotationUtils;
import org.taranix.cafe.beans.descriptors.CafeClassDescriptors;

public class ClassScannerTests {

    @Test
    void shouldFindAllAnnotatedClasses() {
        //given
        ClassScanner classScanner = ClassScanner.from(CafeAnnotationUtils.BASE_ANNOTATIONS);
        //when
        CafeClassDescriptors descriptors = CafeClassDescriptors.builder()
                .withAnnotations(CafeAnnotationUtils.BASE_ANNOTATIONS)
                .withClasses(classScanner.scan("org.taranix.cafe.beans.scanner"))
                .build();

        //then
        Assertions.assertNotNull(descriptors);
        Assertions.assertNotNull(descriptors.descriptor(FactoryClass.class));
        Assertions.assertNotNull(descriptors.descriptor(ServiceClass.class));
    }

    @Test
    void shouldFindAllAnnotatedClassesAndResolveTem() {
        //given
        CafeApplicationContext cafeApplicationContext = CafeApplicationContext
                .builder()
                .withAnnotations(CafeAnnotationUtils.BASE_ANNOTATIONS)
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
