package org.taranix.cafe.beans.events;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

@DisplayName("Unit tests for EventHub")
class EventHubTest {

    private EventHub hub;

    @BeforeEach
    void setUp() {
        hub = new EventHub();
    }

    // --- register / unregister ---

//    @Test
//    @DisplayName("registered listener receives matching event")
//    void shouldDispatchToRegisteredListener() {
//        List<TestEvent> received = new ArrayList<>();
//        Object listener = new Object() {
//            @CafeHandler
//            public void on(TestEvent e) {
//                received.add(e);
//            }
//        };
//        hub.register(listener);
//        TestEvent event = new TestEvent();
//        hub.send(event);
//        assertEquals(List.of(event), received);
//    }
//
//    @Test
//    @DisplayName("unregistered listener no longer receives events")
//    void shouldNotDispatchAfterUnregister() {
//        List<TestEvent> received = new ArrayList<>();
//        Object listener = new Object() {
//            @CafeHandler
//            public void on(TestEvent e) {
//                received.add(e);
//            }
//        };
//        hub.register(listener);
//        hub.unregister(listener);
//        hub.send(new TestEvent());
//        assertTrue(received.isEmpty());
//    }
//
//    @Test
//    @DisplayName("listener with no @CafeHandler methods is ignored silently")
//    void shouldIgnoreListenerWithNoHandlers() {
//        assertDoesNotThrow(() -> {
//            hub.register(new Object());
//            hub.send(new TestEvent());
//        });
//    }
//
//    // --- Matching: exact event type ---
//
//    @Test
//    @DisplayName("handler for parent type is NOT matched when subtype is sent (exact type match)")
//    void shouldNotMatchSubtype() {
//        List<CafeEvent> received = new ArrayList<>();
//        Object listener = new Object() {
//            @CafeHandler
//            public void on(CafeEvent e) {
//                received.add(e);
//            }
//        };
//        hub.register(listener);
//        hub.send(new TestEvent()); // TestEvent extends CafeEvent — must NOT match CafeEvent handler
//        assertTrue(received.isEmpty());
//    }
//
//    @Test
//    @DisplayName("handler for exact event type is matched")
//    void shouldMatchExactType() {
//        List<TestEvent> received = new ArrayList<>();
//        Object listener = new Object() {
//            @CafeHandler
//            public void on(TestEvent e) {
//                received.add(e);
//            }
//        };
//        hub.register(listener);
//        hub.send(new TestEvent());
//        assertEquals(1, received.size());
//    }
//
//    // --- Matching: id equality ---
//
//    @Test
//    @DisplayName("bare @CafeHandler with empty id matches event with empty id")
//    void shouldMatchEmptyIds() {
//        List<TestEvent> received = new ArrayList<>();
//        Object listener = new Object() {
//            @CafeHandler
//            public void on(TestEvent e) {
//                received.add(e);
//            }
//        };
//        hub.register(listener);
//        hub.send(new TestEvent(""));
//        assertEquals(1, received.size());
//    }
//
//    @Test
//    @DisplayName("@CafeHandler with id matches event with the same id")
//    void shouldMatchSameId() {
//        List<NamedEvent> received = new ArrayList<>();
//        Object listener = new Object() {
//            @CafeHandler(id = "ORDER")
//            public void on(NamedEvent e) {
//                received.add(e);
//            }
//        };
//        hub.register(listener);
//        hub.send(new NamedEvent("ORDER"));
//        assertEquals(1, received.size());
//    }
//
//    @Test
//    @DisplayName("@CafeHandler with id does NOT match event with different id")
//    void shouldNotMatchDifferentId() {
//        List<NamedEvent> received = new ArrayList<>();
//        Object listener = new Object() {
//            @CafeHandler(id = "ORDER")
//            public void on(NamedEvent e) {
//                received.add(e);
//            }
//        };
//        hub.register(listener);
//        hub.send(new NamedEvent("PAYMENT"));
//        assertTrue(received.isEmpty());
//    }

    // --- send with targetType ---

//    @Test
//    @DisplayName("send(event, targetType) only dispatches to listeners of that type")
//    void shouldDispatchOnlyToTargetType() {
//        List<String> log = new ArrayList<>();
//
//        TypeA a = new TypeA(log);
//        TypeB b = new TypeB(log);
//        hub.register(a);
//        hub.register(b);
//
//        hub.send(new TestEvent(), TypeA.class);
//
//        assertEquals(List.of("A"), log);
//    }

    // --- fan-out ---

//    @Test
//    @DisplayName("send() delivers to all matching registered listeners")
//    void shouldFanOutToAllListeners() {
//        List<String> log = new ArrayList<>();
//        hub.register(new Object() {
//            @CafeHandler
//            public void on(TestEvent e) {
//                log.add("1");
//            }
//        });
//        hub.register(new Object() {
//            @CafeHandler
//            public void on(TestEvent e) {
//                log.add("2");
//            }
//        });
//        hub.send(new TestEvent());
//        assertTrue(log.contains("1"));
//        assertTrue(log.contains("2"));
//    }

    // --- resilience ---

//    @Test
//    @DisplayName("handler exception does not prevent other listeners from receiving the event")
//    void shouldContinueAfterHandlerException() {
//        List<String> log = new ArrayList<>();
//        hub.register(new Object() {
//            @CafeHandler
//            public void on(TestEvent e) {
//                throw new RuntimeException("boom");
//            }
//        });
//        hub.register(new Object() {
//            @CafeHandler
//            public void on(TestEvent e) {
//                log.add("ok");
//            }
//        });
//        assertDoesNotThrow(() -> hub.send(new TestEvent()));
//        assertEquals(List.of("ok"), log);
//    }

    // --- Fixtures ---

//    static class TestEvent extends CafeEvent {
//        TestEvent() {
//            super("");
//        }
//
//        TestEvent(String id) {
//            super(id);
//        }
//    }

//    static class NamedEvent extends CafeEvent {
//        NamedEvent(String id) {
//            super(id);
//        }
//    }

    static class TypeA {
        final List<String> log;

        TypeA(List<String> log) {
            this.log = log;
        }

//        @CafeHandler
//        public void on(TestEvent e) {
//            log.add("A");
//        }
    }

    static class TypeB {
        final List<String> log;

        TypeB(List<String> log) {
            this.log = log;
        }

//        @CafeHandler
//        public void on(TestEvent e) {
//            log.add("B");
//        }
    }
}
