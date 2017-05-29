/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.core.timegraph.model.provider.arrows;

import java.util.concurrent.FutureTask;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.arrows.TimeGraphArrowRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.arrows.TimeGraphArrowSeries;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeRender;

import com.efficios.jabberwocky.common.TimeRange;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;

/**
 * Provider for timegraph arrow series.
 *
 * It can be used stand-alone (ie, for testing) but usually would be part of a
 * {@link org.lttng.scope.tmf2.views.core.timegraph.model.provider.ITimeGraphModelProvider}.
 *
 * @author Alexandre Montplaisir
 */
public interface ITimeGraphModelArrowProvider {

    /**
     * Property representing the trace this arrow provider fetches its data for.
     *
     * @return The trace property
     */
    ObjectProperty<@Nullable ITmfTrace> traceProperty();

    /**
     * Property indicating if this specific arrow provider is currently enabled
     * or not.
     *
     * Normally the controls should not send queries to this provider if it is
     * disabled (they would only query this state), but the arrow provider is
     * free to make use of this property for other reasons.
     *
     * @return The enabled property
     */
    BooleanProperty enabledProperty();

    /**
     * Get the arrow series supplied by this arrow provider. A single provider
     * supplies one and only one arrow series.
     *
     * @return The arrow series
     */
    TimeGraphArrowSeries getArrowSeries();

    /**
     * Get a render of arrows from this arrow provider.
     *
     * @param treeRender
     *            The tree render for which the query is done
     * @param timeRange
     *            The time range of the query. The provider may decide to
     *            include arrows partially inside this range, or not.
     * @param task
     *            An optional task parameter, which can be checked for
     *            cancellation to stop processing at any point and return.
     * @return The corresponding arrow render
     */
    TimeGraphArrowRender getArrowRender(TimeGraphTreeRender treeRender, TimeRange timeRange, @Nullable FutureTask<?> task);

}
