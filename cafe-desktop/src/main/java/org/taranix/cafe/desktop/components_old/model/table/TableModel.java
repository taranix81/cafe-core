package org.taranix.cafe.desktop.components_old.model.table;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TableModel {


    private TableHeader[] headers;
    private List<TableRow> rows = new ArrayList<>();

    public TableModel(TableHeader[] headers) {
        this.headers = headers;
    }

    public int headersAmount() {
        return headers.length;
    }

    public Object getCell(int rowIndex, int columnIndex) {
        return getRow(rowIndex).get(columnIndex);
    }

    public TableRow getRow(int index) {
        return rows.get(index);
    }

    public TableHeader getHeader(int index) {
        return headers[index];
    }

    public TableRow remove(int rowIndex) {
        return rows.remove(rowIndex);
    }

    public TableRow createRow() {
        TableRow tableRow = new TableRow(this);
        rows.add(tableRow);
        return tableRow;
    }

    public int getAmount() {
        return rows.size();
    }


//    public <U> void sort(Function<? super TableRow, ? extends TableRow> comparator) {
//        rows = rows.stream().sorted(Comparator.comparing(comparator)).toList();
//    }
}
