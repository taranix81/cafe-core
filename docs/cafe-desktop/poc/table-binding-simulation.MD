# Table Binding — Theoretical Simulation

Two directions traced end-to-end:
- **A** — user double-clicks a cell and types a new value
- **B** — application code updates a model object programmatically

---

## Setup

### Models

```java
@CafeModel
@CafeStateful
class Playlist {
    @CafeModelProperty
    private String name;

    @CafeModelProperty
    private List<Track> tracks;
}

@CafeModel
class Track {
    @CafeModelProperty String title;
    @CafeModelProperty String artist;
    @CafeModelProperty int    duration;
}
```

### Column descriptors

The `TablePropertyBinder` must know which column maps to which model property. Columns are declared on the binder, not in the renderer:

```java
// inside PlaylistForm.create(parent, binder):

Table table = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
table.setHeaderVisible(true);

binder.property("tracks")             // CafePropertyBinder<List<Track>>
      .bind(table)
      .column(0, "title",    "Title",    200)
      .column(1, "artist",   "Artist",   150)
      .column(2, "duration", "Duration",  80);
```

`CafeColumnDescriptor`:

```java
record CafeColumnDescriptor(int index, String property, String header, int width) {}
```

### Initial data

```
Playlist
  name   = "Road Trip"
  tracks = [
    Track { title="Come As You Are",  artist="Nirvana",       duration=219 }  ← index 0
    Track { title="Roxanne",          artist="The Police",    duration=183 }  ← index 1
    Track { title="Creep",            artist="Radiohead",     duration=239 }  ← index 2
  ]
```

---

## Internal state after binding

After `form.create(parent, binder)` completes, `TablePropertyBinder` holds:

```
TablePropertyBinder<Track>
  ├── table           : SWT Table (3 columns, 3 rows)
  ├── columns         : [ (0,"title"), (1,"artist"), (2,"duration") ]
  ├── items           : List<Track>   = [track0, track1, track2]
  ├── itemToRow       : Map<Track, TableItem>
  │     track0 → TableItem[0]
  │     track1 → TableItem[1]
  │     track2 → TableItem[2]
  ├── rowToItem       : Map<TableItem, Track>
  │     TableItem[0] → track0
  │     TableItem[1] → track1
  │     TableItem[2] → track2
  └── cursor          : CafeCursor<Track>   (current = null, no selection)
```

SWT Table state:

```
row 0: ["Come As You Are",  "Nirvana",    "219"]
row 1: ["Roxanne",          "The Police", "183"]
row 2: ["Creep",            "Radiohead",  "239"]
```

---

## Simulation A — User edits a cell

**Action:** user double-clicks the "Artist" cell of row 1 ("The Police") and changes it to "The Police UK".

### A.1 — Mouse event captured

```
SWT fires: MouseEvent(x=312, y=28, button=1, count=2)
  on: Table widget
```

`TablePropertyBinder` has a registered `MouseListener.mouseDoubleClick`:

```
event.x=312, event.y=28
table.getItem(Point(312, 28))  →  TableItem[1]   (row 1 = "Roxanne")
```

Column hit-test (iterate column bounds):

```
column 0 bounds at row 1: x=0..200
column 1 bounds at row 1: x=200..350   ← 312 falls here
column 2 bounds at row 1: x=350..430

result: columnIndex = 1  →  descriptor = (1, "artist", "Artist", 150)
```

### A.2 — Cursor moves to row 1

```
TablePropertyBinder:
  rowToItem.get(TableItem[1])  →  track1

cursor.select(track1)
  cursor.current  =  track1
  cursor.binder.setModel(track1)
    → each CafePropertyBinder on cursor refreshes its widget (if any detail widgets are bound)
  cursor.onMove listeners fired:
    → PlaylistEditorComponent.onTrackSelected(track1)
```

### A.3 — Inline editor created

`TablePropertyBinder` creates a `TableEditor` overlaid on cell (row=1, col=1):

```
TableEditor editor = new TableEditor(table)
Text cellText      = new Text(table, SWT.NONE)
cellText.setText(TableItem[1].getText(1))   →  "The Police"
cellText.selectAll()
editor.setEditor(cellText, TableItem[1], 1)
cellText.setFocus()
```

Screen state: "The Police" is selected inside an inline text field on the cell.

### A.4 — User types

```
User clears text, types: "The Police UK"
SWT fires: ModifyEvent on cellText  (multiple times, one per keystroke)
  → NOT yet committed to model  — still in the editor
```

### A.5 — User presses Enter (commit)

`FocusListener.focusLost` or `KeyListener` (Enter) triggers commit:

```
newValue = cellText.getText()   →  "The Police UK"
cellText.dispose()
editor.dispose()
```

`TablePropertyBinder.onCellCommit(TableItem[1], columnIndex=1, "The Police UK")`:

```
1. descriptor = columns.get(1)       →  property = "artist"

2. track = rowToItem.get(TableItem[1])  →  track1

3. cursor.property("artist").setValue("The Police UK")
   │
   ├── 3a. CafeModelInspector.setValue(track1, "artist", "The Police UK")
   │         track1.artist = "The Police UK"        ← model updated
   │
   ├── 3b. wrapper.setDirty(true)                   ← @CafeStateful
   │
   ├── 3c. TableItem[1].setText(1, "The Police UK") ← cell refreshed in SWT
   │         (editor is already gone — this ensures the real cell shows the value)
   │
   └── 3d. onChange listeners on cursor.property("artist") fired:
             → PlaylistEditorComponent.onArtistChanged("The Police UK")  [if @OnCursorChange]
```

