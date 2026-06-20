# SWT Table — Knowledge Base

## 1. Table Construction

A `Table` is a plain SWT widget. Style flags define behavior at creation time — they cannot be changed after:

```java
Table table = new Table(parent, SWT.BORDER | SWT.VIRTUAL | SWT.V_SCROLL | SWT.H_SCROLL);
table.setHeaderVisible(true);
table.setLinesVisible(true);
```

Key style flags:

| Flag | Effect |
|---|---|
| `SWT.SINGLE` / `SWT.MULTI` | Row selection mode |
| `SWT.FULL_SELECTION` | Whole row highlighted on select (default: first column only) |
| `SWT.VIRTUAL` | Lazy rendering — critical for large datasets |
| `SWT.CHECK` | Adds a checkbox column |
| `SWT.H_SCROLL` / `SWT.V_SCROLL` | Scrollbars |

Project config: `DefaultTableConfig` uses `SWT.BORDER | SWT.VIRTUAL | SWT.V_SCROLL | SWT.H_SCROLL`.

---

## 2. Columns (`TableColumn`)

Columns define layout and header text. Added to the table in order:

```java
TableColumn col = new TableColumn(table, SWT.LEFT);
col.setText("Name");
col.setWidth(120);
col.setMoveable(true);   // user can drag to reorder
col.setResizable(true);  // user can resize
col.pack();              // auto-size to content
```

`TableViewer.buildHeaders()` iterates `getColumnsAmount()` and creates one `TableColumn` per column, storing an id via `column.setData("id", ...)`.

---

## 3. Data Providing — Two Modes

### Eager mode (no `SWT.VIRTUAL`)

All `TableItem`s created upfront. Simple, but slow for large row counts because SWT allocates a native OS row per item:

```java
for (int r = 0; r < rowCount; r++) {
    TableItem item = new TableItem(table, SWT.NONE);
    item.setText(0, "col0 data");
    item.setText(1, "col1 data");
}
```

### Virtual mode (`SWT.VIRTUAL`) — lazy / on-demand

The table renders only visible rows. Declare the total count, then fill each item when the OS requests it via `SWT.SetData`:

```java
table.setItemCount(10_000);  // declares size, no allocation

table.addListener(SWT.SetData, event -> {
    TableItem item = (TableItem) event.item;
    int row = table.indexOf(item);  // which row is being rendered
    item.setText(0, data.get(row).getName());
    item.setText(1, data.get(row).getValue());
});
```

`TableViewer.buildRows()` detects the flag and branches:

```java
boolean isVirtual = (table().getStyle() & SWT.VIRTUAL) == SWT.VIRTUAL;
if (isVirtual) {
    table().setItemCount(getRowsAmount());
} else {
    buildTableItems();
}
```

The `@CafeEventHandler(eventType = SWTEventType.SetData)` in `TableViewer` fills each item on demand.

---

## 4. Sorting

SWT Table has no built-in sort. The standard pattern:

1. Add a `SelectionListener` to each `TableColumn`
2. Track sort column and direction
3. On click: sort backing data, reload items, update `table.setSortColumn()` / `table.setSortDirection()`

```java
col.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
    boolean ascending = table.getSortColumn() != col || table.getSortDirection() == SWT.DOWN;

    // sort backing data
    data.sort(Comparator.comparing(Row::getName,
        ascending ? Comparator.naturalOrder() : Comparator.reverseOrder()));

    // reload
    if ((table.getStyle() & SWT.VIRTUAL) != 0) {
        table.clearAll();   // clears item cache, fires SetData for visible rows again
    } else {
        for (int i = 0; i < table.getItemCount(); i++) {
            table.getItem(i).setText(0, data.get(i).getName());
        }
    }

    table.setSortColumn(col);
    table.setSortDirection(ascending ? SWT.UP : SWT.DOWN);
}));
```

> **VIRTUAL tables:** `table.clearAll()` is the trigger — it discards the item cache and fires `SWT.SetData` again for visible rows, now with the re-ordered data.

`TableModel` has a commented-out `sort()` method — that is where the backing data sort belongs.

---

## 5. TableCursor

`TableCursor` overlays the table for cell-by-cell navigation (rather than row-by-row). Setup:

```java
TableCursor cursor = new TableCursor(table, SWT.NONE);
cursor.setBackground(gray);
// cursor.getRow()    → current TableItem
// cursor.getColumn() → current column index
```

`DefaultTableForm` adds a `KeyListener` on the cursor so pressing a printable character opens an inline editor.

---

## 6. Cell Editing (`ControlEditor`)

`ControlEditor` positions any widget (typically `Text`) inside a table cell:

```java
ControlEditor editor = new ControlEditor(cursor);
editor.grabHorizontal = true;
editor.grabVertical = true;

Text text = new Text(cursor, SWT.NONE);
editor.setEditor(text);   // places Text over the active cell
text.setFocus();

text.addFocusListener(focusLostAdapter(e -> {
    cursor.getRow().setText(cursor.getColumn(), text.getText()); // commit
    text.dispose();
}));
```

ESC restores the original value; any navigation key commits and closes the editor.

---

## Summary — Data Flow

```
TableConfig ──► DefaultTableForm.create()
                    ├─ new Table(parent, style)
                    ├─ new TableCursor(table)
                    └─ ControlEditor for inline editing

TableViewer.read()
    ├─ dataSource.read() ──► TModel
    ├─ buildHeaders()    ──► TableColumn per column
    └─ buildRows()
          ├─ VIRTUAL: setItemCount() + SWT.SetData fills rows on scroll
          └─ eager:   new TableItem() per row, setText() per cell
```

---

## Project Reference

| Class | Role |
|---|---|
| `DefaultTableConfig` | Style flags, header/line visibility, editability |
| `DefaultTableForm` | Creates `Table`, `TableCursor`, `ControlEditor` |
| `TableViewer<TModel>` | Abstract base: builds columns/rows, handles `SetData` |
| `TableModelViewer` | Concrete viewer backed by `TableModel` |
| `TableModel` | Backing data: `TableHeader[]` + `List<TableRow>` |
