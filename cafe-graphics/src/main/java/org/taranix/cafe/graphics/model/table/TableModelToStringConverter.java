package org.taranix.cafe.graphics.model.table;

import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.beans.converters.CafeConverter;

@CafeService
public class TableModelToStringConverter implements CafeConverter<TableModel, String> {
    @Override
    public String convert(TableModel tableModel) {
        return null;
    }
}
