/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.tmf2.views.core.timegraph.model.provider;

import static org.lttng.scope.common.core.NonNullUtils.nullToEmptyString;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.FutureTask;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.arrows.TimeGraphArrowRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.drawnevents.TimeGraphDrawnEventRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateInterval;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tooltip.TimeGraphTooltip;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeElement;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeRender;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public abstract class TimeGraphModelRenderProvider implements ITimeGraphModelRenderProvider {

    protected static final SortingMode DEFAULT_SORTING_MODE = new SortingMode(nullToEmptyString(Messages.DefaultSortingModeName));

    private final List<SortingMode> fSortingModes;
    private final List<FilterMode> fFilterModes;

    private final Set<FilterMode> fActiveFilterModes = new HashSet<>();
    private SortingMode fCurrentSortingMode;

    private @Nullable ITmfTrace fCurrentTrace;

    protected TimeGraphModelRenderProvider(@Nullable List<SortingMode> sortingModes,
            @Nullable List<FilterMode> filterModes) {
        if (sortingModes == null || sortingModes.isEmpty()) {
            fSortingModes = ImmutableList.of(DEFAULT_SORTING_MODE);
        } else {
            fSortingModes = ImmutableList.copyOf(sortingModes);

        }
        fCurrentSortingMode = fSortingModes.get(0);

        if (filterModes == null || filterModes.isEmpty()) {
            fFilterModes = ImmutableList.of();
        } else {
            fFilterModes = ImmutableList.copyOf(filterModes);
        }
    }

    @Override
    public final void setTrace(@Nullable ITmfTrace trace) {
        fCurrentTrace = trace;
    }

    protected final @Nullable ITmfTrace getCurrentTrace() {
        return fCurrentTrace;
    }

    // ------------------------------------------------------------------------
    // Render generation methods. Implementation left to subclasses.
    // ------------------------------------------------------------------------

    @Override
    public abstract TimeGraphTreeRender getTreeRender();

    @Override
    public abstract TimeGraphStateRender getStateRender(TimeGraphTreeElement treeElement,
            long rangeStart, long rangeEnd, long resolution, @Nullable FutureTask<?> task);

    @Override
    public abstract TimeGraphDrawnEventRender getDrawnEventRender(
            TimeGraphTreeElement treeElement, long rangeStart, long rangeEnd);

    @Override
    public abstract TimeGraphArrowRender getArrowRender(TimeGraphTreeRender treeRender);

    @Override
    public abstract TimeGraphTooltip getTooltip(TimeGraphStateInterval interval);

    // ------------------------------------------------------------------------
    // Sorting modes
    // ------------------------------------------------------------------------

    @Override
    public final List<SortingMode> getSortingModes() {
        return fSortingModes;
    }

    @Override
    public final SortingMode getCurrentSortingMode() {
        return fCurrentSortingMode;
    }

    @Override
    public final void setCurrentSortingMode(int index) {
        fCurrentSortingMode = fSortingModes.get(index);
    }

    // ------------------------------------------------------------------------
    // Filter modes
    // ------------------------------------------------------------------------

    @Override
    public final List<FilterMode> getFilterModes() {
        return fFilterModes;
    }

    @Override
    public final void enableFilterMode(int index) {
        fActiveFilterModes.add(fFilterModes.get(index));
    }

    @Override
    public final void disableFilterMode(int index) {
        fActiveFilterModes.remove(fFilterModes.get(index));
    }

    @Override
    public final Set<FilterMode> getActiveFilterModes() {
        return ImmutableSet.copyOf(fActiveFilterModes);
    }

}
