# Cafe Desktop — Implementation Plan

---

## Authoritative documents

All decisions are taken from:

| Topic | Authoritative document |
|---|---|
| All design — components, events, layout, menus, DataSource, model | [UIFrameworkDesign.md](UIFrameworkDesign.md) |
| POC / deferred designs | [poc/](poc/) |
| Blueprints | [poc/Example1.java](poc/Example1.java), [architecture/architecture.puml](architecture/architecture.puml) |

### Blueprint → Plan naming map

The blueprints use some terms that differ from the finalised design. When reading `Example1.java` or `architecture.puml`, apply these translations:

| Blueprint term | Finalised plan term | Notes |
|---|---|---|
| `formType` / `viewType` attribute on `@CafeComponent` | `form()` attribute | Same concept; plan name wins |
| `View` interface (`createWidget(Composite)`) | `Form` interface (`create(Composite parent)`) | Plan wins; `Form` is stateless widget factory |
| `View.createWidget(Component)` (puml variant) | `Form.create(Composite parent)` | Puml variant conflicts; plan wins — `Composite` is the correct SWT parent type |
| `ApplicationContainerConfiguration` (puml) | `ApplicationComponentConfigure` | Plan name wins; more precise |
| `MenuModelProvider` (Example1) | Extension point — see Phase 8 | Documented in UIFrameworkDesign.md |
| `ApplicationShellConfiguration` (puml) | Same name — see Phase 2 | Added to plan below |
| `PropertyShellConfiguration` (puml) | Same name — see Phase 10 | Built-in property-based shell configurator |
| `PropertyMenuConfiguration` (puml) | Clarifies `PropertiesMenuModel` role — see Phase 8 | Maps to existing `PropertiesMenuModel` |

**Resolved decisions (previously in conflict):**

| Topic | Decision |
|---|---|
| Menu event handling | `MenuComponent` creates `CafeMenuEvent`; routed to target via `cafeEventHandlerHub.sendTo()` — `UIFrameworkDesign.md` wins |
| Binding declaration API | `widget.setData("id", "propertyName")` in Forms; `CafeComponentClassResolver` scans tagged children after `form.create()` and builds binding map — `UIFrameworkDesign.md` wins |
| Property change handler | `@CafePropertyChangeHandler("name")` on component method — `UIFrameworkDesign.md` wins |

---

## Phase 1 — cafe-beans prerequisites

_Nothing in cafe-desktop can be wired until these exist._

| Task | Source | Status |
|---|---|---|
| Add `EventDispatcher<A>` interface — no `unregister()`; subscriber list is `List<WeakReference<Object>>`; stale entries purged on each `send()` | [UIFrameworkDesign.md §EventDispatcher](UIFrameworkDesign.md) | missing |
| Add `DefaultEventDispatcher<A>` implementation (internal to `EventHub` — not a DI bean); `addIfRelevant(listener)` keeps listener only if it has methods annotated with `A` | same | missing |
| Add `EventHub` concrete class — `register(Object listener)` fans out to all dispatchers; `send(annotationType, args)` / `sendTo(annotationType, target, args)` / `addDispatcher()` | same | missing |
| Add `HandlerMethodInvoker.dispatchAll()` — each handler invoked independently inside try/catch; exceptions logged, dispatch continues (log-and-continue — §1.10) | same | missing |
| Add `HandlerMethodInvoker.dispatchTo()` | same | missing |
| Expose `CafeApplication.getHandlerMethodInvoker()` | same | missing |
| Fix `CafeHandlerFindService.find()` — always returns `Set.of()` | [UIFrameworkDesign.md §Open questions](UIFrameworkDesign.md) | broken |
| `factory.createPrototype()` must skip `@CafeInject` fields whose declared type is `@CafeModel` — leave them `null`; `CafeComponentClassResolver` sets them after creating the Byte Buddy proxy | [UIFrameworkDesign.md §CafeComponentClassResolver](UIFrameworkDesign.md) | missing |

---

## Phase 2 — Core API interfaces & annotations

_Defines the surface everything else implements against._

### Interfaces to update / create

