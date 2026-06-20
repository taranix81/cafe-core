package org.taranix.cafe.beans.events;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.annotations.methods.CafeHandler;
import org.taranix.cafe.beans.repositories.beans.BeansRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests for DefaultEventDispatcher")
class DefaultEventDispatcherTest {

    private DefaultEventDispatcher<CafeHandler> dispatcher;

    @BeforeEach
    void setUp() {
        dispatcher = new DefaultEventDispatcher<>(CafeHandler.class);
    }

    @Test
    @DisplayName("send() with no subscribers does not throw")
    void shouldNotThrowOnSendWithNoSubscribers() {
        assertDoesNotThrow(() -> dispatcher.send("arg1"));
    }

    @Test
    @DisplayName("sendTo() with no subscribers does not throw")
    void shouldNotThrowOnSendToWithNoHandlers() {
        assertDoesNotThrow(() -> dispatcher.sendTo(new Object(), "arg1"));
    }

    @Test
    @DisplayName("addIfRelevant() ignores object with no @CafeHandler methods")
    void shouldNotAddListenerWithNoHandlerMethods() {
        Object noHandlers = new Object();
        assertDoesNotThrow(() -> dispatcher.addIfRelevant(noHandlers));
        // send() with no subscribers — no exception, no dispatch
        assertDoesNotThrow(() -> dispatcher.send());
    }

    @Test
    @DisplayName("addIfRelevant() adds object that has a @CafeHandler method")
    void shouldAddListenerWithHandlerMethod() {
        List<String> received = new ArrayList<>();
        Object listener = new Object() {
            @CafeHandler
            public void onEvent(String msg) { received.add(msg); }
        };

        dispatcher.addIfRelevant(listener);
        dispatcher.send("hello");

        assertEquals(List.of("hello"), received);
    }

    @Test
    @DisplayName("send() dispatches to all registered subscribers (fan-out)")
    void shouldDispatchToAllSubscribers() {
        List<String> log = new ArrayList<>();
        Object a = new Object() {
            @CafeHandler
            public void handle(String s) { log.add("A:" + s); }
        };
        Object b = new Object() {
            @CafeHandler
            public void handle(String s) { log.add("B:" + s); }
        };

        dispatcher.addIfRelevant(a);
        dispatcher.addIfRelevant(b);
        dispatcher.send("x");

        assertTrue(log.contains("A:x"));
        assertTrue(log.contains("B:x"));
    }

    @Test
    @DisplayName("sendTo() dispatches only to the specified target")
    void shouldSendToSpecificTarget() {
        List<String> log = new ArrayList<>();
        Object target = new Object() {
            @CafeHandler
            public void handle(String s) { log.add("TARGET:" + s); }
        };
        Object other = new Object() {
            @CafeHandler
            public void handle(String s) { log.add("OTHER:" + s); }
        };

        dispatcher.addIfRelevant(target);
        dispatcher.addIfRelevant(other);
        dispatcher.sendTo(target, "y");

        assertEquals(List.of("TARGET:y"), log);
    }

    @Test
    @DisplayName("send() skips args that do not match handler parameter types")
    void shouldSkipIncompatibleArgs() {
        List<Object> received = new ArrayList<>();
        Object listener = new Object() {
            @CafeHandler
            public void onString(String s) { received.add(s); }
        };

        dispatcher.addIfRelevant(listener);
        dispatcher.send(42);   // Integer, not String — should not dispatch

        assertTrue(received.isEmpty());
    }

    @Test
    @DisplayName("handler exception does not prevent other subscribers from receiving the event")
    void shouldContinueAfterHandlerException() {
        List<String> log = new ArrayList<>();
        Object failing = new Object() {
            @CafeHandler
            public void handle(String s) { throw new RuntimeException("boom"); }
        };
        Object succeeding = new Object() {
            @CafeHandler
            public void handle(String s) { log.add(s); }
        };

        dispatcher.addIfRelevant(failing);
        dispatcher.addIfRelevant(succeeding);
        assertDoesNotThrow(() -> dispatcher.send("z"));
        assertEquals(List.of("z"), log);
    }

    @Test
    @DisplayName("legacy constructor with HandlerMethodInvoker delegates to invoker on send()")
    void legacyConstructorDelegatesToInvoker() {
        HandlerMethodInvoker invoker = new HandlerMethodInvoker(new BeansRepository());
        DefaultEventDispatcher<CafeHandler> legacy =
                new DefaultEventDispatcher<>(CafeHandler.class, invoker);
        // With an empty repository the invoker call is a no-op — should not throw
        assertDoesNotThrow(() -> legacy.send("arg"));
    }
}
