/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.tmf2.views.core.timegraph.model.provider;

import java.util.List;
import java.util.Set;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.lttng.scope.tmf2.views.core.TimeRange;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.arrows.TimeGraphArrowRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.drawnevents.TimeGraphDrawnEventRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeElement;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeRender;

public interface ITimeGraphModelRenderProvider {

    // ------------------------------------------------------------------------
    // Configuration option classes
    // ------------------------------------------------------------------------

    class SortingMode {

        private final String fName;

        public SortingMode(String name) {
            fName = name;
        }

        public String getName() {
            return fName;
        }
    }

    class FilterMode {

        private final String fName;

        public FilterMode(String name) {
            fName = name;
        }

        public String getName() {
            return fName;
        }
    }

    void setTrace(@Nullable ITmfTrace trace);

    // ------------------------------------------------------------------------
    // Render generation methods
    // ------------------------------------------------------------------------

    TimeGraphTreeRender getTreeRender();

    TimeGraphStateRender getStateRender(TimeGraphTreeElement treeElement,
            TimeRange timeRange, long resolution, @Nullable FutureTask<?> task);

    default List<TimeGraphStateRender> getStateRenders(TimeGraphTreeRender treeRender, TimeRange timeRange, long resolution, @Nullable FutureTask<?> task) {
        return treeRender.getAllTreeElements().stream()
                .map(treeElem -> getStateRender(treeElem, timeRange, resolution, task))
                .collect(Collectors.toList());
    }

    TimeGraphDrawnEventRender getDrawnEventRender(TimeGraphTreeElement treeElement, TimeRange timeRange);

    TimeGraphArrowRender getArrowRender(TimeGraphTreeRender treeRender);


    // ------------------------------------------------------------------------
    // Sorting modes
    // ------------------------------------------------------------------------

    List<SortingMode> getSortingModes();

    SortingMode getCurrentSortingMode();

    void setCurrentSortingMode(int index);

    // ------------------------------------------------------------------------
    // Filter modes
    // ------------------------------------------------------------------------

    List<FilterMode> getFilterModes();

    void enableFilterMode(int index);

    void disableFilterMode(int index);

    Set<FilterMode> getActiveFilterModes();

}
