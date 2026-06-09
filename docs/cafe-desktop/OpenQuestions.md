# Cafe Desktop — Open Questions

Questions and gaps that must be resolved before or during implementation.
Each item links to the relevant section in `UIFrameworkDesign.md`.

---

## 1. Event System

### 1.1 `@CafeHandler` channel attribute — RESOLVED
**Decision: the dispatcher is responsible for target distribution, not the handler.**

`@CafeHandler` requires no `channel` attribute. It remains a simple marker that declares
"this method handles events of this parameter type." Which instances receive a given event
is entirely the dispatcher's concern — determined by the trigger type and dispatcher logic,
not by anything on the handler method.

Consequences:
- No `Channel` enum needed in either project.
- `DEDICATED` channel concept is dropped — target resolution for menu items is handled by
  `MenuItemEventDispatcher` using `MenuItemModel.listeners` + `logicallyActive`. No annotation needed.
- `@CafeHandler` in `cafe-beans` stays as-is (no new attributes).
- Question 1.7 (DEDICATED ambiguity for non-menu events) is resolved by the same decision.

### 1.2 Menu action → handler method mapping
When a menu item is clicked the registry dispatches an action string (e.g. `"save"`).
How does that string reach a `@CafeHandler` method on the active component?
- Is there a dedicated `MenuActionEvent(String action)` wrapper?
- Or does the framework look up a handler by action string directly?
- Who performs the lookup — `ApplicationComponent`, `CafeHandlerExecutorService`, or a new service?

### 1.3 `CafeHandlerFindService.find()` returns `Set.of()`
The method builds `matchedKeys` but always returns an empty set — broken.
Must be fixed before any event dispatch works.

### 1.4 Fan-out (ALL channel) not implemented
`dispatch()` stops after the first match.
Broadcasting to all matching handlers is required for `channel = ALL`.

### 1.5 `publish()` / EventBus API — RESOLVED
`ApplicationComponent.publish()` is dropped. Components that need to fire events
inject `CafeEventHandlerHub` directly and call `send()`, `sendToActive()`, or `dispatchMenuEvent()`.
This avoids a circular dependency (`ApplicationComponent` ← hub ← `ApplicationComponent`).

### 1.6 Stale "Event-based communication" section in UIFrameworkDesign.md
The original section predates the new two-layer architecture. It shows `eventBus.publish()` as
the caller API (superseded by `applicationComponent.publish()`), marks event categories as
"provisional" (now settled), and has a gaps table that duplicates the new one.
Needs to be either removed or merged into the new sections before it causes confusion.

### 1.7 `DEDICATED` channel semantics — RESOLVED
Resolved by 1.1. No channel attribute on `@CafeHandler` means no DEDICATED concept.
Target resolution is entirely the dispatcher's responsibility — see 1.1.

### 1.8 Menu-triggered event types must be no-arg — rule not documented
`MenuItemEventDispatcher` instantiates the event class from `MenuItemModel.eventType` via reflection.
Events that carry data (e.g. `RowSelectedEvent(TableRow row)`) cannot be instantiated from a menu click.
The constraint — **menu-triggered event types must be no-arg** — is implied but never written down.
Options to clarify:
- State it as a hard rule (menu events are always no-arg action signals)
- Or define a factory/supplier on `MenuItemModel` to allow parameterised events

### 1.9 SWT thread model — not mentioned
`EventBus.publish()` called from an SWT `SelectionListener` is on the display thread — safe.
But if a background thread publishes an event (e.g. after async data load), any handler that
touches a widget will throw `SWTException: Invalid thread access`.
- Should `EventBus` enforce display-thread execution (`Display.syncExec` / `asyncExec`)?
- Or is it the caller's responsibility to marshal onto the display thread before publishing?
- Where is this rule documented for application developers?

### 1.11 EventHub DI disambiguation
`EventHub<CafeHandler>` and `EventHub<CafeEventHandler>` are two distinct instances
registered as beans. Java erases generics at runtime, so both have raw type `EventHub`
from the DI container's perspective. Two options to let the container resolve the right one:

