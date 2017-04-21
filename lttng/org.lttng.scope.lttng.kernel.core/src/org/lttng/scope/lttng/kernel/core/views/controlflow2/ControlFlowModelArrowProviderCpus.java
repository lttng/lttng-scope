/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.lttng.kernel.core.views.controlflow2;

import org.lttng.scope.lttng.kernel.core.analysis.os.KernelAnalysisModule;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.statesystem.StateSystemModelArrowProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.ColorDefinition;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.arrows.TimeGraphArrowSeries;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.arrows.TimeGraphArrowSeries.LineStyle;

public class ControlFlowModelArrowProviderCpus extends StateSystemModelArrowProvider {

    private static final TimeGraphArrowSeries ARROW_SERIES = new TimeGraphArrowSeries(
            "CPUs",
            new ColorDefinition(200, 1, 1),
            LineStyle.FULL);

    public ControlFlowModelArrowProviderCpus() {
        super(ARROW_SERIES, KernelAnalysisModule.ID);
    }

}
