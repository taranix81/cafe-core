# Cafe Desktop — UI Framework

> Single reference document. All design decisions, open questions, and architecture live here.
> Implementation phases and task tracking: [implementation_plan.md](implementation_plan.md).
> POC / deferred designs: [poc/](poc/).

---

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

### `LayoutComponent`

Owns the main area and places `ContainerComponent` instances within it.

```java
interface LayoutComponent {
    void    addContainer(ContainerComponent container);
    boolean removeContainer(ContainerComponent container);
}
```

| Class | Behaviour |
|---|---|
| `FlowLayoutComponent` | Containers fill space in order — default |
| `GridLayoutComponent` | Grid-based, reads `@CafeLayout(column, row)` from containers |

### `LayoutService`

```java
interface LayoutService {
    LayoutComponent createLayout(Composite parent);
    void place(LayoutComponent layout, Collection<ContainerComponent> containers);
}
```

`DefaultLayoutService` uses `FlowLayoutComponent` by default. User provides a custom implementation via `@CafeService @CafePrimary` when needed.

### Configuration — two tiers, properties first

```properties
cafe.layout.type                = GRID
cafe.layout.columns             = 3
cafe.layout.rows                = 2
cafe.layout.FileExplorer.column = 0
cafe.layout.FileExplorer.row    = 0
cafe.layout.Editor.column       = 1
cafe.layout.Editor.row          = 0
```

Programmatic fallback — protected method in `CafeDesktopApplication`:

```java
@Override
protected void configureLayout(LayoutService service) {
    service.setType(GridLayoutComponent.class);
    service.place(FileExplorerContainer.class, 0, 0);
    service.place(EditorContainer.class,       1, 0);
}
```

### `@CafeLayout` — container-level hint

Used by `GridLayoutComponent` when no properties or programmatic config covers a container.

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

### Position conflicts — cascade

When multiple containers share a cell they cascade: all added to that cell's region via `addComponent`. Order: properties-configured first, then programmatic, then `@CafeLayout`.

---

## Component model

### Two tiers

- **Leaf components** — plain Java classes annotated `@CafeComponent`. Hold business logic, event handlers, model references. Implement **no interface**.
- **Container components** — implement `ContainerComponent`. Own SWT layout for a group of children.

```
ContainerComponent                   (interface — lifecycle + child management)
  └── ApplicationComponent           (root — owns Shell, event loop)

@CafeComponent class (any name)      (plain POJO — no interface)
  wrapped internally by ↓
ComponentProxy                       (lifecycle, widget binding, event routing)
```

### Scope — Singleton vs Prototype

| Scope | DI annotation | Instances | Example |
|---|---|---|---|
| **Singleton** | `@CafeSingleton` | One per application | `FileExplorerContainer`, sidebar, toolbar |
| **Prototype** | `@CafeComponent` | New instance per `addComponent()` call | `TextEditorComponent` (one per open file) |

`@CafeComponent` is meta-annotated with `@CafePrototype` from `cafe-beans`.

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@CafePrototype
public @interface CafeComponent {
    Class<? extends Form> form()  default Form.class;   // stateless widget factory
    Class<?>              model() default Void.class;    // model POJO; Void.class = no model
}
```

### Annotations

```
cafe-desktop/annotations/
  @CafeComponent             — prototype scope; carries form(), model() attributes
  @CafeForm                  — singleton widget factory
  @CafeService               — singleton business service
  @CafeModel                 — marks a bindable POJO
  @CafeStateful              — enables dirty tracking on a model
  @CafeModelProperty         — exposes a field/getter for binding
  @CafeEventHandler          — desktop handler; bound to CafeEventHandlerHub
  @CafePropertyChangeHandler — reacts to model property changes (SWT→model direction)
```

### Startup configuration — `ApplicationComponentConfigure`

```java
public interface ApplicationComponentConfigure {
    void configure(ApplicationComponent application);
}
```

```java
@CafeSingleton
class MyApplicationConfigure implements ApplicationComponentConfigure {
    @Override
    public void configure(ApplicationComponent application) {
        application.addComponent(FileExplorerContainer.class);
        application.addComponent(EditorArea.class);
    }
}
```

Exactly one bean must exist. The framework opens nothing automatically — everything visible at startup is opened here. Close = destroyed; no hidden state.

### `ContainerComponent`

The framework's only public component interface.

```java
public interface ContainerComponent {
    Widget create(Control parent);
    void   show();
    void   hide();
    void   dispose();

