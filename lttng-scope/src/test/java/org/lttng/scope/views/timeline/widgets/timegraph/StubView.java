/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline.widgets.timegraph;

import com.efficios.jabberwocky.views.timegraph.control.TimeGraphModelControl;
import com.efficios.jabberwocky.views.timegraph.model.provider.ITimeGraphModelProvider;
import org.lttng.scope.common.NestingBoolean;
import org.lttng.scope.views.context.ViewGroupContextManager;

/**
 * Wrapper object around a view and its control, used for tests.
 */
public class StubView {

    private final TimeGraphModelControl fModelControl;
    private final TimeGraphWidget fViewer;

    /**
     * Constructor
     */
    public StubView() {
        ITimeGraphModelProvider fModelRenderProvider = new StubModelProvider();
        fModelControl = new TimeGraphModelControl(ViewGroupContextManager.getCurrent(), fModelRenderProvider);

        TimeGraphWidget viewer = new TimeGraphWidget(fModelControl, new NestingBoolean(), 0);
        fModelControl.setView(viewer);
        fViewer = viewer;
    }

    public void dispose() {
        fModelControl.dispose();
    }

    TimeGraphModelControl getControl() {
        return fModelControl;
    }

    TimeGraphWidget getViewer() {
        return fViewer;
    }
}
