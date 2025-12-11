package org.taranix.cafe.desktop.actions;

import lombok.Builder;
import lombok.Getter;

@Builder
public class ClosingAction extends Action {

    @Getter
    private boolean continueClosing = true;

    public void abort() {
        continueClosing = false;
    }
}