    void    addComponent(Class<?> componentType);
    void    addComponent(Class<?> componentType, String sourceId);  // opens with DataSource from registry
    boolean removeComponent(UUID componentId);
    Object  selected();                           // active child component POJO, or null
    boolean isOpen(String sourceId);
    void    activate(String sourceId);
}
```

### `ApplicationComponent`

Root container — owns the SWT `Shell` and event loop. Holds all open component instances.

```java
interface ApplicationComponent extends ContainerComponent {
    void start();
    void shutDown();
    ContainerComponent          getActiveContainer();
    void                        setLogicallyActive(ContainerComponent container);
    Set<ContainerComponent>     getContainers();
    <T> List<T>                 getComponents(Class<T> componentType);      // all open instances of type
    <T> Optional<T>             getActiveComponent(Class<T> componentType); // active instance of type
}
```

`logicallyActive` is tracked separately from SWT focus — menu clicks do not change it.

### `ApplicationShellConfiguration`

```java
interface ApplicationShellConfiguration {
    void configure(Shell shell);   // set title, size, icon, etc.
}
```

Called by `ApplicationComponent.start()` before layout is applied. `PropertyShellConfiguration` is the framework default — reads `cafe.shell.title`, `cafe.shell.width`, `cafe.shell.height`. User overrides with their own `@CafeSingleton` implementation.

### Prototype — handler lifecycle

Handler registration is automatic — `CafeComponentClassResolver` step 6 calls `eventHub.register(component)`. Cleanup is also automatic — when the container drops its reference on `removeComponent()`, the GC collects the component and the dispatcher's `WeakReference` goes stale on the next `send()`.

```
ContainerComponent.addComponent(PlaylistEditor.class):
  proxy = CafeComponentClassResolver.init(PlaylistEditor.class, parent, sourceId)
          └── step 6: eventHub.register(component)   ← weak ref added to dispatcher(s)

ContainerComponent.removeComponent(componentId):
  proxy.widgetContainer.dispose()
  proxies.remove(componentId)              ← last strong reference dropped
  → GC collects component
  → dispatcher's WeakReference goes stale
  → purged on next send()                  ← no explicit unregister() call needed
```

---

## `Form` — SWT widget creation

A `Form` is a stateless widget factory. Creates SWT controls and tags each bindable one with `widget.setData("id", "propertyName")`. Holds no state; reused across component instances.

```java
public interface Form {
    void create(Composite parent);
}
```

- A `Form` never owns a model or listens to events.
- `@CafeForm` scope is **Singleton** — one shared instance per application.

```java
@CafeForm
class TextEditorForm implements Form {
    @Override
    public void create(Composite parent) {
        Text text = new Text(parent, SWT.MULTI | SWT.BORDER);
        text.setData("id", "content");

        Label fileNameLabel = new Label(parent, SWT.NONE);
        fileNameLabel.setData("id", "fileName");
    }
}
```

### Widget wrappers

Every SWT widget has a typed `CafeWidgetWrapper<T>` (`setValue / getValue / setEnabled / onChanged`):

| Group | Widgets | Value type |
|---|---|---|
| Text input | `Text`, `StyledText` | `String` |
| Display / read-only | `Label`, `CLabel`, `Link` | `String` |
| Boolean | `Button` (CHECK / RADIO / TOGGLE) | `Boolean` |
| Numeric | `Spinner`, `Scale`, `Slider` | `Integer` |
| Progress | `ProgressBar` | `Integer` (read-only) |
| List selection | `Combo`, `CCombo`, `List` | `String` / `List<String>` |
| Date / Time | `DateTime` (DATE/CALENDAR/TIME) | `LocalDate` / `LocalTime` |
| Complex | `Table`, `Tree` | `List<T>` (requires renderer) |
| Navigation | `TabFolder`, `CTabFolder` | `Integer` (selected index) |
| Web | `Browser` | `String` (URL or HTML) |

`CafeWidgetWrapperFactory` selects by widget type and style bits. `Button/SWT.PUSH` and `Button/SWT.ARROW` are action controls — not bindable.

---

## Data binding

### Binding declaration

Forms tag each bindable widget with `widget.setData("id", "propertyName")`. After `form.create()`, `CafeComponentClassResolver` walks `widgetContainer.getChildren()` and builds `Map<String, CafeWidgetWrapper<?>>`. Widgets without tags are invisible to the binding system.

### Direction 1 — SWT → model

```
user edits widget
  → SWT listener (attached by CafeComponentClassResolver after form.create())
  → inspector.setValue(model, property, newValue)
      ├── model field updated
      ├── dirty flag set on ComponentProxy    (@CafeStateful)
      └── @CafePropertyChangeHandler dispatched on component
```

### Direction 2 — model → SWT (Byte Buddy)

`CafeComponentClassResolver` creates the model as a Byte Buddy subclass proxy. Every `@CafeModelProperty` setter is intercepted:

```
component calls model.setName("x")
  → Byte Buddy SetterInterceptor
      → super.setName("x")             ← field updated
      → refreshWidget("name")          ← SWT widget updated
```

```java
class SetterInterceptor {
    private final Consumer<String> refreshCallback;