| Interface | Current state | Change needed |
|---|---|---|
| `ContainerComponent` | `addComponent(Component)`, `removeComponent(Component)`, `selected()` | Rewrite as standalone (no base `Component`); new API: `addComponent(Class<?>)`, `addComponent(Class<?>, String sourceId)`, `removeComponent(UUID)`, `isOpen(String sourceId)`, `activate(String sourceId)`, `create(Control)`, `show()`, `hide()`, `dispose()` — no `closable()`, no menu methods, no `forwardMenuEvent` |
| `ApplicationComponentConfigure` | does not exist | Create: `void configure(ApplicationComponent application)` — implemented as `@CafeSingleton`; called at startup to open initial containers |
| `ApplicationComponent` | `start`, `shutDown`, `getActiveContainer`, `getContainers`, `getComponent` | Add `setLogicallyActive(ContainerComponent)`, `getComponents(Class<T>)`, `getActiveComponent(Class<T>)` — replaces `ComponentRegistry` as the component lookup authority |
| `Form` | does not exist in new package | Create: `void create(Composite parent)` — no binder; Forms tag widgets with `setData("id", ...)` |
| `ApplicationShellConfiguration` | does not exist | Create: `void configure(Shell shell)` — implemented as `@CafeSingleton`; called by `ApplicationComponent.start()` before layout is applied; allows user to set title, size, icon, etc. |

> **`Component`** — no longer a public interface. Leaf `@CafeComponent` classes are plain POJOs. Delete the existing `Component` interface.
> **`PageComponent`** — removed. Leaf components implement no interface.

### Annotations to create (all missing)

```
cafe-desktop/annotations/
  @CafeComponent             — prototype scope; carries form(), model() attributes
  @CafeForm                  — singleton widget factory
  @CafeService               — singleton business service
  @CafeModel                 — marks a bindable POJO
  @CafeStateful              — enables dirty tracking on a model
  @CafeModelProperty         — exposes a field/getter for binding
  @CafeEventHandler          — desktop handler bound to CafeEventHandlerHub
  @CafePropertyChangeHandler — reacts to model property changes (SWT→model direction)
```

> **`@CafeMenuHandler` is removed.** Menu events use `CafeMenuEvent` routed by `MenuComponent`.

### Utilities

| Class | Source |
|---|---|
| `CafeUI` (Display-thread marshalling) | [UIFrameworkDesign.md §SWT thread model](UIFrameworkDesign.md) — `CafeUI.run()` / `runAsync()`; implement `Display.getThread()` guard; **caller responsibility** — `EventHub` does NOT auto-marshal; handlers touching SWT must call `CafeUI.run()` from background threads (§1.9) |

---

## Phase 3 — DataSource layer

_Independent of model/binding — can start immediately after Phase 2._

### `DataSource<T>` interface

```java
interface DataSource<T> {
    T       load();
    void    save(T data);
    String  getDisplayName();
    String  getId();              // stable UUID — never changes on file move
    boolean isReadOnly();
}
```

### Built-in implementations

| Class | Backs | `getId()` |
|---|---|---|
| `FileDataSource<T>` | File on disk; needs `DataSerializer<T>` for marshalling | Stable UUID assigned at creation — not the path; path can change via `moveTo(Path)` |
| `NewDocumentSource<T>` | In-memory; no backing file yet; `getDisplayName()` = "Untitled" | Stable UUID |
| `UrlDataSource<T>` | HTTP GET; `isReadOnly()` = true | URL string |

**`FileDataSource` — file move support:**

```java
class FileDataSource<T> implements DataSource<T> {
    void moveTo(Path newPath);   // updates internal path; fires DataSourceMovedEvent via CafeEventHandlerHub
                                 // no registry re-keying needed — getId() is a stable UUID
}
```

`ApplicationComponent` handles `DataSourceMovedEvent` via `@CafeEventHandler` — iterates all open containers and updates the tab title for any component backed by that `sourceId`. Covers multiple containers sharing one source. See [UIFrameworkDesign.md §Open questions §3.1](UIFrameworkDesign.md).

> **Why stable UUID?** The path is mutable ("Save As", external rename, move). Using it as the registry key would force a remove + re-add cycle on every move.

