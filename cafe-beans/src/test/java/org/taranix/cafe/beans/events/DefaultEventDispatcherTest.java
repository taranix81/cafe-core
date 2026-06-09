package org.taranix.cafe.beans.events;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.annotations.methods.CafeHandler;
import org.taranix.cafe.beans.repositories.beans.BeansRepository;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests for DefaultEventDispatcher")
class DefaultEventDispatcherTest {

    private HandlerMethodInvoker invoker;
    private DefaultEventDispatcher<CafeHandler> dispatcher;

    @BeforeEach
    void setUp() {
        invoker = new HandlerMethodInvoker(new BeansRepository());
        dispatcher = new DefaultEventDispatcher<>(CafeHandler.class, invoker);
    }

    @Test
    @DisplayName("Should not throw when send() is called with no handlers registered")
    void shouldNotThrowOnSendWithNoHandlers() {
        assertDoesNotThrow(() -> dispatcher.send("arg1", "arg2"));
    }

    @Test
    @DisplayName("Should not throw when sendTo() is called with no handlers registered")
    void shouldNotThrowOnSendToWithNoHandlers() {
        assertDoesNotThrow(() -> dispatcher.sendTo(new Object(), "arg1"));
    }

    @Test
    @DisplayName("Should add listener via register() without throwing")
    void shouldRegisterListener() {
        Object listener = new Object();
        assertDoesNotThrow(() -> dispatcher.register(listener));
    }

    @Test
    @DisplayName("Should remove listener via unregister() without throwing")
    void shouldUnregisterListener() {
        Object listener = new Object();
        dispatcher.register(listener);
        assertDoesNotThrow(() -> dispatcher.unregister(listener));
    }

    @Test
    @DisplayName("Should allow registering and unregistering the same listener multiple times")
    void shouldHandleRepeatedRegisterUnregister() {
        Object listener = new Object();
        dispatcher.register(listener);
        dispatcher.register(listener);
        dispatcher.unregister(listener);
        assertDoesNotThrow(() -> dispatcher.send());
    }
}
