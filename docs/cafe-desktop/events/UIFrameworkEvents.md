# Cafe Desktop — Event & Action Mechanism

Companion to [UIFrameworkDesign.md](../UIFrameworkDesign.md).

---

## Overview

The event mechanism spans both subprojects with a strict division of responsibility:

| Responsibility | Project |
|---|---|
| Handler registration, matching, invocation | `cafe-beans` |
| Target resolution (who receives an event) | `cafe-desktop` |

**Core rule: handler execution lives exclusively in `cafe-beans`.**
`cafe-desktop` decides *who* receives an event — it never scans, matches,
or invokes handler methods itself.

### EventHub facade + EventDispatcher instances — one entry point, many typed dispatchers

`EventHub` is a **concrete class** in `cafe-beans` — the single, globally accessible entry point
for event publishing. It is a facade that holds a registry of `EventDispatcher<A>` instances,
one per handler annotation type.

`EventDispatcher<A extends Annotation>` is the **interface** each dispatcher implements.
Each instance handles exactly one annotation type and owns its routing logic.

| Type | Role | Annotation | Lives in | Routing logic |
|---|---|---|---|---|
| `EventHub` | Global facade & dispatcher selector | — | `cafe-beans` | selects dispatcher by annotation type, then delegates |
| `DefaultEventDispatcher<CafeHandler>` | Standard dispatcher | `@CafeHandler` | `cafe-beans` | caller-explicit (`send` / `sendTo`) |
| `CafeEventHandlerHub` | Desktop dispatcher | `@CafeEventHandler` | `cafe-desktop` | hub-owned: menu routing, active-component routing |

`CafeEventHandlerHub` extends `EventDispatcher<CafeEventHandler>` and adds desktop-specific
dispatch methods. Triggers call these methods directly — no separate distributor service needed.

```
menu SelectionListener
        │
        └─→ CafeEventHandlerHub.dispatchMenuEvent(event, menuItem)
                │  resolves: logicallyActive + menuItem.listeners
                │
                └─→ HandlerMethodInvoker  ← dispatches @CafeEventHandler methods

embedded control (table row, button inside a component)
        │
        └─→ CafeEventHandlerHub.sendToActive(event)
                │  resolves: logicallyActive
                │
                └─→ HandlerMethodInvoker

ApplicationComponent.publish() / general component event
        │
        └─→ CafeEventHandlerHub.send(event)  or  sendToActive(event)

─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─

general bean-to-bean event (no UI targeting needed)
        │
        └─→ EventHub.get(CafeHandler.class).send(event)
          or EventHub.get(CafeHandler.class).sendTo(target, event)
                │
                └─→ HandlerMethodInvoker  ← dispatches @CafeHandler methods
```

### EventHub as event-publishing facade

`EventHub` is the global, singleton publishing facade. It is a **concrete class** — not an
interface — that holds a registry of `EventDispatcher<A>` instances keyed by annotation type.
**`EventHub` is responsible for selecting the right `EventDispatcher`** for a given call — the
caller never reaches into the registry directly.

```java
// EventHub selects the dispatcher and delegates
eventHub.send(CafeHandler.class, new DataLoadedEvent<>(data));
eventHub.sendTo(CafeHandler.class, target, new DataLoadedEvent<>(data));

// Desktop-specific routing (dispatchMenuEvent, sendToActive) requires the typed dispatcher
// — inject CafeEventHandlerHub directly, or access via eventHub.dispatcher(CafeEventHandler.class)
```

Each `EventDispatcher<A>` owns all routing decisions for its annotation type before
delegating to `HandlerMethodInvoker`.
Callers that always use the same annotation type inject the concrete dispatcher directly
(e.g. `CafeEventHandlerHub`) — no need to go through `EventHub`.

Routing decisions have three independent dimensions:

