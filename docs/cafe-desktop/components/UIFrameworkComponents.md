# Cafe Desktop — Components

Companion to [UIFrameworkDesign.md](../UIFrameworkDesign.md).  
See also: [menu/UIFrameworkMenu.md](../menu/UIFrameworkMenu.md) — menu contributions per component.

---

## Core abstraction: Component

**Everything is a Component.** The single contract:

```java
interface Component {
    Widget create(Control parent);
}
```

Sub-interfaces specialise by role:

```
Component
  ├── ContainerComponent             holds child components
  │     └── ApplicationComponent    root — owns Shell, event loop
  └── PageComponent                  leaf content (Editor / Viewer) — marker, TBD
```

---

## Component interface — full API

```java
interface Component {
    Widget create(Control parent);
    default boolean closable()                    { return true; }
    default Optional<MenuModel> menu()            { return Optional.empty(); }
    default Optional<MenuModel> contextMenu()     { return Optional.empty(); }
    default Set<String> availableActions()        { return Set.of(); }
}
```

| Method | Purpose |
|---|---|
| `create(parent)` | Creates and returns the SWT widget for this component |
| `closable()` | Controls startup visibility and close behaviour — see below |
| `menu()` | Main menu bar contributions — scanned by `MenuRegistry` |
| `contextMenu()` | Right-click menu local to this component's widget — scanned by `ContextMenuRegistry` |
| `availableActions()` | Action strings available right now — used by registries for enabled/disabled state |

---

## Visibility and lifecycle — `closable()`

`closable()` is the single source of truth for both startup visibility and close behaviour.

| `closable()` | Shown at startup | Can be closed | Example |
|---|---|---|---|
| `false` | **yes** — auto-added by framework during `execute()` | no | sidebar, toolbar |
| `true` (default) | no — must be added via `addComponent()` | yes, widget disposed and removed | editor, dialog panel |

**Close means destroyed** — widget is disposed and the component is removed from its container.
There is no hidden state; a closed component no longer exists in the UI.

> **Future:** a `hideable()` method may be added to support show/hide without destroying the widget.
> Initial design does not include this — close is the only way to remove a component from view.

---

## Component scope: Singleton vs Prototype

Every component has a **DI scope** independent of its `closable()` flag.

| Scope | DI annotation | Instances | Example |
|---|---|---|---|
| **Singleton** | `@CafeSingleton` | One per container | `FileExplorerContainer`, sidebar, toolbar |
| **Component** | `@CafeComponent` | New instance per `addComponent()` call | `EditorContainer` (one per open file), dialog panel |

`@CafeComponent` is a `cafe-desktop` annotation meta-annotated with `@CafePrototype` from `cafe-beans`.
It adds the UI lifecycle signal — the framework knows to hook `addComponent()` / `removeComponent()`
for handler registration and context menu binding.

```java
// cafe-desktop
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@CafePrototype
public @interface CafeComponent {
}
```

`closable()` describes widget lifecycle; scope describes *how many instances exist*.  
A singleton can be `closable() = true` — shown on demand, but always the same object.  
A `@CafeComponent` is always `closable() = true` — it only exists while it has a widget.

### Singletons

Straightforward — the DI container creates one instance, injects it everywhere.
`MenuRegistry` and `ContextMenuRegistry` receive it in their `List<Component>` at startup.
Event handlers are registered once and live for the application lifetime.

### Prototypes — the problem

Prototype instances do not exist at DI initialisation time. This breaks three things:

| Problem | Detail |
|---|---|
| **Menu scanning** | `MenuRegistry` and `ContextMenuRegistry` inject `List<Component>` at startup. Prototype instances are absent — their menu contributions are invisible to the registries. |
| **Event handler registration** | The instance must be registered with `CafeEventHandlerHub` after `create()`, and unregistered on close. Nothing does this automatically. |
| **`availableActions()` / `updateState()`** | Works fine — called on the live instance at click time. Not a problem. |

### Prototype — proposed approach

**Menu scanning** — `menu()` and `contextMenu()` are type-level declarations (same model regardless of
instance state). Register by type at bootstrap before any instance exists:

```java
// bootstrap — prototype type registered by class, not instance
menuRegistry.register(EditorContainer.class, editorMenu);
// OR via properties — always works, no instance needed
// cafe.menu.EditorContainer.File.Save = save
```

**Event handler registration / unregistration** — the framework hooks into `addComponent()` and
`removeComponent()`. The user never calls `register` / `unregister` directly:

