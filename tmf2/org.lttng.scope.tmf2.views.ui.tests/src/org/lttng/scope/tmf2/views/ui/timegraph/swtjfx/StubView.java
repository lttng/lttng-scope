/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timegraph.swtjfx;

/**
 * Stub view for the {@link SwtJfxTimeGraphViewer} tests, providing a concrete
 * implementation of {@link SwtJfxTimeGraphView}.
 *
 * This class is required to be public because it is called by Eclipse's
 * extension mechanisms.
 */
public class StubView extends SwtJfxTimeGraphView {

    /** The view's ID */
    public static final String VIEW_ID = "org.lttng.scope.tmf2.views.ui.tests.timegraph.swtjfx"; //$NON-NLS-1$

    /**
     * Constructor
     */
    public StubView() {
        super(VIEW_ID, new StubModelRenderProvider());
    }

}
