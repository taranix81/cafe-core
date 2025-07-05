package org.taranix.cafe.graphics.components.viewers;

import lombok.AccessLevel;
import lombok.Getter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.taranix.cafe.graphics.events.SWTEventType;
import org.taranix.cafe.graphics.events.annotations.SwtEventHandler;
import org.taranix.cafe.graphics.forms.table.TableForm;

public abstract class TableDataView<TModel> extends AbstractDataView {

    @Getter(AccessLevel.PROTECTED)
    private final TableForm tableForm;
    @Getter(AccessLevel.PROTECTED)
    private TModel model;

    protected TableDataView(TableForm tableForm) {
        this.tableForm = tableForm;
    }

    protected Table table() {
        return (Table) getWidget();
    }

    @Override
    public Widget createWidget(Widget parent) {
        return tableForm.create(parent);
    }

    @Override
    public void read() {
        // model = getDataProvider().read();
        buildHeaders();
        buildRows();
    }

    private void buildRows() {
        boolean isVirtual = (table().getStyle() & SWT.VIRTUAL) == SWT.VIRTUAL;
        if (isVirtual) {
            table().setItemCount(getRowsAmount());
        } else {
            buildTableItems();
        }
    }

    private void buildTableItems() {
        for (int rowIndex = 0; rowIndex < getRowsAmount(); rowIndex++) {
            for (int colIndex = 0; colIndex < getColumnsAmount(); colIndex++) {
                TableItem tableItem = new TableItem(table(), SWT.None);
                tableItem.setText(colIndex, getCellData(rowIndex, colIndex));
            }
        }
    }

    private void buildHeaders() {
        for (int colIndex = 0; colIndex < getColumnsAmount(); colIndex++) {
            TableColumn column = new TableColumn(table(), SWT.NULL);
            column.setText(getColumnName(colIndex));
            column.setData("id", getColumnId(colIndex));
            column.setMoveable(true);
            column.getResizable();
            table().getColumn(colIndex).pack();
        }
    }

    protected abstract Object getColumnId(int column);

    protected abstract String getColumnName(int column);

    protected abstract int getRowsAmount();

    @SwtEventHandler(eventType = SWTEventType.SetData)
    private void onTableSetData(Event event) {
        TableItem tableItem = (TableItem) event.item;
        int rowIndex = tableItem.getParent().indexOf(tableItem);
        for (int column = 0; column < getColumnsAmount(); column++) {
            tableItem.setText(column, getCellData(rowIndex, column));
        }
    }

    protected abstract int getColumnsAmount();

    protected abstract String getCellData(int rowIndex, int columnIndex);
}