| Dimension | Resolved by | Mechanism |
|---|---|---|
| **Annotation type** `A` | `EventHub` (selects dispatcher) or direct dispatcher injection | Each dispatcher is bound to a single `A` — wrong dispatcher means no match |
| **Arguments** | `CafeHandlerSelector` inside `HandlerMethodInvoker` | Matches handlers whose parameter types are compatible with the provided args |
| **Target** (`send` vs `sendTo`) | Caller or dispatcher's routing logic | `send` = broadcast to all matching handlers; `sendTo` = restrict to one instance |

`CafeEventHandlerHub` adds a fourth dimension unique to the desktop:
`dispatchMenuEvent` and `sendToActive` resolve the target from application state
(`logicallyActive`) so the trigger never needs to know which component is active.

---

## Event type design

### Decision — dedicated event types

| Approach | Routing | Type safety | Fit with infrastructure |
|---|---|---|---|
| Generic event + properties | Manual — every handler inspects a type string | None | Fights `CafeHandlerSelector` — all handlers match, need a second filter layer |
| **Dedicated event types** | Automatic — `CafeHandlerSelector` routes by parameter type | Full | Natural fit — no extra layer needed |

**Decision: dedicated event types.**

`CafeHandlerSelector` matches handlers by parameter type. Publishing `new SaveEvent()`
automatically reaches only `@CafeHandler void onSave(SaveEvent e)` methods.

### Event type rules

- Every event is a plain Java class — no required base class or interface.
- Events are immutable value objects — carry only the data the handler needs.
- Generic events are allowed where the payload type matters: `DataLoadedEvent<T>`.
- No SWT types in event classes — events are SWT-independent.
- Menu-triggered events **must be no-arg** — `MenuItemEventDispatcher` instantiates them
  via reflection; parameterised constructors cannot be satisfied at dispatch time.

```java
public record SaveEvent() {}
public record RowSelectedEvent(TableRow row) {}
public record DataLoadedEvent<T>(T data) {}
public record ApplicationClosingEvent() {}
```

### Event categories

| Category | Examples |
|---|---|
| Data | `DataLoadedEvent<T>`, `DataChangedEvent<T>`, `DataSavedEvent<T>` |
| Selection | `RowSelectedEvent`, `ItemSelectedEvent` |
| Component lifecycle | `ComponentOpenedEvent`, `ComponentClosedEvent` |
| Application | `ApplicationClosingEvent` |

---

## cafe-beans

### Architecture

The `cafe-beans` event mechanism is a standalone observer/dispatcher. It has three concerns:

1. **Listener registration** — any object can register as a listener; `@CafeService` beans are registered automatically by the DI container at startup.
2. **Handler matching** — when `send` is called, the dispatcher finds all `@CafeHandler` methods on registered listeners whose parameter types are compatible with the provided arguments.
3. **Invocation** — all matched handlers are called; for targeted sends, only handlers on the specified instance are considered.

```
EventDispatcher.send(arg1, arg2, …)
  → find all @CafeHandler methods on registered listeners
      where method parameter types match (arg1.class, arg2.class, …)
  → invoke each

EventDispatcher.sendTo(target, arg1, arg2, …)
  → same matching, but restricted to methods on target instance only
```

---

### `@CafeHandler` — annotation for `cafe-beans` dispatcher

`@CafeHandler` marks handler methods in `cafe-beans`. It is the annotation type bound
to the `cafe-beans` `DefaultEventDispatcher<CafeHandler>` instance.

```java
// cafe-beans
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@CafeHandlerType
public @interface CafeHandler { }
```

`@CafeHandler` is **not** a meta-annotation base for `@CafeEventHandler`.
The two annotations are independent — each bound to its own `EventDispatcher` instance.
`CafeHandlerSelector` requires no meta-annotation traversal change.

**Matching rules (both dispatchers share this logic via `HandlerMethodInvoker`):**
- Argument count in the `send` call must equal the handler's parameter count.
- Each argument must be type-compatible with the corresponding parameter (subtype allowed).
- A 0-arg handler matches only `send()` with no arguments — not a catch-all.

---

### `EventDispatcher<A extends Annotation>`

The per-annotation dispatcher — generic, typed to a specific handler annotation.
Each instance handles exactly one annotation type and owns its routing logic.

