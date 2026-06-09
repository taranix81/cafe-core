# Cafe Desktop — Menu Design

Companion to [UIFrameworkDesign.md](../UIFrameworkDesign.md).  
See also: [UIFrameworkMenu.puml](UIFrameworkMenu.puml) — class diagram.

---

## Component menus

`MenuRegistry` is the single component responsible for the entire menu lifecycle:

| Responsibility | Detail |
|---|---|
| **Scanning** | All `Component` beans are injected by the DI container; `DefaultMenuRegistry` scans them at construction time via `menu()` / properties |
| **Selection wiring** | Attaches `SelectionListener` to each SWT `MenuItem`; on click, instantiates `MenuItemModel.eventType` and publishes via `EventHub` |
| **State management** | Greys out / enables items by calling `availableActions()` on the logically active component whenever the active component changes |

`MenuRegistry` owns everything end-to-end — model and SWT bar.

### API on `Component`

```java
interface Component {
    Widget create(Control parent);
    default boolean closable()                    { return true; }
    default Optional<MenuModel> menu()            { return Optional.empty(); }
    default Optional<MenuModel> contextMenu()     { return Optional.empty(); }
    default Set<String> availableActions()        { return Set.of(); }
}
```

`menu()` — items this component contributes to the main menu bar, registered with `MenuRegistry`.  
`contextMenu()` — right-click menu attached to the component's own widget; purely local, not merged.  
`availableActions()` — returns the set of `action` strings this component can handle **right now**.
`MenuRegistry` calls this whenever the logically active component changes to compute the enabled state
of each menu item. Default is empty (no targeted items enabled for this component).

---

### MenuRegistry

`MenuRegistry` is the global controller for the menu bar. It is a `@CafeService` — the DI container
injects all `Component` beans automatically, so no manual scanning is needed.

```java
@CafeService
class DefaultMenuRegistry implements MenuRegistry {
    DefaultMenuRegistry(List<Component> components) {
        // DI injects every Component bean; scan them all at construction time
        components.forEach(c -> c.menu().ifPresent(m -> merge(c.getClass(), m)));
    }
}
```

```java
interface MenuRegistry {
    // Programmatic registration (alternative / supplement to menu())
    void register(Class<? extends Component> componentType, MenuModel menu);

    // Composed model
    MenuModel composed();

    // Builds the SWT menu bar on the shell and attaches SelectionListeners
    void buildMenuBar(Shell shell);

    // State update — called when the logically active component changes
    void updateState(Component activeComponent);
}
```

