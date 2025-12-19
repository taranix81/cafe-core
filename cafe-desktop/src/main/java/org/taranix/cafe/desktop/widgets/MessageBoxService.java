package org.taranix.cafe.desktop.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.taranix.cafe.beans.annotations.classes.CafeService;

import java.nio.file.Path;
import java.util.Optional;

@CafeService
public class MessageBoxService {


    public boolean showYesNoDialog(Shell shell, String question, String title) {
        if (shell.isDisposed()) {
            return false;
        }
        MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION
                | SWT.YES | SWT.NO);
        messageBox.setMessage(question);
        messageBox.setText(title);
        return messageBox.open() == SWT.YES;
    }

    public Path showSaveFileDialog(Shell shell) {
        if (shell.isDisposed()) {
            return null;
        }
        FileDialog fd = new FileDialog(shell, SWT.SAVE);
        return Optional.ofNullable(fd.open()).map(Path::of).orElse(null);
    }

    public Path showOpenFileDialog(Shell shell) {
        if (shell.isDisposed()) {
            return null;
        }
        FileDialog fd = new FileDialog(shell, SWT.OPEN);
        return Optional.ofNullable(fd.open()).map(Path::of).orElse(null);
    }

    public void showWarningDialog(Shell shell, String info, String title) {
        if (shell.isDisposed()) {
            return;
        }
        MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
        messageBox.setMessage(info);
        messageBox.setText(title);
        messageBox.open();
    }
}
