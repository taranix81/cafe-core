package org.taranix.cafe.graphics.events;

import lombok.Getter;
import org.eclipse.swt.SWT;

import java.util.Arrays;

public enum SWTEventType {
    MouseMove(SWT.MouseMove),
    MouseEnter(SWT.MouseEnter),
    Resize(SWT.Resize),
    Dispose(SWT.Dispose),
    Activate(SWT.Activate),
    KeyDown(SWT.KeyDown),
    KeyUp(SWT.KeyUp),
    Selection(SWT.Selection),
    Move(SWT.Move),
    Arm(SWT.Arm),
    Close(SWT.Close),
    Modify(SWT.Modify),

    SetData(SWT.SetData);

    @Getter
    private final int swtEventType;

    SWTEventType(int swtEventType) {
        this.swtEventType = swtEventType;
    }

    public static String decode(int code) {
        return Arrays.stream(SWTEventType.values())
                .filter(swtEventType1 -> swtEventType1.getSwtEventType() == code)
                .findFirst()
                .map(Enum::name)
                .orElse(Integer.toString(code));
    }
}
