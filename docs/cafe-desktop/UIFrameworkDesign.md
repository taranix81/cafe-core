# Cafe Desktop — UI Framework Design

## Goals

A simple Java desktop UI framework built on SWT, backed by the `cafe-beans` DI container.
Priorities: low boilerplate, annotation-driven wiring, event-based decoupling between components.

---

## Module structure

```
cafe-beans          DI container — bean lifecycle, annotation scanning, event dispatch infrastructure
cafe-desktop        SWT UI layer — components, containers, layout, menu, shell integration
cafe-shell          CLI layer (separate runtime, not in scope here)
```

---

## Components

See **[components/UIFrameworkComponents.md](components/UIFrameworkComponents.md)** for the full component design.

Summary:
- Everything is a `Component` — the single contract is `create(Control parent)`.
- `ContainerComponent` holds children; `ApplicationComponent` is the root (owns Shell + event loop).
- `closable() = false` → auto-shown at startup, permanent; `closable() = true` → on-demand, destroyed on close.
- Two scopes: **Singleton** (`@CafeService`, one instance) and **Prototype** (new instance per `addComponent()`).
- Prototype handler lifecycle is framework-owned — `addComponent()` registers, `removeComponent()` unregisters.

---

## Component menus

See **[menu/UIFrameworkMenu.md](menu/UIFrameworkMenu.md)** for the full menu design.

Summary:
- `menu()` / `contextMenu()` / `availableActions()` on `Component` — declared per type.
- `MenuRegistry` owns the main bar end-to-end; `ContextMenuRegistry` owns context menus.
- Items carry a `listeners` list: empty = global, non-empty = targeted (enabled ↔ `availableActions()`).
- Prototype types registered via properties or explicit `register()` — absent from DI-injected list.

---

## Layout

### Structure

```
ApplicationComponent  (Shell)
  ├── ApplicationMenuBarComponent    (outside layout — always top)
  ├── LayoutComponent               (main area — managed by LayoutService)
  │     ├── ContainerComponent A
  │     ├── ContainerComponent B
  │     └── ContainerComponent C
  └── StatusBarComponent            (outside layout — always bottom, future)
```

Menu bar and status bar are outside the layout system.

---

### LayoutComponent

Owns the main area and places `ContainerComponent` instances within it.
The rendering is an implementation detail — framework treats it as a black box.

```java
interface LayoutComponent extends Component {
    void    addContainer(ContainerComponent container);
    boolean removeContainer(ContainerComponent container);
}
```

Framework-provided implementations:

| Class | Behaviour |
|---|---|
| `FlowLayoutComponent` | Components fill space in order — default |
| `GridLayoutComponent` | Grid-based, reads `@CafeLayout(column, row)` from containers |

---

### LayoutService

Owns all layout decisions: which `LayoutComponent` to use, how to configure it,
and where each `ContainerComponent` goes.

```java
interface LayoutService {
    LayoutComponent createLayout(Composite parent);
    void place(LayoutComponent layout, Collection<ContainerComponent> containers);
}
```

`DefaultLayoutService` is provided by the framework and uses `FlowLayoutComponent` by default.
User provides a custom implementation via `@CafeService @CafePrimary` when needed.

### Configuration — two tiers, properties first

```
1. Properties    (highest priority)
2. Programmatic  (fallback)
```

**Properties:**
```properties
cafe.layout.type               = GRID
cafe.layout.columns            = 3
cafe.layout.rows               = 2
cafe.layout.FileExplorer.column = 0
cafe.layout.FileExplorer.row    = 0
cafe.layout.Editor.column       = 1
cafe.layout.Editor.row          = 0
```

**Programmatic** — protected method in `CafeDesktopApplication` the user overrides:
```java
@Override
protected void configureLayout(LayoutService service) {
    service.setType(GridLayoutComponent.class);
    service.place(FileExplorerContainer.class, 0, 0);
    service.place(EditorContainer.class,       1, 0);
}
```

Default implementation is empty — `DefaultLayoutService` uses `FlowLayoutComponent`
and places containers in cascade order.

---

### `@CafeLayout` — container-level hint

Used by `GridLayoutComponent` to resolve a container's cell when no properties
or programmatic config is provided for it.

```java
@CafeService
@CafeLayout(column = 0, row = 0)
class FileExplorerContainer implements ContainerComponent { }

@CafeService
@CafeLayout(column = 1, row = 0, columnSpan = 2)
class EditorContainer implements ContainerComponent { }
```

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CafeLayout {
    int   column()     default 0;
    int   row()        default 0;
    int   columnSpan() default 1;
    int   rowSpan()    default 1;
    int   size()       default 0;    // fixed pixels; 0 = not set
    float weight()     default 0f;   // relative 0.0–1.0; 0 = not set
}
```

---

### Position conflicts — cascade

When multiple containers are placed in the same cell they **cascade**: all are added
to that cell's region via `addComponent`. The region `ContainerComponent` decides
how to render its children — the framework treats it as a black box.

Order within a region: properties-configured first, then programmatic, then `@CafeLayout`.

---

## Event mechanism

See **[UIFrameworkEvents.md](UIFrameworkEvents.md)** for the full event and action design.

Summary:
- `EventHub` (concrete class, `cafe-beans`) is the global facade — selects the right `EventDispatcher` by annotation type.
- Two dispatchers: `DefaultEventDispatcher<CafeHandler>` (general beans) and `CafeEventHandlerHub` (`cafe-desktop`, UI events).
- `CafeEventHandlerHub` adds desktop-specific routing: `send`, `sendToActive`, `dispatchMenuEvent`.
- `@CafeEventHandler` marks handler methods in UI components; `@CafeHandler` marks general bean handlers.
- Handler invocation always delegates to `HandlerMethodInvoker` (reflection-based) in `cafe-beans`.

---

## What is decided

| Topic | Decision |
|---|---|
| Component design | See [components/UIFrameworkComponents.md](components/UIFrameworkComponents.md) — hierarchy, scope, lifecycle, registration |
| Menu design | See [menu/UIFrameworkMenu.md](menu/UIFrameworkMenu.md) — registry, merge rule, dispatch, enabled rule |
| Event design | See [events/UIFrameworkEvents.md](events/UIFrameworkEvents.md) — hub, dispatchers, handler lifecycle |
| Communication | Event-based via `CafeEventHandlerHub` — routing logic lives in the hub (`dispatchMenuEvent`, `sendToActive`, `send`) |
| Logical active | `ApplicationComponent` tracks logically active component; menu clicks do not change it |

## What is open

| Topic | Status |
|---|---|
| Layout mechanism | Options A / B / C above — not chosen |
| Event bus public API | Infrastructure exists in `cafe-beans`, surface not finalised |
| `components_old/` | ~80 legacy classes still present — migration or removal pending |
| `ComponentFactory` | Has wrong import (`components_old.Component`) — needs fix |
