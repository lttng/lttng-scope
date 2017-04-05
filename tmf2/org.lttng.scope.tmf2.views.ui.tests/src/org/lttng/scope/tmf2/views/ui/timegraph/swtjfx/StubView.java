/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timegraph.swtjfx;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;
import org.lttng.scope.tmf2.views.core.timegraph.control.TimeGraphModelControl;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.ITimeGraphModelRenderProvider;

import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;

/**
 * Stub view for the {@link SwtJfxTimeGraphViewer} tests, which implements a
 * view containing a single stub widget.
 *
 * This class is required to be public because it is called by Eclipse's
 * extension mechanisms.
 */
public class StubView extends TmfView {

    /** The view's ID */
    public static final String VIEW_ID = "org.lttng.scope.tmf2.views.ui.tests.timegraph.swtjfx"; //$NON-NLS-1$

    private final ITimeGraphModelRenderProvider fModelRenderProvider;
    private final TimeGraphModelControl fModelControl;

    private @Nullable SwtJfxTimeGraphViewer fViewer;

    /**
     * Constructor
     */
    public StubView() {
        super(VIEW_ID);
        fModelRenderProvider = new StubModelRenderProvider();
        fModelControl = new TimeGraphModelControl(fModelRenderProvider);
    }

    @Override
    public void createPartControl(@Nullable Composite parent) {
        if (parent == null) {
            return;
        }
        FXCanvas fxCanvas = new FXCanvas(parent, SWT.NONE);


        SwtJfxTimeGraphViewer viewer = new SwtJfxTimeGraphViewer(fModelControl);
        fModelControl.attachView(viewer);

        fxCanvas.setScene(new Scene(viewer.getRootNode()));
        viewer.getSplitPane().setDividerPositions(0.2);

        fViewer = viewer;
    }

    @Override
    public void dispose() {
        fModelControl.dispose();
    }

    @Override
    public void setFocus() {
    }

    TimeGraphModelControl getControl() {
        return fModelControl;
    }

    @Nullable SwtJfxTimeGraphViewer getViewer() {
        return fViewer;
    }
}