    @RuntimeType
    public Object intercept(@Origin Method method, @SuperCall Callable<?> superCall) throws Exception {
        Object result = superCall.call();
        refreshCallback.accept(toPropertyName(method));
        return result;
    }
}
```

### `@CafePropertyChangeHandler`

Dispatched after Direction 1 updates the model field. Handler may freely call model setters — each triggers Direction 2:

```java
@CafeComponent(form = PlaylistForm.class, model = Playlist.class)
class PlaylistEditor {
    @CafeInject Playlist model;

    @CafePropertyChangeHandler("name")
    void onNameChanged(String name) {
        model.setStatus(name.isEmpty() ? "unnamed" : "OK");
    }
}
```

### Flows

**User edits a widget:**
```
user types in Text
  → SWT ModifyListener
  → inspector.setValue(model, "content", "new text")
      ├── model field updated
      ├── dirty flag set
      └── @CafePropertyChangeHandler("content") dispatched
              └── component may call model.setX(...) → Byte Buddy → widget refreshed
```

**Program swaps the model:**
```
ComponentProxy.setModel(newModel)
  → inspector.copyFields(newModel, model)  // copy values into Byte Buddy proxy
  → refreshAll(model)                      // push all bound values to widgets
```

---

## Component composition patterns

A `@CafeComponent` has three injectable concerns — form, model, source — each in separated or combined form:

| Concern | Separated | Combined |
|---|---|---|
| Form | `@CafeComponent(form = MyForm.class)` | Component implements `Form` directly |
| Model | `@CafeInject DataFile model` | (always injected — Byte Buddy proxy) |
| DataSource | `@CafeInject DataSource<DataFile> source` | Component implements `DataSource<DataFile>` |

```java
// Fully separated — framework wires everything
@CafeComponent(form = TextEditorForm.class, model = DataFile.class)
class TextEditorComponent {
    @CafeInject DataFile             model;
    @CafeInject DataSource<DataFile> source;
    @CafeInject Text                 textWidget;
}

// Combined form — component creates its own widgets
@CafeComponent(model = DataFile.class)
class TextEditorComponent implements Form {
    @CafeInject DataFile             model;
    @CafeInject DataSource<DataFile> source;
    @Override public void create(Composite parent) { /* build SWT tree */ }
}

// Combined source — component IS the data source
@CafeComponent(form = TextEditorForm.class, model = DataFile.class)
class TextEditorComponent implements DataSource<DataFile> {
    @CafeInject DataFile model;
    @Override public DataFile load()          { ... }
    @Override public void    save(DataFile d) { ... }
    @Override public String  getId()          { ... }
    @Override public String  getDisplayName() { ... }
    @Override public boolean isReadOnly()     { return false; }
}

// Fully combined
@CafeComponent(model = DataFile.class)
class TextEditorComponent implements Form, DataSource<DataFile> { ... }
```

---

## `CafeComponentClassResolver` — instantiation order

Extends `AbstractClassResolver` — same extension mechanism as `CafeCommandClassResolver` in cafe-shell.

### Canonical order (separated case)

```
Step 1  Form / SWT
          create widgetContainer (Composite)
          form.create(widgetContainer)
          widgets now exist; setData("id", ...) tags in place

Step 2  DataSource
          resolve from DataSourceRegistry via BeanTypeKey.from(DataSource.class, modelType)
          if multiple matches → select by sourceId from addComponent(type, sourceId)
          if component implements DataSource<T> → skip (resolved in step 4)

Step 3  DataModel
          ByteBuddyModelProxyFactory.create(modelType, refreshCallback)
          inspector.copyFields(source.load(), modelProxy)   ← populate before binding wired

Step 4  Component
          factory.createPrototype(componentType)
            → inject DI beans, modelProxy, DataSource, SWT widgets
          widgetContainer.setData("component", component)
          if component implements DataSource<T> → register in DataSourceRegistry + load()

Step 5  Data binding (Model ↔ SWT)
          scan widgetContainer.getChildren() → build bindings map
          Direction 1: attach SWT listeners
          Direction 2: already wired via Byte Buddy
          refreshAll()   ← push model values to widgets

Step 6  Register handlers
          eventHub.register(component)   ← weak ref added to all matching dispatchers
```

### Combined-Form variation

Step 1 splits: framework creates the component first (constructor + services only), calls `component.create(widgetContainer)`, then completes steps 2–6. Widget and model injection happens after `create()` — component code inside `create()` cannot assume them.

### Combined-DataSource variation

Step 2 is skipped. After step 4, the component is registered in `DataSourceRegistry` and `source.load()` is called. Model proxy created empty in step 3; populated at end of step 4.

### Widget disambiguation (step 4)

```
for each @CafeInject field of SWT Widget subtype:
  1. collect widgetContainer.getChildren() recursively, assignable to field type
  2. if exactly one match → inject
  3. if multiple         → filter by widget.getData("id").equals(field.getName())
  4. ambiguous / no match → throw CafeComponentWiringException