**Option A — Named subinterfaces (no qualifier needed):**
```java
// cafe-beans
public interface CafeHandlerHub extends EventHub<CafeHandler> { }

// cafe-desktop
public interface CafeEventHandlerHub extends EventHub<CafeEventHandler> { }
```
`DefaultEventDistributor` declares `@CafeInject CafeEventHandlerHub eventHub` — resolved
unambiguously by concrete interface type. No string identifiers, full type safety.

**Option B — Identifier/qualifier:**
Register hub beans with a string identifier; inject with a matching qualifier.
Simpler to implement but couples injection to a magic string.

Decision needed before implementing bootstrap or `DefaultEventDistributor`.

### 1.10 Handler exception policy
If a `@CafeHandler` method throws during `dispatchAll` fan-out:
- Does dispatch stop and propagate the exception (one failure kills all remaining handlers)?
- Or does it log and continue to the next handler?
No policy is stated. A silent-kill-on-first-failure breaks fan-out silently.

---

## 2. Menu

### 2.1 Who triggers `MenuRegistry.register()`
The design says components register at startup (`closable() = false`) or when `addComponent()` is called
(`closable() = true`), but nothing specifies who drives this — `CafeDesktopApplication`, the framework,
or the component itself?

### 2.2 Sub-menus
Properties format implies exactly two levels: `{MenuName}.{ItemName}`.
Are sub-menus (three or more levels) ever needed? If yes, how are they expressed?

### 2.3 Separators
No mechanism defined for visual separators between menu item groups.
Options: a dedicated `MenuItemModel` subtype, a naming convention (e.g. empty label `"---"`),
or an explicit `separator: boolean` flag.

### 2.4 `contextMenu()` wiring
`Component.contextMenu()` is declared but the lifecycle is not specified.
- Who calls `contextMenu()` on the component?
- Who creates and attaches the SWT context menu to the widget?
- When — at `create()` time, or lazily on right-click?

### 2.5 `PropertiesMenuModel` → `MenuModel` conversion
`PropertiesMenuModel` exists in code but the contract is not documented.
How does it produce `MenuModel` and `MenuItemModel` instances with the correct `listeners` list?

---

## 3. PageComponent / Editor / Viewer

### 3.1 Type hierarchy
`PageComponent` is a marker interface. The subtypes are not yet designed:
- `Editor<T>` — read + write + dirty state tracking
- `Viewer<T>` — read-only
- What is `T`? A domain model type, a `DataSource`, something else?

### 3.2 Dirty state
Who owns dirty state — the Editor itself, or a wrapper?
How is the tab title updated (e.g. `*filename`) when dirty?
Does dirty state feed back into the menu (e.g. enabling/disabling Save)?

### 3.3 DataSource wiring
How does a `PageComponent` receive its data?
- Injected by the DI container at creation time?
- Passed explicitly when `addComponent()` is called?
- Pulled lazily from a service when the component becomes active?

---

## 4. Layout

### 4.1 Layout mechanism — not yet chosen
The design document describes `FlowLayoutComponent`, `GridLayoutComponent`, `LayoutService`,
`@CafeLayout`, and two configuration tiers (properties / programmatic), but the implementation
approach has not been confirmed. Needs a decision before coding starts.

### 4.2 Default layout behaviour
When no layout configuration is provided, `FlowLayoutComponent` is used.
What is the exact cascade order for containers that share a cell in `GridLayoutComponent`?

---

## 5. Bootstrap & Registration

### 5.1 `CafeDesktopApplication.addComponent()` scope
The design says callers use `addComponent()` to show containers.
Is this method on `CafeDesktopApplication` or on `ApplicationComponent`?
Both are mentioned in different parts of the document — needs to be one place.

### 5.2 `ComponentFactory` wrong import
`ComponentFactory` imports `components_old.Component` instead of `components.Component`.
Blocks use of the factory with the new component model.

---

## 6. Migration

### 6.1 `components_old/` — ~80 legacy classes
No migration plan defined. Options:
- Delete entirely and rewrite what is needed
- Port class by class
- Keep temporarily behind a package boundary and migrate incrementally

### 6.2 `ActionBus` and old event model
`ActionBus` in `cafe-desktop` still references `CafeEventHandler` from `components_old`.
It is unclear whether `ActionBus` will survive the redesign or be replaced by the new event system.
