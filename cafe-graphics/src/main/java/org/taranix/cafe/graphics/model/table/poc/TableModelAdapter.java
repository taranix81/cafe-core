package org.taranix.cafe.graphics.model.table.poc;

public class TableModelAdapter<T> {

    //  private final List<GetMapper<T, Object>> getMappers;
//    private final List<SetMapper<T, Object>> setMappers;

//    protected TableModelAdapter(GetMapper<T, ?> getters, SetMapper<T, ?> setters) {
//        this.getMappers = List.of(getters);
//        this.setMappers = List.of(setters);
//
//
//    }

//    public <T> TableModelAdapter(GetMapper<T, Object> getters, SetMapper<T, Object> setters) {
//        this.getMappers = List.of(getters);
//        this.setMappers = List.of(setters);
//
//    }

//    public void main(String[] args) {
//
//        TableModelAdapter<TestData> adapter = new TableModelAdapter<>(TestData::getN1, TestData::setN1);
//        adapter.readTo(TestData::setN2);
//    }
//
//    private void readTo(BiConsumer<T, Integer> f) {
//        T d = null;
////        f.accept(d, "he;;");
//    }
//
//    public List<T> read() {
//        List<T> result = new ArrayList<>();
//        return result;
//    }
//
//
//    public void load(Table table, List<T> data) {
//        if (isVirtual(table)) {
//            table.setItemCount(data.size());
//            table.addListener(SWT.SetData, event -> writeValues(data, event));
//        } else {
//            data.forEach(dataItem -> writeValues(dataItem, new TableItem(table, SWT.NULL)));
//        }
//
//    }
//
//    private boolean isVirtual(Table table) {
//        return (table.getStyle() & SWT.VIRTUAL) == SWT.VIRTUAL;
//    }
//
//    private void writeValues(List<T> data, Event event) {
//        TableItem tableItem = (TableItem) event.item;
//        Table table = tableItem.getParent();
//        int rowIndex = table.indexOf(tableItem);
//        T dataItem = data.get(rowIndex);
//        writeValues(dataItem, tableItem);
//    }
//
////    private void readValue(T dataItem, TableItem tableItem) {
////        for (int columnIndex = 0; columnIndex < columnReadMappers.size(); columnIndex++) {
////            String value = tableItem.getText(columnIndex);
////            columnReadMappers.get(columnIndex).apply(value);
////
////        }
////    }
//
//    private void writeValues(T dataItem, TableItem tableItem) {
//        for (int columnIndex = 0; columnIndex < getMappers.size(); columnIndex++) {
//            tableItem.setText(columnIndex, getMappers.get(columnIndex).apply(dataItem));
//        }
//    }

//    private void injectValues(T dataItem) {
//        TableItem tableItem = new TableItem(table, SWT.NULL);
//        injectValues(dataItem, tableItem);
//    }
}