```

---

## Model

Plain POJOs — no framework dependency.

```java
@CafeModel
@CafeStateful
class DataFile {
    @CafeModelProperty
    private String content;

    @CafeModelProperty(name = "fileName")
    public String getFileName() { ... }

    @CafeModelProperty(readOnly = true)
    public long getLastModified() { ... }
}
```

| Annotation | Purpose |
|---|---|
| `@CafeModel` | Marks a bindable POJO |
| `@CafeStateful` | Dirty tracking via `ComponentProxy.isDirty()` |
| `@CafeModelProperty` | Exposes field or getter as a bindable property |

`CafeModelWrapper<T>` holds the instance and dirty flag. `CafeModelInspector` resolves metadata and performs reflective get/set.

---

## DataSource layer

### `DataSource<T>`

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
| `FileDataSource<T>` | File on disk; needs `DataSerializer<T>` | Stable UUID assigned at creation |
| `NewDocumentSource<T>` | In-memory; `getDisplayName()` = "Untitled" | Stable UUID |
| `UrlDataSource<T>` | HTTP GET; `isReadOnly()` = true | URL string |

**File move support:**

```java
class FileDataSource<T> implements DataSource<T> {
    void moveTo(Path newPath);   // updates internal path; fires DataSourceMovedEvent via CafeEventHandlerHub
}
```

`getId()` is a stable UUID — not the path. Path can change via `moveTo()` without re-keying the registry.

### `DataSerializer<T>`

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

Stores `WeakReference<DataSource<?>>` values — auto-removes GC'd entries. `get()`, `isOpen()`, `all()` filter stale entries. Keyed by `BeanTypeKey.from(DataSource.class, modelType)` for type-safe lookup; `sourceId` disambiguates when multiple sources of the same model type are open.

**Typical call site:**

```java
DataSource<DataFile> source = dataSourceFactory.fromFile(path, DataFileSerializer.INSTANCE);
dataSourceRegistry.register(source);
editorArea.addComponent(TextEditorComponent.class, source.getId());

// Deduplication
if (dataSourceRegistry.isOpen(source.getId())) {
    editorArea.activate(source.getId());
    return;
}
```

---

## Event system

### Two event types

| Type | Direction | Handler location |
|---|---|---|
| **Type 1 — component-to-component** | Any component/service → any registered listener | `@CafeEventHandler` on `@CafeSingleton` services or registered component POJOs |
| **Type 2 — SWT ↔ model** | SWT widget ↔ model field | `@CafePropertyChangeHandler` on component; Byte Buddy setter interceptor |

Type 2 is fully internal — application code never calls the wiring methods.

### Multiple instances — broadcast vs targeted

Because `EventDispatcher` holds all registered subscribers (via weak references), `send()` naturally reaches **all live instances** that have a matching handler — not just the active one.

| Method | Reaches |
|---|---|
| `eventHub.send(annotationType, event)` | All live subscribers with a matching handler method |
| `cafeEventHandlerHub.send(event)` | All live `@CafeEventHandler` subscribers |
| `cafeEventHandlerHub.sendTo(target, event)` | The specific instance only (e.g. active editor) |
| `cafeEventHandlerHub.sendToActive(event)` | The logically active container only |

**Example — 3 open editors:**
```
TextEditorComponent A (file1.txt) ─┐
TextEditorComponent B (file2.txt) ─┼── all registered via eventHub.register()
TextEditorComponent C (file3.txt) ─┘

hub.send(new ThemeChangedEvent())      → A, B, C all update their theme
hub.sendTo(activeEditor, saveEvent)    → only the active editor saves
```

When editor B is closed and GC'd, it is automatically removed from the subscriber list — it never receives another event. No cleanup code required.

### Widget event boundary rule

SWT widget events inside a component are **consumed by that component**. No automatic propagation outside.

| Decision | Mechanism |
|---|---|
| Handle internally | `@CafePropertyChangeHandler` for model-bound changes; plain method for everything else |
| Forward outside | Component calls `cafeEventHandlerHub.send(new SomeEvent(...))` explicitly |

```java
@CafeComponent(form = FileListForm.class)
class FileListComponent {
    @CafeInject CafeEventHandlerHub hub;

    @CafePropertyChangeHandler("filter")
    void onFilterChanged(String filter) { /* local model update */ }

    @CafeEventHandler
    void onRowDoubleClicked(RowDoubleClickedEvent e) {
        hub.send(new FileSelectedEvent(e.getRow().getPath()));  // explicit re-send
    }
}
```

There is no SWT event bubbling. The component boundary is the routing boundary.

### `CafeEvent` — routing envelope

```java
public class CafeEvent {
    private final Object target;   // intended receiver — resolved instance
    private final Object source;   // sender
    private final Object message;  // content — action string, selection, payload