```java
public interface EventDispatcher<A extends Annotation> {
    void register(Object listener);

    void unregister(Object listener);

    void send(Object... args);                         // broadcast — all matching handlers

    void sendTo(Object target, Object... args);        // targeted — only handlers on target
}
```

`send` and `sendTo` are two distinct methods — no overload ambiguity with the argument list.
The split directly maps to the **target** routing dimension: `send` = broadcast, `sendTo` = targeted.
See [Routing dimensions](#eventhub-as-event-publishing-facade) in the Overview.

`DefaultEventDispatcher<A>` is the standard implementation — constructed with
`Class<A> annotationType` and delegates all dispatching to `HandlerMethodInvoker`.
The annotation type is the only thing that differs between instances.

---

### `EventHub` — global facade & dispatcher selector

`EventHub` is a **concrete class** — the globally accessible entry point for event publishing.
It holds a registry of `EventDispatcher` instances keyed by annotation type and is responsible
for **selecting the right dispatcher** for each call.

```java
// cafe-beans
public class EventHub {
    // Primary API — EventHub selects the dispatcher, then delegates
    public <A extends Annotation> void send(Class<A> annotationType, Object... args);
    public <A extends Annotation> void sendTo(Class<A> annotationType, Object target, Object... args);

    // Direct dispatcher access — for desktop-specific methods (dispatchMenuEvent, sendToActive)
    public <A extends Annotation> EventDispatcher<A> dispatcher(Class<A> annotationType);

    // Registration (called at bootstrap)
    public <A extends Annotation> void register(Class<A> annotationType, EventDispatcher<A> dispatcher);
}
```

At bootstrap, one `EventDispatcher` is created per annotation type and registered with `EventHub`.
Callers that always use the same annotation type and need type-specific methods inject the
concrete dispatcher directly (e.g. `CafeEventHandlerHub`) — no need to go through `EventHub`.
Callers that need annotation-agnostic publishing inject `EventHub`.

**DI disambiguation:** each dispatcher is now a distinct type in the registry:
- `DefaultEventDispatcher<CafeHandler>` is added to the context explicitly at bootstrap
- `CafeEventHandlerHub` (named subinterface of `EventDispatcher<CafeEventHandler>`) resolves unambiguously by its own interface type
- `EventHub` (the facade class) resolves unambiguously — there is only one instance

This resolves OpenQuestions 1.11.

---

### `HandlerMethodInvoker` — required additions

The existing `dispatch()` returns after the first match and is not used by `EventDispatcher`.
Two new methods are needed:

| New method | Behaviour |
|---|---|
| `dispatchAll(annotationType, args)` | fan-out — invokes **all** matching `@CafeHandler` methods across all registered listeners |
| `dispatchTo(annotationType, target, args)` | targeted — invokes matching methods on `target` instance only (identity check) |

---

### Listener registration

Each dispatcher registers only the listeners relevant to its annotation type:
- `DefaultEventDispatcher<CafeHandler>` — receives beans that contain `@CafeHandler` methods
- `CafeEventHandlerHub` — receives beans that contain `@CafeEventHandler` methods

`@CafeService` beans are registered automatically at startup via the existing
`SingletonHandlerMethodResolver` mechanism (one resolver instance per annotation type).
No explicit `register()` call needed in application code.

For non-DI objects (e.g. a short-lived component or a test double), `register()` /
`unregister()` are called explicitly on the appropriate dispatcher.

### Singleton vs Prototype — handler lifecycle

See [UIFrameworkDesign.md — Component scope](../UIFrameworkDesign.md) for the full definition.
The event-relevant difference is handler registration lifetime:

| Scope | Registration | Unregistration |
|---|---|---|
| **Singleton** | Automatic at startup via `SingletonHandlerMethodResolver` | Never — lives for the application lifetime |
| **Prototype** | Manual — framework calls `register()` after `create()` | Manual — framework calls `unregister()` on close |

**Prototype registration flow** — the framework hooks `ContainerComponent.addComponent()` and
`removeComponent()` so the user never calls `register` / `unregister` directly:

```
ContainerComponent.addComponent(component):
  widget = component.create(parent)
  cafeEventHandlerHub.register(component)          ← registers @CafeEventHandler methods
  contextMenuRegistry.bind(component, widget)

ContainerComponent.removeComponent(component):
  cafeEventHandlerHub.unregister(component)        ← removes all handlers for this instance
  widget.dispose()
```

A prototype only registers with `CafeEventHandlerHub` — prototype UI components carry
`@CafeEventHandler` methods, not `@CafeHandler` methods (which belong to pure service beans).

**Why unregister matters:** `CafeEventHandlerHub` holds strong references to registered listeners.
A closed prototype that is not unregistered leaks memory and may receive events after its widget
is disposed — both are bugs.

---

### Infrastructure (existing)

| Class | Role |
|---|---|
| `@CafeHandler` | marks a method as a handler — already exists, no changes |
| `HandlerTypeKey` | repository key — stores parameter types and annotations |
| `CafeHandlerSignature` | holds method + instance to invoke |
| `HandlerMethodInvoker` | finds matching handlers and invokes them |
| `CafeHandlerSelector` | matches by annotation type + parameter types |

---

### Required changes in `cafe-beans`

- Add `EventDispatcher<A extends Annotation>` interface
- Add `DefaultEventDispatcher<A>` implementation — constructor takes `Class<A> annotationType` + `HandlerMethodInvoker`
- Add `EventHub` concrete class — global facade, selector; exposes `send(annotationType, args)` / `sendTo(annotationType, target, args)` / `dispatcher(annotationType)`
- Add `dispatchAll()` and `dispatchTo()` to `HandlerMethodInvoker`
- Rename `CafeHandlerExecutorService` → `HandlerMethodInvoker` in source
- Expose `getHandlerMethodInvoker()` on `CafeApplication` (consistent with existing `getBeansFactory()`)
- `CafeHandlerSelector` — no changes needed (each dispatcher passes its own annotation type directly)

---

## cafe-desktop

### `CafeEventHandlerHub`

The desktop dispatcher. Extends `EventDispatcher<CafeEventHandler>` with dispatch methods that own
the routing logic — no separate distributor service is needed.

```java
// cafe-desktop
public interface CafeEventHandlerHub extends EventDispatcher<CafeEventHandler> {

    // inherited — caller-explicit targeting (used when the caller knows the target)
    // void send(Object... args);
    // void sendTo(Object target, Object... args);

    // desktop-specific — hub resolves the target
    void sendToActive(Object... args);                             // routes to logicallyActive
    void dispatchMenuEvent(Object event, MenuItemModel menuItem);  // routes via listeners + logicallyActive
}
```

`MenuItemEventDispatcher` injects `ApplicationComponent` to read `logicallyActive`.
`ApplicationComponent` does **not** inject the hub — this breaks the potential cycle.
Components that want to fire events inject `CafeEventHandlerHub` directly
(see `ApplicationComponent.publish()` decision below).

---

### Target resolution — inside `MenuItemEventDispatcher`

```
dispatchMenuEvent(event, menuItem):
  menuItem.listeners is empty  → send(event)                      // broadcast
  menuItem.listeners non-empty → active = getActiveContainer()
                                 active.getClass() ∈ listeners
                                   → sendTo(active, event)
                                 active.getClass() ∉ listeners
                                   → no dispatch (item was greyed)

sendToActive(args):
  active = getActiveContainer()
  active != null → sendTo(active, args)
  active == null → no dispatch
```

```java
class MenuItemEventDispatcher implements CafeEventHandlerHub {
    private final HandlerMethodInvoker executorService;
    private final ApplicationComponent application;

    @Override
    public void dispatchMenuEvent(Object event, MenuItemModel menuItem) {
        if (menuItem.listeners().isEmpty()) {
            send(event);
        } else {
            ContainerComponent active = application.getActiveContainer();
            if (active != null && menuItem.listeners().contains(active.getClass())) {
                sendTo(active, event);
            }
        }
    }

    @Override
    public void sendToActive(Object... args) {
        ContainerComponent active = application.getActiveContainer();
        if (active != null) {
            sendTo(active, args);
        }
    }

    @Override
    public void send(Object... args) {
        executorService.dispatchAll(CafeEventHandler.class, args);
    }

    @Override
    public void sendTo(Object target, Object... args) {
        executorService.dispatchTo(CafeEventHandler.class, target, args);
    }
}
```

---

### Triggers

Each trigger calls the hub directly with the appropriate method.
No intermediate distributor service.

**Menu item trigger:**
```java
// inside SelectionListener on a menu item
Object event = menuItem.eventType().getDeclaredConstructor().newInstance();
cafeEventHandlerHub.dispatchMenuEvent(event, menuItem);
```

**Embedded control (table row, button inside a component):**
```java
// inside a control's listener — fires to logicallyActive (its own container)
cafeEventHandlerHub.sendToActive(new RowSelectedEvent(row));
```

**General component event (broadcast):**
```java
cafeEventHandlerHub.send(new DataLoadedEvent<>(data));
```

New trigger types (toolbar, keyboard shortcut) call the appropriate hub method:
`dispatchMenuEvent` if the trigger carries a `MenuItemModel`,
`sendToActive` if the trigger is embedded in a component,
`send` for application-wide events.

---

### Logical active vs SWT focus

`ApplicationComponent` tracks `logicallyActive` separately from SWT focus.
`Menu` is not a `Control` in SWT — it fires no focus events — so menu clicks
never affect `logicallyActive`.

```
User activates editor tab   → logicallyActive = EditorContainer   ✓
User clicks menu item       → SWT focus = menu (not a Control)
                              logicallyActive = EditorContainer (unchanged) ✓
User activates sidebar      → logicallyActive = SidebarContainer  ✓
```

`logicallyActive` is set by `ContainerComponent` implementations via
`applicationComponent.setLogicallyActive(this)` on tab selection or mouse-down.

---

### `ApplicationComponent` — event API

```java
interface ApplicationComponent extends ContainerComponent {
    void setLogicallyActive(ContainerComponent container);  // called by containers on user interaction
    ContainerComponent getActiveContainer();                 // returns logicallyActive
}
```

`publish(Object event)` is **removed**. It was a facade over the hub, but since `CafeEventHandlerHub`
is an injectable `@CafeService`, components that need to fire events inject it directly.
Keeping `publish()` would require `ApplicationComponent` to inject `CafeEventHandlerHub`,
creating a cycle (`ApplicationComponent` ← hub ← `ApplicationComponent`).

Components fire events by:
```java
@CafeService
class EditorContainer implements ContainerComponent {
    @CafeInject CafeEventHandlerHub hub;

    void onTableRowClicked(TableRow row) {
        hub.sendToActive(new RowSelectedEvent(row));   // targets this container via logicallyActive
    }
}
```

---

### Menu item bridge

`MenuItemModel.eventType` maps the action string to a no-arg event class:

```java
class MenuItemModel {
    String   label;
    String   action;                            // "save"
    Class<?> eventType;                         // SaveEvent.class — must be no-arg
    List<Class<? extends Component>> listeners; // empty = broadcast; non-empty = targeted
    boolean  enabled;
    boolean  visible;
}
```

---

### `@CafeEventHandler` — annotation for the desktop hub

Independent annotation bound to `CafeEventHandlerHub` (`EventDispatcher<CafeEventHandler>`). No relationship to
`@CafeHandler` — the dispatcher boundary replaces any need for meta-annotation inheritance.

```java
// cafe-desktop
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CafeEventHandler { }
```

Usage in a UI component:
```java
@CafeEventHandler
void onSave(SaveEvent e) { ... }

@CafeEventHandler
void onRowSelected(RowSelectedEvent e) { ... }
```

These methods are invisible to `DefaultEventDispatcher<CafeHandler>` and vice-versa.

---

### Required changes in `cafe-desktop`

- Add `@CafeEventHandler` annotation (independent — no meta-annotation)
- Add `CafeEventHandlerHub` interface (extends `EventDispatcher<CafeEventHandler>`)
- Add `MenuItemEventDispatcher` implementation (injects `ApplicationComponent`)
- Register `MenuItemEventDispatcher` as a bean at bootstrap; register with `EventHub` facade
- Implement `setLogicallyActive()` in `DefaultApplicationComponent` (remove `publish()`)
- Wire menu `SelectionListener` → `cafeEventHandlerHub.dispatchMenuEvent(event, menuItem)`
- Wire embedded controls → `cafeEventHandlerHub.sendToActive(event)`

### What is retired

| Class | Reason |
|---|---|
| `HandlersService` | ID-map routing — replaced by `CafeEventHandlerHub.dispatchMenuEvent()` |
| `HandlerSignature` (cafe-desktop) | Duplicates `CafeHandlerSignature` from `cafe-beans` |
| `@CafeMenuItemSelectionHandler` | Replaced by `MenuItemModel.eventType` + `@CafeEventHandler` |
| `CafeMenuItemSelectionMethodResolver` | Only needed by `HandlersService` |
| `ActionBus` | Dead code — `handlers` and `broadcast()` commented out |
| `EventBus` (components_old) | SWT-event-centric, disabled (`@CafeService` commented out) |
| `@CafeActionHandler` | Defined but never used |

`@CafeShellHandler` + shell handler map in `HandlersService` may survive in reduced form
for shell lifecycle events (`shellClosed`) that do not fit the component event model.

---

## Bootstrap sequence

```
CafeDesktopApplication.beforeContextInit()
  addBeanToContext(getBeansFactory())                          // already present
  addBeanToContext(getHandlerMethodInvoker())                  // NEW — exposes HandlerMethodInvoker

  // create EventHub (selector facade) and DefaultEventDispatcher<CafeHandler>; add both to context
  EventHub eventHub = new EventHub()
  EventDispatcher<CafeHandler> cafeHandlerDispatcher =
      new DefaultEventDispatcher<>(CafeHandler.class, getHandlerMethodInvoker())
  eventHub.register(CafeHandler.class, cafeHandlerDispatcher)
  addBeanToContext(eventHub)
  addBeanToContext(cafeHandlerDispatcher)

  // CafeEventHandlerHub is a @CafeService — DI resolves it via constructor injection;
  // it registers itself with EventHub on creation (or at post-init)

DI resolves:
  MenuItemEventDispatcher   ← HandlerMethodInvoker + ApplicationComponent + EventHub
  DefaultApplicationComponent  ← (no dispatcher dependency — removes publish())

Listener auto-registration:
  beans with @CafeHandler methods      → registered with DefaultEventDispatcher<CafeHandler>
  beans with @CafeEventHandler methods → registered with CafeEventHandlerHub
  (via SingletonHandlerMethodResolver, one resolver instance per annotation type)
```

---

## Known gaps

| Gap | Project | Detail |
|---|---|---|
| `EventDispatcher<A>` interface + `DefaultEventDispatcher<A>` | `cafe-beans` | Not yet created |
| `EventHub` concrete class (global facade) | `cafe-beans` | Not yet created |
| `HandlerMethodInvoker.dispatchAll()` | `cafe-beans` | Not yet implemented |
| `HandlerMethodInvoker.dispatchTo()` | `cafe-beans` | Not yet implemented |
| `CafeHandlerExecutorService` → rename to `HandlerMethodInvoker` in source | `cafe-beans` | Not yet renamed |
| `CafeApplication.getHandlerMethodInvoker()` | `cafe-beans` | Not yet exposed |
| `CafeHandlerFindService.find()` | `cafe-beans` | Returns `Set.of()` — broken |
| `@CafeEventHandler` annotation | `cafe-desktop` | Not yet created |
| `CafeEventHandlerHub` interface + `MenuItemEventDispatcher` | `cafe-desktop` | Not yet created |