### `DataSerializer<T>` interface

```java
interface DataSerializer<T> {
    T      deserialize(byte[] bytes);
    byte[] serialize(T data);
}
```

### `DataSourceFactory`

Pure creation — no registration, no lifecycle.

```java
@CafeService
interface DataSourceFactory {
    <T> DataSource<T> fromFile(Path path, DataSerializer<T> serializer);
    <T> DataSource<T> fromUrl(URL url, DataSerializer<T> serializer);
    <T> DataSource<T> newDocument(Class<T> modelType);
}
```

**Typical call site:**

```java
DataSource<DataFile> source = dataSourceFactory.fromFile(path, DataFileSerializer.INSTANCE);
dataSourceRegistry.register(source);
editorArea.addComponent(TextEditorComponent.class, source.getId());
```

### `DataSourceRegistry`

```java
@CafeSingleton
class DataSourceRegistry {
    DataSource<?>              register(DataSource<?> source);
    DataSource<?>              get(String sourceId);
    void                       remove(String sourceId);
    boolean                    isOpen(String sourceId);
    Collection<DataSource<?>>  all();
}
```

**Type-safe lookup via `BeanTypeKey`:**
Keyed by `BeanTypeKey.from(DataSource.class, modelType)`. `DataSource<DataFile>` is distinct from `DataSource<PlaylistRecord>`. When multiple sources of the same model type are open, the `sourceId` from `addComponent(type, sourceId)` selects the instance.

**Memory management:** Holds `WeakReference<DataSource<?>>`. Stale entries auto-removed. `get()` and `all()` filter them before returning.

---

## Phase 4 — Model layer

_Must exist before binding can work._

| Class | Source |
|---|---|
| `CafeModelWrapper<T>` | [UIFrameworkDesign.md §Model](UIFrameworkDesign.md) — fully specced |
| `CafeModelInspector` | same — `getValue`, `setValue`, `getProperties` via reflection on `@CafeModelProperty` |
| `CafePropertyDescriptor` | same — `record(name, type, readOnly)` |

---

## Phase 5 — Widget wrappers

_Self-contained; no dependencies on other new code — can run in parallel with Phase 4._

22 wrapper classes + factory, all fully specced in [UIFrameworkDesign.md §Widget wrappers](UIFrameworkDesign.md):

```
CafeWidgetWrapper<T>              (interface)
CafeWidgetWrapperFactory          (@CafeService — dispatches by type + style bits)

wrappers/
  TextWidgetWrapper               Text        → String       ModifyListener
  StyledTextWidgetWrapper         StyledText  → String       ModifyListener
  LabelWidgetWrapper              Label       → String       read-only
  CLabelWidgetWrapper             CLabel      → String       read-only
  LinkWidgetWrapper               Link        → String       read-only
  CheckButtonWidgetWrapper        Button/CHECK  → Boolean    SelectionListener
  RadioButtonWidgetWrapper        Button/RADIO  → Boolean    SelectionListener
  ToggleButtonWidgetWrapper       Button/TOGGLE → Boolean    SelectionListener
  SpinnerWidgetWrapper            Spinner     → Integer      ModifyListener
  ScaleWidgetWrapper              Scale       → Integer      SelectionListener
  SliderWidgetWrapper             Slider      → Integer      SelectionListener
  ProgressBarWidgetWrapper        ProgressBar → Integer      read-only
  ComboWidgetWrapper              Combo       → String       ModifyListener
  CComboWidgetWrapper             CCombo      → String       ModifyListener
  ListWidgetWrapper               List        → List<String> SelectionListener
  DateWidgetWrapper               DateTime/DATE → LocalDate  SelectionListener
  TimeWidgetWrapper               DateTime/TIME → LocalTime  SelectionListener
  TableWidgetWrapper<T>           Table       → List<T>      SelectionListener  (+ CafeTableRenderer<T>)
  TreeWidgetWrapper<T>            Tree        → List<T>      SelectionListener  (+ CafeTreeRenderer<T>)
  TabFolderWidgetWrapper          TabFolder   → Integer      SelectionListener
  CTabFolderWidgetWrapper         CTabFolder  → Integer      SelectionListener
  BrowserWidgetWrapper            Browser     → String       LocationListener

CafeTableRenderer<T>              (interface — used by TableWidgetWrapper)
CafeTreeRenderer<T>               (interface — used by TreeWidgetWrapper)
```