All **singleton** `Component` beans are injected and scanned at startup.
Prototype components require separate handling — see [Singleton vs Prototype](#singleton-vs-prototype) below.

Registration is **type-only**: the registry stores `Class<? extends Component>` references, never instances.

---

### Registration and merge rule

When a component type registers its `MenuModel`, the registry walks each item:

- Top-level menu (`File`) already exists → reuse it, no duplicate
- Item (`File > Save`) already exists → **do not duplicate; add the component type to that item's listener list**
- Item does not exist → create it, add component type as the sole listener

```
EditorContainer.class  registers  File[Save, SaveAs]  Edit[Undo, Redo]
PdfViewer.class        registers  File[Save]

Registry result:
  File > Save     listeners: [EditorContainer.class, PdfViewer.class]
  File > SaveAs   listeners: [EditorContainer.class]
  Edit > Undo     listeners: [EditorContainer.class]
  Edit > Redo     listeners: [EditorContainer.class]
  File > Exit     listeners: [ApplicationComponent.class]   ← always enabled
  Help > About    listeners: [ApplicationComponent.class]   ← always enabled
```

Registering a type that is already registered is a no-op — idempotent.

---

### Selection wiring

`MenuRegistry.buildMenuBar(shell)` creates the SWT bar and attaches a `SelectionListener` to every `MenuItem`:

```
SelectionListener fires
  → event = menuItem.eventType().getDeclaredConstructor().newInstance()
  → eventHub.send(CafeEventHandler.class, event)
      → CafeEventHandlerHub.dispatchMenuEvent(event, menuItemModel)
          → resolves logicallyActive, checks listeners, calls sendTo / send
```

`MenuItemModel.eventType` must be a **no-arg class** — `MenuRegistry` instantiates it via reflection.
See [UIFrameworkEvents.md](../events/UIFrameworkEvents.md) for the full dispatch flow inside the hub.

---

### Enabled / disabled — `updateState`

`MenuRegistry.updateState(activeComponent)` is called whenever `logicallyActive` changes.
It iterates every `MenuItemModel` in the composed bar and sets the SWT `MenuItem` enabled flag:

```
For each MenuItemModel:
  listeners is empty
    → always enabled

  active.getClass() ∈ listeners
    → enabled ↔ active.availableActions().contains(item.action)

  active.getClass() ∉ listeners
    → disabled
```

`availableActions()` is the single source of truth for dynamic state — the component reports
which of its registered actions are usable at this moment (e.g. `save` only when dirty,
`undo` only when the undo stack is non-empty).

```java
// Example — EditorContainer
@Override
public Set<String> availableActions() {
    Set<String> actions = new HashSet<>(List.of("save-as", "undo", "redo"));  // always available
    if (isDirty()) actions.add("save");
    return actions;
}
```

### Click dispatch

```
User clicks a menu item  (item is enabled — updateState already ran)
  → SelectionListener fires (wired by MenuRegistry.bindMenuItems)
  → eventHub.send(CafeEventHandler.class, new SaveEvent())
  → CafeEventHandlerHub resolves logicallyActive and dispatches
```

The active instance is resolved inside `CafeEventHandlerHub.dispatchMenuEvent` — `MenuRegistry`
never holds component instances.

---

### `MenuItemModel`

```java
class MenuItemModel {
    String  label;
    String  action;
    Class<?> eventType;                              // no-arg event class instantiated at dispatch time
    boolean enabled = true;                          // false → greyed out
    boolean visible = true;                          // false → hidden
    List<Class<? extends Component>> listeners;      // empty = global (any active); non-empty = targeted
}
```

---

## Context menus

Context menus are right-click pop-ups local to a single component's widget.
They are simpler than the main bar — the target is always unambiguous (the right-clicked component itself).

| Aspect | Main bar | Context menu |
|---|---|---|
| Scope | Global — merged from all components | Local — one model per component type |
| Registry | `MenuRegistry` (`@CafeService`) | `ContextMenuRegistry` (`@CafeService`) |
| Target resolution | `logicallyActive()` at click time | Always the right-clicked component |
| `listeners` on items | Required — determines routing | Not used — target is implicit |
| Merging | Items merged across component types | No merging — each type owns its model |
| State | `updateState(activeComponent)` | Evaluated against the component itself at click time |

### `ContextMenuRegistry`

`ContextMenuRegistry` is a `@CafeService` that owns the full context menu lifecycle — model and SWT wiring.
It receives all `Component` beans via DI and scans them at construction time, same pattern as `MenuRegistry`.

```java
@CafeService
class DefaultContextMenuRegistry implements ContextMenuRegistry {
    DefaultContextMenuRegistry(List<Component> components) {
        components.forEach(c -> c.contextMenu().ifPresent(m -> register(c.getClass(), m)));
    }
}
```

```java
interface ContextMenuRegistry {
    // Programmatic registration (alternative to contextMenu())
    void register(Class<? extends Component> componentType, MenuModel menu);

    // Returns the context menu model for a specific component type
    Optional<MenuModel> menuFor(Class<? extends Component> componentType);

    // Attaches a MenuDetectListener to the widget; no-op if no model registered for this type
    void bind(Component component, Control widget);
}
```

Unlike `MenuRegistry` there is no merging — each component type owns exactly one context menu model.
`listeners` on `MenuItemModel` items are unused; the target is always the component itself.

### Binding and dispatch

`ContextMenuRegistry.bind(component, widget)` is called by the framework after each `create()`:

```
ContainerComponent.addComponent(component):
  widget = component.create(parent)
  contextMenuRegistry.bind(component, widget)
```

On right-click:

```
MenuDetectListener fires
  → model = menuFor(component.getClass())   — looked up at bind time, not click time
  → pop-up SWT Menu built from model
  → for each item: enabled ↔ component.availableActions().contains(item.action)
  → user selects item
  → event = item.eventType().getDeclaredConstructor().newInstance()
  → eventHub.sendTo(CafeEventHandler.class, component, event)
```

Dispatch goes directly to the right-clicked component — no hub routing needed beyond `sendTo`.

### Binding lifecycle

`ContextMenuBinder.bind()` is called by the framework when a component's widget is created.
The natural call-site is wherever `component.create(parent)` is invoked — inside
`ContainerComponent.addComponent()`:

```
ContainerComponent.addComponent(component):
  widget = component.create(parent)
  contextMenuBinder.bind(component, widget)   ← framework calls this
```

`ContextMenuBinder` is injected into `ContainerComponent` (or `ApplicationComponent`) via DI.
The component does not call `bind` itself — it only declares `contextMenu()`.

### Properties config

Format: `cafe.contextmenu.{ComponentClass}.{MenuItemName} = action`

```properties
cafe.contextmenu.EditorContainer.Cut    = cut
cafe.contextmenu.EditorContainer.Copy   = copy
cafe.contextmenu.EditorContainer.Paste  = paste
```

Priority: properties (highest) → `contextMenu()` (fallback).

---

### Menu configuration via properties

Format: `cafe.menu.{ComponentClass}.{MenuName}.{MenuItemName} = action`

```properties
cafe.menu.ApplicationComponent.File.Exit    = exit
cafe.menu.ApplicationComponent.Help.About   = about

cafe.menu.EditorContainer.File.Save         = save
cafe.menu.EditorContainer.File.SaveAs       = save-as
cafe.menu.EditorContainer.Edit.Undo         = undo
cafe.menu.EditorContainer.Edit.Redo         = redo
```

Priority per component:
```
1. Properties    (highest)
2. menu()        (fallback)
```

---

## Singleton vs Prototype

See [UIFrameworkDesign.md — Component scope](../UIFrameworkDesign.md) for the full definition.
The menu-relevant difference is how each scope interacts with the two registries.

| Aspect | Singleton | Prototype |
|---|---|---|
| In DI-injected `List<Component>` | Yes — scanned automatically at startup | No — absent at init time |
| Main bar menu scanning | Automatic via `menu()` on injected instance | Explicit `register(Class, MenuModel)` at bootstrap, or via properties |
| Context menu model | Automatic via `contextMenu()` on injected instance | Explicit `register(Class, MenuModel)` at bootstrap, or via properties |
| Context menu `bind()` | Called once after `create()` — same for both scopes | Called per instance after each `create()` |
| `updateState()` / `availableActions()` | Called on the active instance — same for both scopes | Called on the active instance — same for both scopes |

### Prototype — menu model registration

Because prototype instances don't exist at `MenuRegistry` / `ContextMenuRegistry` initialisation,
their menu contributions must be declared by type before the first instance is created.
Two options — both work, use whichever fits:

**Properties (recommended — no code needed):**
```properties
cafe.menu.EditorContainer.File.Save     = save
cafe.menu.EditorContainer.File.SaveAs   = save-as
cafe.contextmenu.EditorContainer.Cut    = cut
cafe.contextmenu.EditorContainer.Copy   = copy
```

**Programmatic (at bootstrap):**
```java
menuRegistry.register(EditorContainer.class, editorMenuModel);
contextMenuRegistry.register(EditorContainer.class, editorContextMenuModel);
```

`menu()` / `contextMenu()` on a prototype class are ignored by the registries — the injected
`List<Component>` never contains prototype instances. Override them in the class anyway as
documentation; the properties / programmatic registration is what actually takes effect.

### Prototype — `bind()` and handler lifecycle

`ContextMenuRegistry.bind(component, widget)` and `CafeEventHandlerHub.register(component)` are
called by the framework on every `addComponent()` — singleton or prototype, no difference here.
On close, `CafeEventHandlerHub.unregister(component)` is called to prevent leaks.

```
ContainerComponent.addComponent(component):
  widget = component.create(parent)
  cafeEventHandlerHub.register(component)       ← handlers registered per instance
  contextMenuRegistry.bind(component, widget)   ← context menu attached per instance

ContainerComponent.removeComponent(component):
  cafeEventHandlerHub.unregister(component)     ← prevents memory leak + stale dispatch
  widget.dispose()
```

---

## What is decided

| Topic | Decision |
|---|---|
| Menu controller | `MenuRegistry` (`@CafeService`) — owns scanning, SWT bar building, selection wiring, state management |
| Menu DI injection | `DefaultMenuRegistry` receives `List<Component>`; scans all beans at construction time |
| Menu bar building | `MenuRegistry.buildMenuBar(shell)` — creates SWT bar + attaches `SelectionListener` on each item |
| Menu registry | Type-only; each `MenuItemModel` carries a `listeners` list of component types |
| Menu state management | `MenuRegistry.updateState(activeComponent)` — enables/disables SWT items based on `availableActions()` |
| Menu enabled rule | `listeners` empty → always enabled; `active.class ∈ listeners` → enabled ↔ `active.availableActions().contains(item.action)`; `active.class ∉ listeners` → disabled |
| Dynamic availability | `Component.availableActions(): Set<String>` — component reports which of its registered actions are usable right now |
| Menu event dispatch | `CafeEventHandlerHub.dispatchMenuEvent(event, menuItem)` — routing logic lives in the hub; registries never hold component instances |
| Context menu controller | `ContextMenuRegistry` (`@CafeService`) — owns scanning, `bind()`, dispatch; no separate binder |
| Context menu DI injection | `DefaultContextMenuRegistry` receives `List<Component>`; scans `contextMenu()` at construction time |
| Context menu binding | `ContextMenuRegistry.bind(component, widget)` — called by framework after `create()`; no-op if no model registered |
| Context menu target | Always the right-clicked component — `eventHub.sendTo(CafeEventHandler.class, component, event)` |
| Context menu state | Same `availableActions()` rule, evaluated against the component itself at click time |
| Prototype menu registration | Properties or explicit `register()` at bootstrap — prototype types absent from DI-injected list |
| Prototype handler lifecycle | Framework owns `register()` / `unregister()` via `addComponent()` / `removeComponent()` — not the user's responsibility |
