package org.taranix.cafe.desktop.components_old.model.table;

import org.taranix.cafe.beans.converters.CafeConverter;

import java.util.Arrays;

//@CafeService
class StringToTableModelConverter implements CafeConverter<String, TableModel> {
    @Override
    public TableModel convert(String s) {
        String[] lines = s.split("\n");
        if (lines.length > 1) {

            String[] headers = lines[0].split(";");
            TableHeader[] tableHeaders = Arrays.stream(headers)
                    .map(TableHeader::from).toList()
                    .toArray(new TableHeader[]{});

            TableModel tableModel = new TableModel(tableHeaders);

            for (int i = 1; i < lines.length; i++) {
                String row = lines[i];
                String[] rowValues = row.split(";");
                TableRow tableRow = tableModel.createRow();
                for (int headerIndex = 0; headerIndex < tableHeaders.length; headerIndex++) {
                    tableRow.set(headerIndex, rowValues[headerIndex]);
                }

            }
            return tableModel;
        }
        return null;
    }
}
