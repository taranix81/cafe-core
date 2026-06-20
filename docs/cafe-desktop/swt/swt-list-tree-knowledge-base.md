# SWT List and Tree — Knowledge Base

---

## Part 1 — `List`

### 1.1 Construction

`List` is a single-column, flat widget for displaying a scrollable list of strings.

```java
List list = new List(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
```

Key style flags:

| Flag | Effect |
|---|---|
| `SWT.SINGLE` | Only one item can be selected at a time |
| `SWT.MULTI` | Multiple items selectable (click + Ctrl/Shift) |
| `SWT.V_SCROLL` / `SWT.H_SCROLL` | Scrollbars |

> `List` has **no VIRTUAL mode** and **no columns** — it is a plain string list. For rich data use `Table` or `Tree`.

---

### 1.2 Data Providing

All items are plain `String`s. Provide them in bulk or one by one:

```java
// bulk — replaces all items
list.setItems(new String[]{"Alpha", "Beta", "Gamma"});

// append one
list.add("Delta");

// insert at index
list.add("New Item", 1);  // inserts before index 1

// remove
list.remove(2);           // by index
list.remove("Beta");      // by value (first match)
list.removeAll();
```

Reading back:
```java
String[] all      = list.getItems();
String   item     = list.getItem(0);
int      count    = list.getItemCount();
```

---

### 1.3 Selection

```java
// programmatic selection
list.setSelection(new int[]{0, 2});   // select by index
list.setSelection(new String[]{"Alpha"}); // select by value

// read selection
int[]    indices = list.getSelectionIndices();
String[] values  = list.getSelection();

// listen
list.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
    String[] selected = list.getSelection();
}));

// double-click (default action)
list.addSelectionListener(SelectionListener.widgetDefaultSelectedAdapter(e -> {
    // fired on double-click or Enter
}));
```

---

### 1.4 Sorting

`List` has no built-in sort. Sort the backing array before calling `setItems()`:

```java
String[] items = data.stream().sorted().toArray(String[]::new);
list.setItems(items);
```

To keep selection stable after re-sort, record selected values before, restore after:

```java
String[] selected = list.getSelection();
list.setItems(sorted);
list.setSelection(selected);
```

---

### 1.5 Summary — List Data Flow

```
String[] / List<String> (backing data)
    └─ sort if needed
    └─ list.setItems(array)          ← full replace
          or list.add(item)          ← append
    └─ SelectionListener             ← react to user selection
```

---

---

## Part 2 — `Tree`

### 2.1 Construction

`Tree` is a hierarchical widget. Items can have children, forming a collapsible tree structure. It optionally supports multiple columns (like `Table`).

```java
Tree tree = new Tree(parent, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
tree.setHeaderVisible(true);   // show column headers (only meaningful with TreeColumns)
tree.setLinesVisible(true);
```

Key style flags:

| Flag | Effect |
|---|---|
| `SWT.SINGLE` / `SWT.MULTI` | Selection mode |
| `SWT.CHECK` | Checkbox per item |
| `SWT.FULL_SELECTION` | Whole row highlighted (default: first column only) |
| `SWT.VIRTUAL` | Lazy rendering — essential for deep/wide trees |

---

### 2.2 Columns (`TreeColumn`)

Same API as `TableColumn`. Only meaningful when you want multi-column rows:

```java
TreeColumn col0 = new TreeColumn(tree, SWT.LEFT);
col0.setText("Name");
col0.setWidth(200);
col0.setMoveable(true);

TreeColumn col1 = new TreeColumn(tree, SWT.LEFT);
col1.setText("Value");
col1.setWidth(100);
col1.pack();
```

Without `TreeColumn`s the tree shows a single unlabelled column.

---

### 2.3 Data Providing

#### Flat root items

```java
TreeItem root = new TreeItem(tree, SWT.NONE);
root.setText("Root A");              // single column
root.setText(new String[]{"Root A", "val1"});  // multi-column
root.setImage(image);               // optional icon
root.setExpanded(true);             // expand programmatically
```

#### Child items

```java
TreeItem child = new TreeItem(root, SWT.NONE);  // parent is TreeItem, not Tree
child.setText("Child 1");

TreeItem grandchild = new TreeItem(child, SWT.NONE);
grandchild.setText("Grandchild");
```

#### Inserting at a position

```java
TreeItem item = new TreeItem(root, SWT.NONE, 0);  // third arg = index within parent
```

#### Removing items

```java
item.dispose();           // removes item and all its children
tree.removeAll();         // clears everything
```

---

### 2.4 Virtual Mode (`SWT.VIRTUAL`)

Same contract as `Table.VIRTUAL`: declare counts, fill on `SWT.SetData`.

