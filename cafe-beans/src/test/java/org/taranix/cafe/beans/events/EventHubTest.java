package org.taranix.cafe.beans.events;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.annotations.methods.CafeHandler;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests for EventHub")
class EventHubTest {

    private EventHub hub;

    @BeforeEach
    void setUp() {
        hub = new EventHub();
    }

    @Test
    @DisplayName("Should register a dispatcher and retrieve it by annotation type")
    void shouldRegisterAndRetrieveDispatcher() {
        EventDispatcher<CafeHandler> dispatcher = noopDispatcher();
        hub.register(CafeHandler.class, dispatcher);
        assertSame(dispatcher, hub.dispatcher(CafeHandler.class));
    }

    @Test
    @DisplayName("Should return null for unregistered annotation type")
    void shouldReturnNullForUnregisteredType() {
        assertNull(hub.dispatcher(CafeHandler.class));
    }

    @Test
    @DisplayName("Should route send() to the registered dispatcher")
    void shouldRouteSendToRegisteredDispatcher() {
        List<Object[]> captured = new ArrayList<>();
        hub.register(CafeHandler.class, capturingDispatcher(captured));

        hub.send(CafeHandler.class, "arg1", "arg2");

        assertEquals(1, captured.size());
        assertArrayEquals(new Object[]{"arg1", "arg2"}, captured.get(0));
    }

    @Test
    @DisplayName("Should route sendTo() to the registered dispatcher")
    void shouldRouteSendToToRegisteredDispatcher() {
        List<Object[]> captured = new ArrayList<>();
        Object target = new Object();

        hub.register(CafeHandler.class, new EventDispatcher<CafeHandler>() {
            public void register(Object l) {}
            public void unregister(Object l) {}
            public void send(Object... args) {}
            public void sendTo(Object t, Object... args) {
                assertSame(target, t);
                captured.add(args);
            }
        });

        hub.sendTo(CafeHandler.class, target, "payload");

        assertEquals(1, captured.size());
        assertArrayEquals(new Object[]{"payload"}, captured.get(0));
    }

    @Test
    @DisplayName("Should not throw when send() called for unregistered annotation type")
    void shouldNotThrowOnUnregisteredSend() {
        assertDoesNotThrow(() -> hub.send(CafeHandler.class, "arg1"));
    }

    @Test
    @DisplayName("Should not throw when sendTo() called for unregistered annotation type")
    void shouldNotThrowOnUnregisteredSendTo() {
        assertDoesNotThrow(() -> hub.sendTo(CafeHandler.class, new Object(), "arg1"));
    }

    @Test
    @DisplayName("Should support registering multiple annotation types independently")
    void shouldSupportMultipleDispatchers() {
        EventDispatcher<CafeHandler> d1 = noopDispatcher();
        EventDispatcher<Override> d2 = noopDispatcher();

        hub.register(CafeHandler.class, d1);
        hub.register(Override.class, d2);

        assertSame(d1, hub.dispatcher(CafeHandler.class));
        assertSame(d2, hub.dispatcher(Override.class));
    }

    // --- Helpers ---

    private <A extends Annotation> EventDispatcher<A> noopDispatcher() {
        return new EventDispatcher<>() {
            public void register(Object l) {}
            public void unregister(Object l) {}
            public void send(Object... args) {}
            public void sendTo(Object target, Object... args) {}
        };
    }

    private EventDispatcher<CafeHandler> capturingDispatcher(List<Object[]> captured) {
        return new EventDispatcher<>() {
            public void register(Object l) {}
            public void unregister(Object l) {}
            public void send(Object... args) { captured.add(args); }
            public void sendTo(Object target, Object... args) {}
        };
    }
}
