/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.tmf2.views.core.timegraph.model.provider;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.arrows.ITimeGraphModelArrowProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.states.ITimeGraphModelStateProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeRender;

public interface ITimeGraphModelProvider {

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

    // ------------------------------------------------------------------------
    // General methods
    // ------------------------------------------------------------------------

    String getName();

    void setTrace(@Nullable ITmfTrace trace);

    // ------------------------------------------------------------------------
    // Render providers
    // ------------------------------------------------------------------------

    TimeGraphTreeRender getTreeRender();

    ITimeGraphModelStateProvider getStateProvider();

    Collection<ITimeGraphModelArrowProvider> getArrowProviders();

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
