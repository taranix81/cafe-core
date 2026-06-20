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
    @DisplayName("addDispatcher() registers a dispatcher retrievable by annotation type")
    void shouldRegisterAndRetrieveDispatcher() {
        EventDispatcher<CafeHandler> dispatcher = noopDispatcher();
        hub.addDispatcher(CafeHandler.class, dispatcher);
        assertSame(dispatcher, hub.dispatcher(CafeHandler.class));
    }

    @Test
    @DisplayName("dispatcher() returns null for unregistered annotation type")
    void shouldReturnNullForUnregisteredType() {
        assertNull(hub.dispatcher(CafeHandler.class));
    }

    @Test
    @DisplayName("send() routes to the registered dispatcher")
    void shouldRouteSendToRegisteredDispatcher() {
        List<Object[]> captured = new ArrayList<>();
        hub.addDispatcher(CafeHandler.class, capturingDispatcher(captured));

        hub.send(CafeHandler.class, "arg1", "arg2");

        assertEquals(1, captured.size());
        assertArrayEquals(new Object[]{"arg1", "arg2"}, captured.get(0));
    }

    @Test
    @DisplayName("sendTo() routes to the registered dispatcher with the target")
    void shouldRouteSendToToRegisteredDispatcher() {
        List<Object[]> captured = new ArrayList<>();
        Object target = new Object();

        hub.addDispatcher(CafeHandler.class, new EventDispatcher<>() {
            public void addIfRelevant(Object l) {}
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
    @DisplayName("send() does not throw for unregistered annotation type")
    void shouldNotThrowOnUnregisteredSend() {
        assertDoesNotThrow(() -> hub.send(CafeHandler.class, "arg1"));
    }

    @Test
    @DisplayName("sendTo() does not throw for unregistered annotation type")
    void shouldNotThrowOnUnregisteredSendTo() {
        assertDoesNotThrow(() -> hub.sendTo(CafeHandler.class, new Object(), "arg1"));
    }

    @Test
    @DisplayName("addDispatcher() supports multiple annotation types independently")
    void shouldSupportMultipleDispatchers() {
        EventDispatcher<CafeHandler> d1 = noopDispatcher();
        EventDispatcher<Override> d2 = noopDispatcher();

        hub.addDispatcher(CafeHandler.class, d1);
        hub.addDispatcher(Override.class, d2);

        assertSame(d1, hub.dispatcher(CafeHandler.class));
        assertSame(d2, hub.dispatcher(Override.class));
    }

    @Test
    @DisplayName("register(listener) fans out addIfRelevant() to all dispatchers")
    void shouldFanOutRegisterToAllDispatchers() {
        List<Object> receivedByD1 = new ArrayList<>();
        List<Object> receivedByD2 = new ArrayList<>();

        hub.addDispatcher(CafeHandler.class, new EventDispatcher<>() {
            public void addIfRelevant(Object l) { receivedByD1.add(l); }
            public void send(Object... args) {}
            public void sendTo(Object t, Object... args) {}
        });
        hub.addDispatcher(Override.class, new EventDispatcher<>() {
            public void addIfRelevant(Object l) { receivedByD2.add(l); }
            public void send(Object... args) {}
            public void sendTo(Object t, Object... args) {}
        });

        Object listener = new Object();
        hub.register(listener);

        assertEquals(List.of(listener), receivedByD1);
        assertEquals(List.of(listener), receivedByD2);
    }

    // --- Helpers ---

    private <A extends Annotation> EventDispatcher<A> noopDispatcher() {
        return new EventDispatcher<>() {
            public void addIfRelevant(Object l) {}
            public void send(Object... args) {}
            public void sendTo(Object target, Object... args) {}
        };
    }

    private EventDispatcher<CafeHandler> capturingDispatcher(List<Object[]> captured) {
        return new EventDispatcher<>() {
            public void addIfRelevant(Object l) {}
            public void send(Object... args) { captured.add(args); }
            public void sendTo(Object target, Object... args) {}
        };
    }
}
