/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.lttng.scope.common.core.NestingBoolean;
import org.lttng.scope.tmf2.views.ui.context.ViewGroupContextManager;

import com.efficios.jabberwocky.timegraph.control.TimeGraphModelControl;
import com.efficios.jabberwocky.timegraph.model.provider.ITimeGraphModelProvider;

import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;

/**
 * Stub view for the {@link TimeGraphWidget} tests, which implements a view
 * containing a single stub widget.
 *
 * This class is required to be public because it is called by Eclipse's
 * extension mechanisms.
 */
public class StubView extends ViewPart {

    /** The view's ID */
    public static final String VIEW_ID = "org.lttng.scope.tmf2.views.ui.tests.timegraph.swtjfx"; //$NON-NLS-1$

    private final ITimeGraphModelProvider fModelRenderProvider;
    private final TimeGraphModelControl fModelControl;

    private @Nullable TimeGraphWidget fViewer;

    /**
     * Constructor
     */
    public StubView() {
        super();
        fModelRenderProvider = new StubModelProvider();
        fModelControl = new TimeGraphModelControl(ViewGroupContextManager.getCurrent(), fModelRenderProvider);
    }

    @Override
    public void createPartControl(@Nullable Composite parent) {
        if (parent == null) {
            return;
        }
        FXCanvas fxCanvas = new FXCanvas(parent, SWT.NONE);


        TimeGraphWidget viewer = new TimeGraphWidget(fModelControl, new NestingBoolean());
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

    @Nullable TimeGraphWidget getViewer() {
        return fViewer;
    }
}