```
ContainerComponent.addComponent(component):
  widget = component.create(parent)
  cafeEventHandlerHub.register(component)        ← registers @CafeEventHandler methods
  contextMenuRegistry.bind(component, widget)    ← attaches context menu

ContainerComponent.removeComponent(component):
  cafeEventHandlerHub.unregister(component)      ← prevents memory leak + stale dispatch
  widget.dispose()
```

> **Open:** `@CafePrototype` annotation not yet defined in `cafe-beans`.
> Workaround: explicit `register()` at bootstrap or properties-only menu config for prototype types.

---

## `ContainerComponent`

```java
interface ContainerComponent extends Component {
    void    addComponent(Component component);
    boolean removeComponent(Component component);
    Component selected();
}
```

A container manages a collection of child components and knows which one is active.
`removeComponent` is responsible for disposing the widget when `closable()` is true.
The rendering of children is an implementation detail — the framework treats `ContainerComponent` as a black box.

---

## `ApplicationComponent`

```java
interface ApplicationComponent extends ContainerComponent {
    void start();
    void shutDown();
    ContainerComponent      getActiveContainer();
    void                    setLogicallyActive(ContainerComponent container);
    Set<ContainerComponent> getContainers();
    <T extends Component> Set<T> getComponent(Class<T> componentType);
}
```

The application is itself a container. It owns the SWT `Shell` and event loop, holds all
`ContainerComponent` instances, and exposes lifecycle (`start` / `shutDown`) and lookup.

`logicallyActive` is tracked separately from SWT focus — menu clicks do not change it.
See [UIFrameworkEvents.md](../events/UIFrameworkEvents.md) for how this feeds dispatch.

---

## `PageComponent`

Marker interface. Leaf content: **Editor** (read + write + dirty) and **Viewer** (read-only).
Not yet implemented — placeholder for the next design step.

---

## Object graph at runtime

```
ApplicationComponent  (Shell)
  └── ContainerComponent           (CTabFolder)
        ├── PageComponent A        (e.g. Editor<Document>)
        └── PageComponent B        (e.g. Viewer<TableModel>)
```

Menu bar and status bar are outside the component tree — managed by `MenuRegistry` directly.

---

## Container registration

### DI registration

```java
@CafeSingleton                                          // permanent — one instance, always present
class FileExplorerContainer implements ContainerComponent { ... }

@CafeComponent                                          // prototype — new instance per addComponent()
class EditorContainer implements ContainerComponent { ... }
```

### Showing a component — `addComponent()`

The caller decides **when** a component becomes visible:

```java
// singleton — inject the single instance and show it
cafeDesktopApplication.addComponent(fileExplorerContainer);

// component (prototype) — container creates a new instance on addComponent()
@CafeEventHandler
void onFileOpen(FileOpenedEvent e) {
    cafeDesktopApplication.addComponent(EditorContainer.class, e.file());
}
```

No auto-discovery policy, no extra annotations — the user controls visibility through
ordinary application code.

---

## What is decided

| Topic | Decision |
|---|---|
| Universal base | `Component` — everything (container, leaf) implements it |
| Container | `ContainerComponent` — holds children, tracks active |
| Root | `ApplicationComponent extends ContainerComponent` — owns Shell + event loop |
| Leaf content | `PageComponent` — marker for Editor / Viewer (not yet implemented) |
| Logical active | `ApplicationComponent` tracks logically active component; menu clicks do not change it |
| Bean wiring | `@CafeSingleton` for permanent components; `@CafeComponent` for UI prototype components |
| `@CafeComponent` | `cafe-desktop` annotation, meta-annotated with `@CafePrototype`; signals framework lifecycle hooks |
| Component menu registration | `@CafeComponent` types register via properties or explicit `register()` — absent from DI-injected list |
| Component handler lifecycle | Framework owns `register()` / `unregister()` via `addComponent()` / `removeComponent()` |
| `closable()` | `false` → auto-shown at startup, permanent; `true` → on-demand, destroyed on close |
| Close semantics | Close = destroyed — widget disposed, component removed from container, no hidden state |
| Startup auto-add | Framework adds all `ContainerComponent` beans where `closable() == false` during `execute()` |

## What is open

| Topic | Status |
|---|---|
| `@CafePrototype` / `@CafeSingleton` | Defined in design — not yet implemented; see [cafe-beans/di/BeanScopeDesign.md](../../cafe-beans/di/BeanScopeDesign.md) |
| `@CafeComponent` | Defined in design — not yet implemented in `cafe-desktop` |
| `PageComponent` / Editor / Viewer | Marker exists, no implementation yet |
| DataSource wiring | How does a `PageComponent` get its data at startup? |