### A.6 — Final state

```
track1.artist = "The Police UK"
playlist.dirty = true

SWT Table row 1: ["Roxanne", "The Police UK", "183"]

cursor.current = track1
```

---

## Simulation B — Programmatic model update

**Action:** application code updates `track2.duration` to 245.

There are two sub-cases: update via binder (correct), or update directly on the POJO (bypasses framework).

---

### B1 — Via binder (correct path)

```java
// application code
cursor.select(track2);
cursor.property("duration").setValue(245);
```

#### B1.1 — cursor.select(track2)

```
cursor.current  =  track2
cursor.binder.setModel(track2)
  → detail widgets (if any) refresh to show track2's values
  → cursor.onMove listeners fired
```

#### B1.2 — cursor.property("duration").setValue(245)

```
1. CafePropertyBinder<Integer> for "duration" on cursor

2. CafeModelInspector.setValue(track2, "duration", 245)
      track2.duration = 245                 ← model updated

3. wrapper.setDirty(true)                   ← @CafeStateful on Playlist

4. Refresh the SWT cell
   TablePropertyBinder receives notification from cursor:
     row   = itemToRow.get(track2)   →  TableItem[2]
     col   = columnFor("duration")   →  2
     value = String.valueOf(245)     →  "245"
   TableItem[2].setText(2, "245")    ← only this cell redrawn

5. onChange listeners on cursor.property("duration") fired
```

#### B1.3 — Final state

```
track2.duration = 245
playlist.dirty  = true

SWT Table row 2: ["Creep", "Radiohead", "245"]   ← only col 2 changed
```

---

### B2 — Direct POJO mutation (bypasses framework)

```java
// application code — wrong way
track2.duration = 245;
```

```
track2.duration = 245    ← model updated

SWT Table row 2: ["Creep", "Radiohead", "239"]   ← STALE — framework unaware

playlist.dirty = false   ← NOT marked dirty
onChange listeners:      ← NOT fired
```

**The table is now out of sync with the model.** To recover, the application must explicitly resync:

```java
// force full resync of the list
binder.property("tracks").refresh();          // option A: re-renders all rows from model
// OR
binder.setModel(playlist);                    // option B: replaces entire model, all widgets refresh
```

The framework has no way to detect direct POJO mutation without observable properties or proxies. **All model changes must go through a `CafePropertyBinder.setValue()` call.**

---

### B3 — Full list replacement

```java
// application code replaces the entire list
List<Track> newTracks = fileService.loadTracks(newPlaylist);
binder.property("tracks").setValue(newTracks);
```

```
CafePropertyBinder<List<Track>>.setValue(newTracks)
│
├── 1. model.tracks = newTracks
│         CafeModelInspector.setValue(playlist, "tracks", newTracks)
│
├── 2. TablePropertyBinder.setValue(newTracks)
│    │
│    ├── table.removeAll()                 ← wipe existing rows
│    │
│    ├── itemToRow.clear()
│    │   rowToItem.clear()
│    │
│    └── for each track in newTracks:
│              TableItem row = new TableItem(table, SWT.NONE)
│              row.setText(0, track.title)
│              row.setText(1, track.artist)
│              row.setText(2, String.valueOf(track.duration))
│              itemToRow.put(track, row)
│              rowToItem.put(row, track)
│
├── 3. cursor.select(null)                 ← selection lost, cursor reset
│         cursor.onMove listeners fired with null
│
├── 4. wrapper.setDirty(true)
│
└── 5. onChange listeners on property("tracks") fired
```

---

## Key data structures summary

```
TablePropertyBinder<T>
│
├── table          SWT Table
├── columns        List<CafeColumnDescriptor>       index ↔ propertyName
├── items          List<T>                           current ordered list
├── itemToRow      IdentityHashMap<T, TableItem>     model object → SWT row
├── rowToItem      IdentityHashMap<TableItem, T>     SWT row → model object
└── cursor         CafeCursor<T>
                     ├── current    T (or null)
                     └── binder     CafeDataBinder   (bound to cursor.current)
```

---

## Decision points this simulation reveals

| Issue | Decision needed |
|---|---|
| **Direct POJO mutation is invisible** | Framework must document: all changes must go through `CafePropertyBinder.setValue()`. Direct field writes bypass dirty tracking and widget sync. |
| **Cell commit trigger** | Enter key vs focus-lost vs explicit "commit" button — needs a policy. Focus-lost is most natural but fires unexpectedly on window switch. |
| **Partial row refresh vs full re-render** | Programmatic single-cell update (`setValue` via cursor) should update only that `TableItem` cell, not re-render the whole table. Full re-render only on `setValue(List<T>)`. |
| **Cursor on list replacement** | When `setValue(newList)` is called, the cursor resets to `null`. If the same logical item exists in the new list, should the cursor try to re-select it by identity or by index? |
| **Multi-selection** | `CafeCursor<T>` currently tracks a single item. Multi-row selection (e.g. bulk delete) needs a separate API — `cursor.selection()` returning `List<T>`. |
| **Validation on cell commit** | Where does validation live? In the `onChange` listener, in the `CafePropertyBinder`, or in the model itself? Rejected values must revert the cell text. |
