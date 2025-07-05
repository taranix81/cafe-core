package org.taranix.cafe.graphics.forms.table;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.taranix.cafe.beans.annotations.CafeService;

import java.util.Optional;
import java.util.function.Consumer;

import static org.eclipse.swt.events.FocusListener.focusLostAdapter;

@CafeService
class DefaultTableForm implements TableForm {

    private final TableConfig tableConfig;

    DefaultTableForm(TableConfig tableConfig) {
        this.tableConfig = tableConfig;
    }

    @Override
    public Table create(Widget parent) {
        Table table = new Table((Composite) parent, tableConfig.getTableStyle());
        table.setHeaderVisible(tableConfig.isHeaderVisible());
        table.setLinesVisible(tableConfig.isLinesVisible());
        table.setHeaderBackground(tableConfig.getHeaderBackground());


        //Hide row selection
        hideRowSelectionInTable(table);

        // create a TableCursor to navigate around the table
        final TableCursor cursor = new TableCursor(table, SWT.NONE);
        Color gray = parent.getDisplay().getSystemColor(SWT.COLOR_GRAY);
        cursor.setBackground(gray);


        // create an editor to edit the cell when the user hits "ENTER"
        // while over a cell in the table
        final Optional<ControlEditor> editor = getControlEditor(cursor);

        editor.ifPresent(controlEditor -> cursor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (canEditOnKeyEvent(e)) {
                    Text text = createCellEditor(cursor, controlEditor, s -> setActiveCellText(cursor, s));
                    text.setText(String.valueOf(e.character));
                    //move caret to the end
                    text.setSelection(1, 1);
                }
            }
        }));


        return table;
    }


    private Optional<ControlEditor> getControlEditor(TableCursor cursor) {
        if (tableConfig.isCellEditable()) {
            final ControlEditor editor = new ControlEditor(cursor);
            editor.grabHorizontal = true;
            editor.grabVertical = true;
            return Optional.of(editor);
        }
        return Optional.empty();
    }

    private void hideRowSelectionInTable(Table table) {
        if (tableConfig.isHideRowSelection()) {
            table.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    table.deselectAll();
                }
            });
        }
    }

    private void setActiveCellText(TableCursor cursor, String data) {
        cursor.getRow().setText(cursor.getColumn(), data);
    }

    private String getActiveCellText(TableCursor cursor) {
        return cursor.getRow().getText(cursor.getColumn());
    }

    private boolean canEditOnKeyEvent(KeyEvent e) {
        return e.keyCode != SWT.ARROW_DOWN
                && e.character != SWT.CR
                && e.keyCode != SWT.ARROW_UP
                && e.keyCode != SWT.ARROW_LEFT
                && e.keyCode != SWT.ARROW_RIGHT
                && e.keyCode != SWT.ESC
                ;
    }

    private Text createCellEditor(TableCursor cursor, ControlEditor editor, Consumer<String> consumer) {
        String originalText = getActiveCellText(cursor);

        final Text text = new Text(cursor, SWT.NONE);
        // close the text editor when the user tabs away
        text.addFocusListener(focusLostAdapter(event -> {
            consumer.accept(text.getText());
            text.dispose();
        }));
        text.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!canEditOnKeyEvent(e)) {
                    consumer.accept(e.keyCode == SWT.ESC ? originalText : text.getText());
                    text.dispose();
                }
            }
        });

        editor.setEditor(text);
        text.setFocus();
        return text;
    }


}
