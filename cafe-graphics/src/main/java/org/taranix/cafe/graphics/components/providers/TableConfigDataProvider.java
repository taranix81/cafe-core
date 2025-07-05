package org.taranix.cafe.graphics.components.providers;

import lombok.Getter;
import lombok.Setter;
import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.beans.annotations.Scope;
import org.taranix.cafe.beans.converters.CafeConverter;
import org.taranix.cafe.graphics.components.AbstractComponent;
import org.taranix.cafe.graphics.model.table.TableModel;

@CafeService(scope = Scope.Prototype)
public class TableConfigDataProvider extends AbstractComponent implements DataProvider<TableModel> {

    private final CafeConverter<String, TableModel> stringToTableConfigCafeConverter;

    private final CafeConverter<TableModel, String> tableConfigToStringCafeConverter;

    @Getter
    @Setter
    private DataProvider<String> stringDataProvider;

    public TableConfigDataProvider(CafeConverter<String, TableModel> stringToTableConfigCafeConverter, CafeConverter<TableModel, String> tableConfigToStringCafeConverter, DataProvider<String> stringDataProvider) {
        this.stringToTableConfigCafeConverter = stringToTableConfigCafeConverter;
        this.tableConfigToStringCafeConverter = tableConfigToStringCafeConverter;

    }

    @Override
    public String getName() {
        return stringDataProvider.getName();
    }

    @Override
    public boolean write(TableModel tableModel) {
        String data = tableConfigToStringCafeConverter.convert(tableModel);
        return stringDataProvider.write(data);
    }

    @Override
    public TableModel read() {
        String data = stringDataProvider.read();
        return stringToTableConfigCafeConverter.convert(data);
    }
}