    public CafeEvent(Object target, Object source, Object message) { ... }
}
```

### `CafeMenuEvent` — menu selection

```java
public class CafeMenuEvent extends CafeEvent {
    // source  = the MenuItemModel that was clicked
    // message = menuItemModel.action (e.g. "file.save")
    // target  = resolved by MenuComponent
    public CafeMenuEvent(Object target, MenuItemModel source, String action) {
        super(target, source, action);
    }
}
```

Handlers:

```java
@CafeEventHandler
void onMenuAction(CafeMenuEvent event) {
    switch ((String) event.getMessage()) {
        case "file.save"  -> save();
        case "file.saveAs" -> saveAs();
    }
}
```

### `EventDispatcher<A>` and `EventHub`

`EventDispatcher<A>` holds subscribers as **weak references** — when the last strong reference to a subscriber is dropped, it becomes eligible for GC and is silently removed on the next `send()` call. No explicit `unregister()` is needed for normal component lifecycle.

```java
public interface EventDispatcher<A extends Annotation> {
    void send(Object... args);                   // broadcast — all live matching handlers
    void sendTo(Object target, Object... args);  // targeted — handlers on target only
    // Note: no unregister() — weak references handle cleanup automatically
}
```

`EventHub` is the global, non-generic facade — one instance in DI. It adds subscriber registration and fans out to all dispatchers:

```java
public class EventHub {
    // Subscriber registration — delegates to all dispatchers; each dispatcher keeps only
    // listeners that have methods annotated with its own annotation type
    public void register(Object listener);

    // Dispatch
    public <A extends Annotation> void send(Class<A> annotationType, Object... args);
    public <A extends Annotation> void sendTo(Class<A> annotationType, Object target, Object... args);
    public <A extends Annotation> EventDispatcher<A> dispatcher(Class<A> annotationType);

    // Bootstrap only — wires dispatchers into the hub
    public <A extends Annotation> void addDispatcher(Class<A> annotationType, EventDispatcher<A> dispatcher);
}
```

`DefaultEventDispatcher<A>` is the standard implementation — held internally by `EventHub`, never a DI bean.

**Subscriber registration flow:**

| Subscriber type | Trigger | Mechanism |
|---|---|---|
| `@CafeSingleton` service | Startup | `SingletonHandlerMethodResolver` calls `eventHub.register(bean)` automatically for every singleton bean |
| Prototype `@CafeComponent` | Step 6 of `CafeComponentClassResolver.init()` | Resolver calls `eventHub.register(component)` after wiring is complete |
| Prototype cleanup | Component GC'd after container drops its reference | Dispatcher's `WeakReference` becomes stale; filtered out on next `send()` — no explicit call needed |

```java
// DefaultEventDispatcher — sketch of weak-reference subscriber list
class DefaultEventDispatcher<A extends Annotation> implements EventDispatcher<A> {
    private final List<WeakReference<Object>> subscribers = new CopyOnWriteArrayList<>();
    private final Class<A> annotationType;

    void addIfRelevant(Object listener) {
        boolean hasHandlers = Arrays.stream(listener.getClass().getMethods())
            .anyMatch(m -> m.isAnnotationPresent(annotationType));
        if (hasHandlers) subscribers.add(new WeakReference<>(listener));
    }

    @Override
    public void send(Object... args) {
        subscribers.removeIf(ref -> ref.get() == null);   // purge stale entries
        subscribers.stream()
            .map(WeakReference::get).filter(Objects::nonNull)
            .forEach(listener -> invoker.dispatchAll(annotationType, listener, args));
    }
}
```

### `CafeEventHandlerHub`

Desktop dispatcher — extends `EventDispatcher<CafeEventHandler>`.

```java
public interface CafeEventHandlerHub extends EventDispatcher<CafeEventHandler> {
    void sendToActive(Object... args);   // routes to logicallyActive container
}
```

```java
class DefaultCafeEventHandlerHub implements CafeEventHandlerHub {
    private final HandlerMethodInvoker   invoker;
    private final ApplicationComponent   application;

    @Override public void sendToActive(Object... args) {
        ContainerComponent active = application.getActiveContainer();
        if (active != null) sendTo(active, args);
    }
    @Override public void send(Object... args)                      { invoker.dispatchAll(CafeEventHandler.class, args); }
    @Override public void sendTo(Object target, Object... args)    { invoker.dispatchTo(CafeEventHandler.class, target, args); }
}
```

### `@CafeHandler` and `@CafeEventHandler`

Two independent annotations — each bound to its own dispatcher:

```java
// cafe-beans — general bean-to-bean handlers
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD) @CafeHandlerType
public @interface CafeHandler { }

