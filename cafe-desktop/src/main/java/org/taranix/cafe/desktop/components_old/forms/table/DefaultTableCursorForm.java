package org.taranix.cafe.desktop.components_old.forms.table;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import java.util.function.Consumer;

import static org.eclipse.swt.events.FocusListener.focusLostAdapter;

//@CafeService
class DefaultTableCursorForm implements TableCursorForm {
    @Override
    public TableCursor create(Widget parent) {
        final TableCursor cursor = new TableCursor((Table) parent, SWT.NONE);
        Color gray = parent.getDisplay().getSystemColor(SWT.COLOR_GRAY);
        cursor.setBackground(gray);

        final ControlEditor editor = getControlEditor(cursor);

        cursor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                Text text = createCellEditor(cursor, editor, s -> setActiveCellText(cursor, s));
                text.setText(String.valueOf(e.character));
                //move caret to the end
                text.setSelection(1, 1);
            }
        });

        return cursor;
    }

    private ControlEditor getControlEditor(TableCursor cursor) {
        final ControlEditor editor = new ControlEditor(cursor);
        editor.grabHorizontal = true;
        editor.grabVertical = true;
        return editor;
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

    private void setActiveCellText(TableCursor cursor, String data) {
        cursor.getRow().setText(cursor.getColumn(), data);
    }
}
