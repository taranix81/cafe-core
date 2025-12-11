package org.taranix.cafe.desktop.components_old.viewers;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.desktop.components_old.forms.table.TableForm;
import org.taranix.cafe.desktop.components_old.model.table.TableModel;

@Slf4j
//@CafeService(scope = Scope.Prototype)
class TableModelViewer extends TableViewer<TableModel> {


    protected TableModelViewer(TableForm tableForm) {
        super(tableForm);
    }

    @Override
    protected Object getColumnId(int column) {
        return getModel().getHeader(column).getId();
    }

    @Override
    protected String getColumnName(int column) {
        return getModel().getHeader(column).getText();
    }

    @Override
    protected int getRowsAmount() {
        return getModel().getAmount();
    }

    @Override
    protected int getColumnsAmount() {
        return getModel().headersAmount();
    }

    @Override
    protected String getCellData(int rowIndex, int columnIndex) {
        return String.valueOf(getModel().getCell(rowIndex, columnIndex));
    }


}