// cafe-desktop — UI component handlers
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
public @interface CafeEventHandler { }
```

### `HandlerMethodInvoker` additions

| Method | Behaviour |
|---|---|
| `dispatchAll(annotationType, args)` | Fan-out — invokes all matching handlers across all registered listeners |
| `dispatchTo(annotationType, target, args)` | Targeted — invokes matching handlers on target instance only |

### `CafeEventHandlerRegistry`

Scans `@CafeSingleton` beans at startup and builds a pre-computed roadmap:

```java
@CafeSingleton
class CafeEventHandlerRegistry {
    private final Map<Class<?>, List<HandlerEntry>> roadmap;

    void register(Object singleton) { /* scan @CafeEventHandler methods, add by parameter type */ }
    List<HandlerEntry> handlersFor(Class<?> eventType) { ... }
}
```

### Logical active vs SWT focus

`logicallyActive` is set by containers via `applicationComponent.setLogicallyActive(this)` on tab selection. Menu clicks do not affect it — `Menu` is not a `Control` in SWT.

```
User activates editor tab   → logicallyActive = EditorContainer   ✓
User clicks menu item       → logicallyActive unchanged            ✓
User activates sidebar      → logicallyActive = SidebarContainer  ✓
```

---

## Menu system

### `MenuComponent` — SWT selection translator

Framework-internal `SelectionListener` per menu item. Translates SWT selection to `CafeMenuEvent`.

**Target resolution:**

| `targetClass` | Instances found | Target |
|---|---|---|
| `null` (default) | — | `ApplicationComponent` — root container |
| set | exactly one | that instance (singleton or unique component) |
| set | more than one | active instance of that type (`applicationComponent.getActiveComponent(targetClass)`) |

```java
// MenuComponent.widgetSelected():
Object target;
if (menuItemModel.targetClass == null) {
    target = applicationComponent;
} else {
    List<?> instances = applicationComponent.getComponents(menuItemModel.targetClass);
    target = instances.size() == 1
        ? instances.get(0)
        : applicationComponent.getActiveComponent(menuItemModel.targetClass).orElse(applicationComponent);
}
cafeEventHandlerHub.sendTo(target, new CafeMenuEvent(target, menuItemModel, menuItemModel.action));
```

### `MenuItemModel`

```java
class MenuItemModel {
    String   label;
    String   action;           // "file.save" — becomes CafeMenuEvent.message
    Class<?> targetClass;      // null = ApplicationComponent; non-null = specific type
    boolean  enabled;
    boolean  visible;
}
```

### `MenuModel` and `MenuRegistry`

`MenuRegistry` builds the menu bar from `PropertiesMenuModel` at startup.

**Menu model resolution order:**

```
MenuRegistry.buildMenuBar(shell):
  1. Check DI for MenuModelProvider bean
       → if present: menuModel = provider.provide()          // fully programmatic
       → if absent:  menuModel = propertiesMenuModel.toMenuModel()
  2. Check DI for MenuModelConfiguration bean
       → if present: configuration.configure(menuModel)      // post-process / override
  3. Build SWT menu bar from final menuModel
```

| Interface | Role |
|---|---|
| `MenuModelProvider` | `MenuModel provide()` — supplies entire menu model programmatically |
| `MenuModelConfiguration` | `void configure(MenuModel)` — post-processes after properties load |

---

## Bootstrap sequence

```
CafeDesktopApplication.beforeContextInit()
  addBeanToContext(getBeansFactory())
  addBeanToContext(getHandlerMethodInvoker())

  EventHub eventHub = new EventHub(getHandlerMethodInvoker())
  addBeanToContext(eventHub)

DI resolves:
  DefaultCafeEventHandlerHub   ← HandlerMethodInvoker + ApplicationComponent
  CafeEventHandlerRegistry     ← scans singletons, builds @CafeEventHandler roadmap
  DefaultApplicationComponent

Listener auto-registration (SingletonHandlerMethodResolver):
  for each @CafeSingleton bean → eventHub.register(bean)
  EventHub fans out to all dispatchers; each dispatcher keeps the bean only if it has
  methods annotated with that dispatcher's annotation type

ApplicationComponent.start():
  Create Shell
  ApplicationShellConfiguration.configure(shell)
  Set layout
  ApplicationComponentConfigure.configure(this)   ← user opens startup containers
  MenuRegistry.buildMenuBar(shell)
  Activate default view in each container

SWT event loop:
  MenuComponent.widgetSelected() → CafeMenuEvent → cafeEventHandlerHub.sendTo(target, event)
  Embedded control               → cafeEventHandlerHub.sendToActive(event)
  Component fires event          → cafeEventHandlerHub.send(event)

ApplicationComponent.shutDown():
  Dispose all containers (recursive)
```

---

## End-to-end walkthrough — Text editor

### Model

```java
@CafeModel @CafeStateful
class DataFile {
    private Path path;

    @CafeModelProperty
    private String content;

