package org.taranix.cafe.beans.app;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.CafeApplication;

@Slf4j
class CafeApplicationTests {

    @Test
    void shouldRunApplicationWithPostInitMethod() {
        CafeApplication testApp = create(CafeApplicationConfigurationWithPostConstructMethod.class);
        testApp.run(new String[]{"Hello", "app"});

    }

    private CafeApplication create(Class<?> configClass) {
        return new CafeApplication(configClass) {


            @Override
            protected int execute(String[] args) {
                log.debug("{}", (Object) args);
                return 0;
            }
        };
    }

}
