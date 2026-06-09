package org.taranix.cafe.beans.app;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.CafeApplication;
import org.taranix.cafe.beans.annotations.methods.CafeHandler;
import org.taranix.cafe.beans.events.EventHub;
import org.taranix.cafe.beans.events.HandlerMethodInvoker;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class CafeApplicationTests {

    @Test
    void shouldRunApplicationWithPostInitMethod() {
        CafeApplication testApp = create(CafeApplicationConfigurationWithPostConstructMethod.class);
        testApp.run(new String[]{"Hello", "app"});
    }

    @Test
    @DisplayName("HandlerMethodInvoker should be available as an injectable bean after context init")
    void shouldExposeHandlerMethodInvokerAsBean() {
        CafeApplication app = create(CafeApplicationConfigurationWithPostConstructMethod.class);
        HandlerMethodInvoker invoker = app.getInstance(HandlerMethodInvoker.class);
        assertNotNull(invoker);
    }

    @Test
    @DisplayName("EventHub should be available as an injectable bean after context init")
    void shouldExposeEventHubAsBean() {
        CafeApplication app = create(CafeApplicationConfigurationWithPostConstructMethod.class);
        EventHub hub = app.getInstance(EventHub.class);
        assertNotNull(hub);
    }

    @Test
    @DisplayName("EventHub should have a CafeHandler dispatcher pre-registered")
    void shouldHaveCafeHandlerDispatcherInEventHub() {
        CafeApplication app = create(CafeApplicationConfigurationWithPostConstructMethod.class);
        EventHub hub = app.getInstance(EventHub.class);
        assertNotNull(hub.dispatcher(CafeHandler.class));
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