    @CafeModelProperty(name = "fileName")
    public String getFileName() { return path != null ? path.getFileName().toString() : ""; }
}
```

### Form

```java
@CafeForm
class TextEditorForm implements Form {
    @Override
    public void create(Composite parent) {
        parent.setLayout(new GridLayout(1, false));
        Label nameLabel = new Label(parent, SWT.NONE);
        nameLabel.setData("id", "fileName");
        Text text = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        text.setData("id", "content");
    }
}
```

### Component

```java
@CafeComponent(form = TextEditorForm.class, model = DataFile.class)
class TextEditorComponent {
    @CafeInject DataFile             model;
    @CafeInject DataSource<DataFile> source;
    @CafeInject CafeEventHandlerHub  hub;

    @CafePropertyChangeHandler("content")
    public void onContentChanged(String v) { /* model.content already updated */ }

    @CafeEventHandler
    public void onMenuAction(CafeMenuEvent event) {
        switch ((String) event.getMessage()) {
            case "file.save"  -> source.save(model);
            case "file.saveAs" -> { /* prompt for path, then source.moveTo(newPath); source.save(model); */ }
        }
    }
}
```

### Opening a file

```java
@CafeEventHandler
void onFileDoubleClicked(FileDoubleClickedEvent e) {
    DataSource<DataFile> source = dataSourceFactory.fromFile(e.path(), DataFileSerializer.INSTANCE);
    dataSourceRegistry.register(source);
    editorArea.addComponent(TextEditorComponent.class, source.getId());
}
```

---

## What is retired

| Class | Reason |
|---|---|
| `HandlersService` | Replaced by `CafeEventHandlerHub` |
| `HandlerSignature` (cafe-desktop) | Duplicates `CafeHandlerSignature` from `cafe-beans` |
| `@CafeMenuItemSelectionHandler` | Replaced by `MenuItemModel.action` + `@CafeEventHandler` |
| `CafeMenuItemSelectionMethodResolver` | Only needed by `HandlersService` |
| `ActionBus` | Dead code |
| `EventBus` (components_old) | Disabled — replaced by EventHub |
| `@CafeActionHandler` | Defined but never used |
| `ComponentProxy` (old) | Replaced by `CafeComponentClassResolver` 6-step init |
| `ComponentRegistry` | `ApplicationComponent` owns component lookup directly |
| `publish()` on `ApplicationComponent` | Replaced by direct `CafeEventHandlerHub` injection |

---

## Known gaps (not yet implemented)

### cafe-beans

| Gap | Detail |
|---|---|
| `EventDispatcher<A>` + `DefaultEventDispatcher<A>` | Not yet created |
| `EventHub` | Not yet created |
| `HandlerMethodInvoker.dispatchAll()` | Not yet implemented |
| `HandlerMethodInvoker.dispatchTo()` | Not yet implemented |
| Rename `CafeHandlerExecutorService` → `HandlerMethodInvoker` | Not yet done |
| `CafeApplication.getHandlerMethodInvoker()` | Not yet exposed |
| `CafeHandlerFindService.find()` | Always returns `Set.of()` — broken |
| `factory.createPrototype()` skip `@CafeModel` fields | Not yet implemented |

### cafe-desktop

| Gap | Detail |
|---|---|
| `@CafeEventHandler` annotation | Not yet created |
| `CafeEventHandlerHub` + `DefaultCafeEventHandlerHub` | Not yet created |
| `CafeEventHandlerRegistry` | Not yet created |
| `CafeEvent` + `CafeMenuEvent` | Not yet created |
| `MenuComponent` | Not yet created |
| `EventHub.register(Object listener)` fan-out + weak-ref subscriber list in dispatchers | Not yet implemented |
| `DataSource<T>` + built-in implementations | Not yet created |
| `DataSourceRegistry` | Not yet created |
| `DataSourceFactory` | Not yet created |
| `CafeComponentClassResolver` | Not yet created |
| `ByteBuddyModelProxyFactory` | Not yet created |
| Widget wrappers (22 classes) | Not yet created |
| `@CafeComponent`, `@CafeForm`, `@CafeModel`, etc. | Not yet created |
| Layout (`FlowLayoutComponent`, `GridLayoutComponent`, `LayoutService`) | Not yet created |
| `ComponentProxy` | Not yet created |

---

## Open questions

### Event system

**§1.3 `CafeHandlerFindService.find()` always returns `Set.of()`** — implementation bug, see Known Gaps.

**§1.4 Fan-out not implemented** — implementation task (`dispatchAll()`), see Known Gaps.

**§1.9 SWT thread model — RESOLVED**
Decision: **caller responsibility**. `EventHub` does not auto-marshal to the display thread. Handlers that touch SWT widgets must wrap in `CafeUI.run()` / `CafeUI.runAsync()` when called from a background thread. `CafeUI` is the documented helper for this.

```java
@CafeEventHandler
void onDataLoaded(DataLoadedEvent<DataFile> e) {
    CafeUI.run(() -> model.setContent(e.data().getContent()));  // safe from any thread
}
```

**§1.10 Handler exception policy — RESOLVED**
Decision: **log-and-continue**. Each handler in a `dispatchAll` fan-out is invoked independently inside a try/catch. Exceptions are logged; dispatch continues to the remaining handlers. A single failing handler must not silently kill all subsequent ones.

### Menu

**§2.1 Who triggers menu registration — RESOLVED**
Decision: **no per-component registration needed**. Menu structure is purely properties-driven. `MenuRegistry.buildMenuBar()` reads `PropertiesMenuModel` directly — no `register()` call per `@CafeComponent` type.

**§2.2 Sub-menus — deferred**
Properties format supports two levels (`{Menu}.{Item}`). Three+ levels not yet specified.

**§2.3 Separators — RESOLVED**
Decision: **`MenuItemModel.separator` boolean flag**. When `true`, a visual separator is rendered and all other fields (`label`, `action`, `targetClass`) are ignored.

```java
class MenuItemModel {
    String   label;
    String   action;
    Class<?> targetClass;
    boolean  enabled;
    boolean  visible;
    boolean  separator;   // when true, renders a separator; all other fields ignored
}
```

Properties convention: a line whose value is `---` produces a separator item:
```properties
File.--- = ---
```

**§2.4 Context menu — deferred**

**§2.5 `PropertiesMenuModel` contract — RESOLVED**
Decision: properties keys follow `{MenuName}.{ItemLabel} = {action}`. Optional per-item overrides use dotted suffix:
```properties
File.New       = file.new
File.Open      = file.open
File.---       = ---
File.Save      = file.save
File.Save.target = org.example.FileMenuService
Edit.Copy      = edit.copy
```
`PropertiesMenuModel.toMenuModel()` groups by first key segment (`File`, `Edit`) into `MenuModel` instances, creates one `MenuItemModel` per second segment. `targetClass` is null unless a `.target` suffix is provided (resolved by class name via reflection). `separator = true` when value is `---`.

**§2.6 Drop-down functionality — deferred**

### Data

**§3.1 File move — who handles `DataSourceMovedEvent` — RESOLVED**
Decision: **`ApplicationComponent`** handles it. `FileDataSource.moveTo()` fires `DataSourceMovedEvent(sourceId, newDisplayName)` via `EventHub.send()`. `ApplicationComponent` has a `@CafeEventHandler` that iterates all open containers and updates the tab title for any component backed by that `sourceId`. This covers multiple containers sharing one source.

```java
// DefaultApplicationComponent
@CafeEventHandler
void onDataSourceMoved(DataSourceMovedEvent e) {
    getContainers().forEach(c -> c.updateTitle(e.sourceId(), e.newDisplayName()));
}
```

**§3.2 Dirty state tab title — RESOLVED**
Decision: **`ComponentProxy` fires `ComponentDirtyChangedEvent`** via `EventHub.send()`. The containing `ContainerComponent` handles it and updates the tab item title (e.g. `*report.txt` when dirty).

```java
public record ComponentDirtyChangedEvent(UUID componentId, boolean dirty, String displayName) {}
```

`ComponentProxy` fires this event after each Direction 1 change (dirty → true) and after `setModel()` (dirty → false). The container matches by `componentId` and sets `CTabItem.setText(dirty ? "*" + displayName : displayName)`.

### Layout

**§4.1 Layout implementation approach — RESOLVED**
Decision: implement `FlowLayoutComponent` first (default, covers most cases). `GridLayoutComponent` second. `LayoutService` and `@CafeLayout` as designed. Programmatic config via `CafeDesktopApplication.configureLayout()` protected method override. Start with `FlowLayoutComponent` only if `GridLayoutComponent` is not needed for the first working milestone.

**§4.2 Layout cascade order — RESOLVED**
Decision: properties → programmatic → `@CafeLayout` → insertion order. Later entries in the same tier do not override earlier ones.

### Bootstrap

**§5.1 `addComponent()` scope — RESOLVED**
Decision: **`ApplicationComponent`**. `ContainerComponent.addComponent()` is already on the interface; `ApplicationComponent extends ContainerComponent`. `CafeDesktopApplication` is only the bootstrap entry point — it never exposes `addComponent()` directly. Callers use it exclusively inside `ApplicationComponentConfigure.configure(ApplicationComponent application)`.

**§5.2 `ComponentFactory` wrong import** — trivial fix in Phase 11.

### Migration

**§6.1 `components_old/` — RESOLVED**
Decision: **delete entirely**. Rewrite only what is needed in the new model (identified by Phase 11 task list). No incremental port — the old model is incompatible with the new design.

**§6.2 `ActionBus` — RESOLVED**
Decision: **delete**. `ActionBus` is dead code replaced by `EventHub` + `CafeEventHandlerHub`. Deleted in Phase 11 cleanup.