---

## Phase 6 — Data binding

_Depends on: Phase 4 (model layer) + Phase 5 (wrappers)._

### Classes

| Class | Source |
|---|---|
| `ByteBuddyModelProxyFactory` | [UIFrameworkDesign.md §Data binding](UIFrameworkDesign.md) — creates Byte Buddy subclass proxy; intercepts `@CafeModelProperty` setters; fires `Consumer<String>` callback into `CafeComponentClassResolver` |

> **`CafeDataBinder`** and **`CafeWidgetRegistry`** are removed. Forms declare bindings via `widget.setData("id", "propertyName")`. `CafeComponentClassResolver` scans `widgetContainer.getChildren()` after `form.create()` and builds an internal `Map<String, CafeWidgetWrapper<?>>` directly.

### `ByteBuddyModelProxyFactory` — interception scope

Only methods that are setters for a `@CafeModelProperty` field are intercepted:

```
method name starts with "set"
  AND corresponding field (or getter) is annotated @CafeModelProperty
  AND property name exists as a key in the binding map built by `CafeComponentClassResolver`
```

Push / Arrow `Button` setters, Lombok internal helpers, and unbound properties are passed through unmodified.

### Deferred

| Class | Reason |
|---|---|
| `CafeCursor<T>` / `TablePropertyBinder<T>` | Table master-detail deferred — design in [poc/table-binding-simulation.MD](poc/table-binding-simulation.MD) |

---

## Phase 7 — Event system (cafe-desktop)

_Depends on: Phase 1 (cafe-beans EventHub) + Phase 2 (annotations)._

| Class | Source |
|---|---|
| `CafeEvent` base class | [UIFrameworkDesign.md §CafeEvent](UIFrameworkDesign.md) |
| `CafeMenuEvent extends CafeEvent` | same — `source` = `MenuItemModel`; `message` = action string |
| `DataSourceMovedEvent(sourceId, newDisplayName)` | same — fired by `FileDataSource.moveTo()`; handled by `ApplicationComponent` |
| `ComponentDirtyChangedEvent(componentId, dirty, displayName)` | same — fired by `ComponentProxy` after each Direction 1 field change (dirty→true) and after `setModel()` (dirty→false); handled by containing `ContainerComponent` to update `CTabItem` title |
| `CafeEventHandlerHub` interface | [UIFrameworkDesign.md §CafeEventHandlerHub](UIFrameworkDesign.md) — `send` (all live subscribers), `sendTo` (specific instance), `sendToActive` (logically active container) |
| `DefaultCafeEventHandlerHub` implementation | same |
| `CafeEventHandlerRegistry` | same — pre-computed roadmap for singleton handler lookup; `Map<eventType, List<HandlerEntry>>` |
| Register `DefaultCafeEventHandlerHub` with `EventHub.addDispatcher()` at bootstrap | [UIFrameworkDesign.md §Bootstrap](UIFrameworkDesign.md) |

**Subscriber registration via `SingletonHandlerMethodResolver`:**
At startup, for every `@CafeSingleton` / `@CafeService` / `@CafeForm` bean: call `eventHub.register(bean)`. `EventHub` fans out to all dispatchers; each keeps the bean only if it has matching annotated methods. No manual registration per dispatcher needed.

**Multiple instances:** `send()` reaches all live registered subscribers simultaneously — e.g. `ThemeChangedEvent` updates every open editor. `sendTo()` / `sendToActive()` restrict to one instance.

---

## Phase 8 — Menu system

_Depends on: Phase 7 (event system) + Phase 2 (Component interface complete)._