```java
Tree tree = new Tree(parent, SWT.VIRTUAL | SWT.BORDER);

// set root count
tree.setItemCount(rootData.size());

tree.addListener(SWT.SetData, event -> {
    TreeItem item = (TreeItem) event.item;
    TreeItem parentItem = item.getParentItem();

    if (parentItem == null) {
        // root level
        int index = tree.indexOf(item);
        Node node = rootData.get(index);
        item.setText(node.getName());
        item.setItemCount(node.getChildCount());  // declare children count
    } else {
        // child level
        int parentIndex = tree.indexOf(parentItem);   // or parentItem.indexOf(item)
        int childIndex  = parentItem.indexOf(item);
        Node node = rootData.get(parentIndex).getChild(childIndex);
        item.setText(node.getName());
        item.setItemCount(node.getChildCount());      // recurse deeper
    }
});
```

> `item.setItemCount(n)` on a `TreeItem` declares how many children it has without creating them — the OS will fire `SetData` for each child when the node is expanded.

To force a lazy refresh (e.g. after data change): `tree.clearAll(true)` — the boolean controls whether children are also cleared recursively.

---

### 2.5 Selection

```java
// read
TreeItem[] selected = tree.getSelection();

// programmatic select
tree.setSelection(new TreeItem[]{item});

// listen
tree.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
    TreeItem[] sel = tree.getSelection();
}));

// expand / collapse event
tree.addTreeListener(new TreeAdapter() {
    @Override
    public void treeExpanded(TreeEvent e) {
        TreeItem item = (TreeItem) e.item;
        // good place to lazy-load children in non-VIRTUAL trees
    }
    @Override
    public void treeCollapsed(TreeEvent e) { }
});
```

---

### 2.6 Sorting

No built-in sort. Attach a `SelectionListener` to `TreeColumn` and rebuild the tree:

```java
col0.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
    boolean ascending = tree.getSortColumn() != col0 || tree.getSortDirection() == SWT.DOWN;

    // sort root-level backing data
    rootData.sort(Comparator.comparing(Node::getName,
        ascending ? Comparator.naturalOrder() : Comparator.reverseOrder()));

    // rebuild
    tree.removeAll();
    for (Node node : rootData) {
        TreeItem item = new TreeItem(tree, SWT.NONE);
        item.setText(node.getName());
        buildChildren(item, node);  // recursive helper
    }

    tree.setSortColumn(col0);
    tree.setSortDirection(ascending ? SWT.UP : SWT.DOWN);
}));
```

For VIRTUAL trees: sort backing data, then call `tree.clearAll(true)` — the `SetData` listener re-reads the sorted data on next render.

---

### 2.7 Cell Editing (`TreeEditor`)

`TreeEditor` is the Tree equivalent of `ControlEditor`:

```java
TreeEditor editor = new TreeEditor(tree);
editor.horizontalAlignment = SWT.LEFT;
editor.grabHorizontal = true;

tree.addMouseListener(MouseListener.mouseDoubleClickAdapter(e -> {
    TreeItem item = tree.getSelection()[0];

    Text text = new Text(tree, SWT.NONE);
    text.setText(item.getText());
    text.selectAll();
    text.setFocus();

    text.addFocusListener(focusLostAdapter(ev -> {
        item.setText(text.getText());
        text.dispose();
    }));

    editor.setEditor(text, item, 0);  // widget, row item, column index
}));
```

---

### 2.8 Traversal Utilities

```java
// walk all items recursively
void walk(Tree tree) {
    for (TreeItem root : tree.getItems()) walk(root);
}
void walk(TreeItem item) {
    // process item
    for (TreeItem child : item.getItems()) walk(child);
}

// find parent path
java.util.Deque<TreeItem> path = new ArrayDeque<>();
TreeItem cursor = someItem;
while (cursor != null) {
    path.push(cursor);
    cursor = cursor.getParentItem();  // null at root
}
```

---

## Comparison — List vs Table vs Tree

| | `List` | `Table` | `Tree` |
|---|---|---|---|
| Structure | Flat | Flat | Hierarchical |
| Columns | No (strings only) | Yes | Yes (optional) |
| VIRTUAL support | No | Yes | Yes |
| Sorting (built-in) | No | No | No |
| Cell editing | No | `ControlEditor` | `TreeEditor` |
| Cursor navigation | No | `TableCursor` | No |
| Typical use | Simple string pickers | Grids / data tables | File trees, outlines |

---

## Summary — Tree Data Flow

```
Backing data (tree-shaped model)
    └─ sort if needed

Eager:
    tree.removeAll()
    for each root node:
        new TreeItem(tree, SWT.NONE) → setText / setImage
        for each child:
            new TreeItem(rootItem, SWT.NONE) → setText
            ...

VIRTUAL:
    tree.setItemCount(rootCount)
    SWT.SetData listener:
        item.setText(...)
        item.setItemCount(childCount)   ← declares children, deferred until expand
    tree.clearAll(true)                 ← force refresh after data change
```
