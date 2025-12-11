package org.taranix.cafe.desktop.components_old.model.table;

public class TableCell<T> {
    private final TableHeader header;
    private TableRow row;
    private T value;


    public TableCell(TableHeader header) {
        this.header = header;
    }
}