| Class | Source | Note |
|---|---|---|
| `CafeMenuEvent extends CafeEvent` | [UIFrameworkDesign.md §CafeMenuEvent](UIFrameworkDesign.md) | `source` = `MenuItemModel`; `message` = action string; `target` = resolved instance |
| `MenuComponent` | [UIFrameworkDesign.md §MenuComponent](UIFrameworkDesign.md) | SWT `SelectionListener` per menu item; resolves target; calls `cafeEventHandlerHub.sendTo(target, event)` |
| `MenuModel`, `MenuItemModel` | [UIFrameworkDesign.md §Menu system](UIFrameworkDesign.md) | `MenuItemModel` fields: `label`, `action`, `targetClass`, `enabled`, `visible`, `separator` — when `separator = true`, renders a divider and all other fields are ignored |
| `MenuRegistry`, `DefaultMenuRegistry` | [UIFrameworkDesign.md §Menu system](UIFrameworkDesign.md) | Builds menu bar from `PropertiesMenuModel` at startup; no per-component registration needed |
| `PropertiesMenuModel` | already exists — verify against resolved contract | Keys: `{MenuName}.{ItemLabel} = action`; `.target` suffix adds `targetClass`; value `---` produces separator. See [UIFrameworkDesign.md §Open questions §2.5](UIFrameworkDesign.md) |
| `MenuModelConfiguration` | [architecture/architecture.puml](architecture/architecture.puml) Extensions package | `void configure(MenuModel)` — optional `@CafeSingleton` post-processor |
| `MenuModelProvider` | [poc/Example1.java](poc/Example1.java) | `MenuModel provide()` — optional `@CafeSingleton`; bypasses properties when present |

**Menu model resolution order:**

```
MenuRegistry.buildMenuBar(shell):
  1. Check DI for MenuModelProvider → provider.provide()  OR  propertiesMenuModel.toMenuModel()
  2. Check DI for MenuModelConfiguration → configuration.configure(menuModel)
  3. Build SWT menu bar from final menuModel
```

> **`ContextMenuRegistry` is deferred.** Context menus are not in scope. `ContainerComponent` has no `contextMenu()` method.

---

## Phase 9 — Container updates + resolver

_Depends on: Phases 2–8 all complete._

| Task | Source |
|---|---|
| `DataSourceRegistry` (`@CafeSingleton`) | [UIFrameworkDesign.md §DataSource layer](UIFrameworkDesign.md) |
| `CafeComponentClassResolver` | [UIFrameworkDesign.md §CafeComponentClassResolver](UIFrameworkDesign.md) — 6-step init order: (1) Form/SWT, (2) DataSource, (3) DataModel+load, (4) Component+inject, (5) Binding wire, (6) `eventHub.register(component)` — weak ref added to all matching dispatchers; cleanup is automatic when component is GC'd after `removeComponent()` |
| Fix `ComponentFactory` wrong import (`components_old.Component` → new package) | Phase 11 deletes `components_old/` entirely (§6.1) — this fix is part of that cleanup |
| Update `DefaultApplicationComponent`: add `setLogicallyActive()`, `getComponents()`, `getActiveComponent()`, remove `publish()` | [UIFrameworkDesign.md §ApplicationComponent](UIFrameworkDesign.md) |
| Update `CTabFolderComponent`: add `activate(sourceId)`, `isOpen(sourceId)`, hook `CafeEventHandlerHub` | [UIFrameworkDesign.md §ContainerComponent](UIFrameworkDesign.md) |

### `@CafeComponent` — composition patterns (from `poc/Example1.java`)

Both separated and combined-Form patterns are supported. See [UIFrameworkDesign.md §Component composition patterns](UIFrameworkDesign.md) for all four variants.

| | Pattern A — separated (default) | Pattern B — combined-Form |
|---|---|---|
| Pattern | `@CafeComponent(form = MyForm.class)` + separate `@CafeForm` class | Component class implements `Form` directly |
| Reuse | `@CafeForm` is singleton; shared across prototype instances | Form logic tied to this component only |
| Blueprint ref | `MenuBarComponent2` + `MenuForm` in `Example1.java` | `MenuBarComponent1` in `Example1.java` |
| When to use | Most components — default choice | One-off widgets tightly coupled to their UI |

---

## Phase 10 — Bootstrap & end-to-end wiring

_Depends on: all previous phases._

