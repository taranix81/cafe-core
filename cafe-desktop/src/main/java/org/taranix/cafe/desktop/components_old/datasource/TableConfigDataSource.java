package org.taranix.cafe.desktop.components_old.datasource;

import lombok.Getter;
import lombok.Setter;
import org.taranix.cafe.beans.converters.CafeConverter;
import org.taranix.cafe.desktop.components_old.AbstractComponent;
import org.taranix.cafe.desktop.components_old.model.table.TableModel;

//@CafeService(scope = Scope.Prototype)
public class TableConfigDataSource extends AbstractComponent implements DataSource<TableModel> {

    private final CafeConverter<String, TableModel> stringToTableConfigCafeConverter;

    private final CafeConverter<TableModel, String> tableConfigToStringCafeConverter;

    @Getter
    @Setter
    private DataSource<String> stringDataSource;

    public TableConfigDataSource(CafeConverter<String, TableModel> stringToTableConfigCafeConverter, CafeConverter<TableModel, String> tableConfigToStringCafeConverter, DataSource<String> stringDataSource) {
        this.stringToTableConfigCafeConverter = stringToTableConfigCafeConverter;
        this.tableConfigToStringCafeConverter = tableConfigToStringCafeConverter;

    }

    @Override
    public String getName() {
        return stringDataSource.getName();
    }

    @Override
    public boolean write(TableModel tableModel) {
        String data = tableConfigToStringCafeConverter.convert(tableModel);
        return stringDataSource.write(data);
    }

    @Override
    public TableModel read() {
        String data = stringDataSource.read();
        return stringToTableConfigCafeConverter.convert(data);
    }
}
