/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.lttng.kernel.ui.views.controlflow2;

import org.lttng.scope.lttng.kernel.core.views.controlflow2.ControlFlowRenderProvider;
import org.lttng.scope.tmf2.views.ui.timegraph.swtjfx.SwtJfxTimeGraphView;

public class SwtJfxControlFlowView extends SwtJfxTimeGraphView {

    private static final String VIEW_ID = "org.eclipse.tracecompass.analysis.os.linux.views.controlflow2.swtjfx"; //$NON-NLS-1$

    public SwtJfxControlFlowView() {
        super(VIEW_ID, new ControlFlowRenderProvider());
    }

}
