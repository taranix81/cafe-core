package org.taranix.cafe.graphics.components.viewers;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.beans.annotations.Scope;
import org.taranix.cafe.graphics.forms.table.TableForm;
import org.taranix.cafe.graphics.model.table.TableModel;

@Slf4j
@CafeService(scope = Scope.Prototype)
class TableModelDataView extends TableDataView<TableModel> {


    protected TableModelDataView(TableForm tableForm) {
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