Wire the startup sequence as defined in [UIFrameworkDesign.md §Bootstrap](UIFrameworkDesign.md):

```
1. CafeDIContext.scan()
   ├── Discover @CafeSingleton, @CafeForm, @CafeService → instantiate, wire @CafeInject
   ├── Register @CafeComponent factories (not yet instantiated)
   └── Register @CafeComponent menu contributions by class (prototype types)

2. ApplicationComponent.start()
   ├── Create Shell
   ├── ApplicationShellConfiguration.configure(shell)
   ├── Set layout
   ├── ApplicationComponentConfigure.configure(this)
   │     → user explicitly calls addComponent() for each startup container
   ├── Build menu bar → MenuRegistry.buildMenuBar(shell)
   └── Activate default view in each container

3. SWT event loop
   ├── MenuComponent.widgetSelected() → CafeMenuEvent → cafeEventHandlerHub.sendTo(target, event)
   └── Component event → cafeEventHandlerHub.send() / sendToActive()

4. ApplicationComponent.shutDown()
   └── Dispose all containers (recursive)
```

Bootstrap additions needed in `CafeDesktopApplication.beforeContextInit()`:
- Add `HandlerMethodInvoker` to context
- Create `EventHub`; create `DefaultEventDispatcher<CafeHandler>` and `DefaultCafeEventHandlerHub`; register both with `EventHub.addDispatcher()`
- `SingletonHandlerMethodResolver` calls `eventHub.register(bean)` for every singleton/service bean — `EventHub` fans out; each dispatcher keeps beans with matching annotated methods
- Register `PropertyShellConfiguration` as the default `ApplicationShellConfiguration` bean

---

## Phase 11 — Migration / cleanup

_Can run in parallel with Phase 9 or after._

| Task | Source |
|---|---|
| **Delete `components_old/`** entirely (~80 legacy classes) — no incremental port; rewrite only what is needed in new model (§6.1) | [UIFrameworkDesign.md §Open questions §6.1](UIFrameworkDesign.md) |
| **Delete `ActionBus`** — replaced by `EventHub` + `CafeEventHandlerHub` (§6.2) | [UIFrameworkDesign.md §Open questions §6.2](UIFrameworkDesign.md) |
| Delete remaining dead code: `HandlersService`, desktop `HandlerSignature`, `@CafeMenuItemSelectionHandler`, `CafeMenuItemSelectionMethodResolver`, old `EventBus`, `@CafeActionHandler` | [UIFrameworkDesign.md §What is retired](UIFrameworkDesign.md) |
| Fix `ComponentFactory` wrong import (`components_old.Component` → new package) | part of `components_old/` deletion above |

---

## Deferred — no blocking decision remaining

| Topic | Blocker | Source |
|---|---|---|
| Table inline editing | Cell commit policy, validation, multi-select | [poc/table-binding-simulation.MD](poc/table-binding-simulation.MD) |
| Sub-menus (3+ levels) | Properties format supports only two levels today | [UIFrameworkDesign.md §Open questions §2.2](UIFrameworkDesign.md) |
| Context menus | Out of scope for current milestone | [UIFrameworkDesign.md §Open questions §2.4](UIFrameworkDesign.md) |
| Drop-down / split-button menus | Out of scope | [UIFrameworkDesign.md §Open questions §2.6](UIFrameworkDesign.md) |

---

## Dependency order

```
Phase 1  cafe-beans (EventHub, dispatchers, HandlerMethodInvoker)
    │
    └── Phase 2  Core API + annotations
              ├── Phase 3  DataSource layer ─────────┐
              ├── Phase 4  Model layer ───────────────┤
              └── Phase 5  Widget wrappers            │
                                                      ▼
                                               Phase 6  Data binding
                                                      │
                                               Phase 7  Event system
                                                      │
                                               Phase 8  Menu system
                                                      │
                                               Phase 9  Container updates + resolver
                                                      │
                                               Phase 10 Bootstrap & wiring
                                                      │
                                               Phase 11 Migration / cleanup
```

Phases 3, 4, and 5 can run in parallel.
Phases 1–5 have no open decisions blocking them and can start immediately.
