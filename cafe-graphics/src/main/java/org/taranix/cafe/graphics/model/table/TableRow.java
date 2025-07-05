package org.taranix.cafe.graphics.model.table;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class TableRow {
    @Getter
    private final TableModel tableModel;


    private final Map<TableHeader, Object> cells = new HashMap<>();

    TableRow(TableModel tableModel) {
        this.tableModel = tableModel;
    }

    public TableHeader[] getTableHeaders() {
        return tableModel.getHeaders();
    }

    public TableRow set(TableHeader header, Object value) {
        cells.put(header, value);
        return this;
    }

    public TableRow set(int headerIndex, Object value) {
        return set(getTableHeaders()[headerIndex], value);
    }

    public Object get(TableHeader header) {
        return cells.getOrDefault(header, StringUtils.EMPTY);
    }

    public Object get(int headerIndex) {
        return get(getTableHeaders()[headerIndex]);
    }
}
