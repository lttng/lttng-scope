/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timegraph.swtjfx;

public class SwtJfxTimeGraphViewStub extends SwtJfxTimeGraphView {

    public static final String VIEW_ID = "org.lttng.scope.tmf2.views.ui.tests.timegraph.swtjfx"; //$NON-NLS-1$

    public SwtJfxTimeGraphViewStub() {
        super(VIEW_ID, new ModelRenderProviderFixture());
    }

}
