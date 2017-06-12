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

import javafx.beans.property.ObjectProperty;

/**
 * Base interface for time graph model providers.
 *
 * This object is responsible for the generation of the "tree" part of the
 * timegraph, and other generic options like sorting and filtering modes.
 *
 * It also encapsulates one {@link ITimeGraphModelStateProvider} (which is
 * responsible of providing state intervals), and zero or more
 * {@link ITimeGraphModelArrowProvider} (which provide model-defined arrow
 * series).
 *
 * @author Alexandre Montplaisir
 */
public interface ITimeGraphModelProvider {

    // ------------------------------------------------------------------------
    // Configuration option classes
    // ------------------------------------------------------------------------

    /**
     * Class representing one sorting mode. A sorting mode is like a comparator
     * to sort tree elements. Only one can be active at any time.
     *
     * The exact behavior of the sorting mode is defined by the model provider
     * itself.
     */
    class SortingMode {

        private final String fName;

        public SortingMode(String name) {
            fName = name;
        }

        public String getName() {
            return fName;
        }
    }

    /**
     * Class representing a filter mode. A filter mode is like a filter applied
     * on the list tree elements. Zero or more can be active at the same time.
     *
     * The exact behavior of the filter mode is defined by the model provider
     * itself.
     */
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

    /**
     * Get the name of this model provider. This can be used for example to name
     * a corresponding view in the UI.
     *
     * @return The model provider's name
     */
    String getName();

    /**
     * Set the trace for which this model provider fetches its information.
     *
     * @param trace
     *            The source trace
     */
    void setTrace(@Nullable ITmfTrace trace);


    /**
     * Get the trace for which this model provider fetches its information.
     *
     * @return The current trace
     */
    @Nullable ITmfTrace getTrace();

    /**
     * The property representing the target trace of this model provider.
     *
     * @return The trace property
     */
    ObjectProperty<@Nullable ITmfTrace> traceProperty();

    // ------------------------------------------------------------------------
    // Render providers
    // ------------------------------------------------------------------------

    /**
     * Get a tree render corresponding to the current configuration settings.
     *
     * @return A tree render
     */
    TimeGraphTreeRender getTreeRender();

    /**
     * Get the state provider supplied by this model provider.
     *
     * @return The state provider
     */
    ITimeGraphModelStateProvider getStateProvider();

    /**
     * Get the arrow providers supplied by this model provider.
     *
     * @return The arrow providers. May be empty but should not be null.
     */
    Collection<ITimeGraphModelArrowProvider> getArrowProviders();

    // ------------------------------------------------------------------------
    // Sorting modes
    // ------------------------------------------------------------------------

    /**
     * Get a list of all the available sorting modes for this provider.
     *
     * @return The sorting modes
     */
    List<SortingMode> getSortingModes();

    /**
     * Get the current sorting mode. There should always be one and only one.
     *
     * @return The current sorting mode
     */
    SortingMode getCurrentSortingMode();

    /**
     * Change the configured sorting mode to another one.
     *
     * @param index
     *            The index of the corresponding mode in the list returned by
     *            {@link #getSortingModes()}.
     */
    void setCurrentSortingMode(int index);

    // ------------------------------------------------------------------------
    // Filter modes
    // ------------------------------------------------------------------------

    /**
     * Get a list of all the available filter modes for this provider.
     *
     * @return The list of available filter modes. It may be empty but should
     *         not be null.
     */
    List<FilterMode> getFilterModes();

    /**
     * Enable the specified filter mode.
     *
     * @param index
     *            The index of the filter mode in the list returned by
     *            {@link #getFilterModes()}.
     */
    void enableFilterMode(int index);

    /**
     * Disable the specified filter mode.
     *
     * @param index
     *            The index of the filter mode in the list returned by
     *            {@link #getFilterModes()}.
     */
    void disableFilterMode(int index);

    /**
     * Get the currently active filter modes.
     *
     * @return The active filter modes. There might be 0 or more.
     */
    Set<FilterMode> getActiveFilterModes();

}
