package org.lttng.scope.tmf2.views.ui.timegraph.swtjfx;

import org.eclipse.swt.widgets.Composite;
import org.lttng.scope.tmf2.views.core.timegraph.control.TimeGraphModelControl;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;

public class SwtJfxTimeGraphViewerStub extends SwtJfxTimeGraphViewer {

    public SwtJfxTimeGraphViewerStub(Composite parent, TimeGraphModelControl control) {
        super(parent, control);
    }

    // ------------------------------------------------------------------------
    // Visibility-increasing overrides
    // ------------------------------------------------------------------------

    @Override
    protected Pane getTimeGraphPane() {
        return super.getTimeGraphPane();
    }

    @Override
    protected ScrollPane getTimeGraphScrollPane() {
        return super.getTimeGraphScrollPane();
    }
}
