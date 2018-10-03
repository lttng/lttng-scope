/*
 * Copyright (C) 2016-2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.timegraph.model.provider

import com.efficios.jabberwocky.project.TraceProject
import com.efficios.jabberwocky.views.timegraph.model.provider.arrows.TimeGraphModelArrowProvider
import com.efficios.jabberwocky.views.timegraph.model.provider.states.TimeGraphModelStateProvider
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeRender
import javafx.beans.property.ObjectProperty

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
 */
interface ITimeGraphModelProvider {

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
    class SortingMode(val name: String)

    /**
     * Class representing a filter mode. A filter mode is like a filter applied
     * on the list tree elements. Zero or more can be active at the same time.
     *
     * The exact behavior of the filter mode is defined by the model provider
     * itself.
     */
    class FilterMode(val name: String)

    // ------------------------------------------------------------------------
    // General methods
    // ------------------------------------------------------------------------

    /**
     * The name of this model provider. This can be used for example to name
     * a corresponding view in the UI.
     */
    val name: String

    /**
     * The trace for which this model provider fetches its information.
     */
    fun traceProjectProperty(): ObjectProperty<TraceProject<*, *>?>
    var traceProject: TraceProject<*, *>?

    // ------------------------------------------------------------------------
    // Render providers
    // ------------------------------------------------------------------------

    /**
     * Get a tree render corresponding to the current configuration settings.
     *
     * @return A tree render
     */
    fun getTreeRender(): TimeGraphTreeRender

    /**
     * The state provider supplied by this model provider.
     */
    val stateProvider: TimeGraphModelStateProvider

    /**
     * Get the arrow providers supplied by this model provider.
     */
    val arrowProviders: Collection<TimeGraphModelArrowProvider>

    // ------------------------------------------------------------------------
    // Sorting modes
    // ------------------------------------------------------------------------

    /**
     * Get a list of all the available sorting modes for this provider.
     */
    val sortingModes: List<SortingMode>

    /**
     * Get the current sorting mode. There should always be one and only one.
     *
     * @return The current sorting mode
     */
    fun getCurrentSortingMode(): SortingMode

    /**
     * Change the configured sorting mode to another one.
     *
     * @param index
     *            The index of the corresponding mode in the list returned by
     *            {@link #getSortingModes()}.
     */
    fun setCurrentSortingMode(index: Int)

    // ------------------------------------------------------------------------
    // Filter modes
    // ------------------------------------------------------------------------

    /**
     * List of all the available filter modes for this provider.
     */
    val filterModes: List<FilterMode>

    /**
     * Enable the specified filter mode.
     *
     * @param index
     *            The index of the filter mode in the list returned by
     *            {@link #getFilterModes()}.
     */
    fun enableFilterMode(index: Int)

    /**
     * Disable the specified filter mode.
     *
     * @param index
     *            The index of the filter mode in the list returned by
     *            {@link #getFilterModes()}.
     */
    fun disableFilterMode(index: Int)

    /**
     * Get the currently active filter modes.
     *
     * @return The active filter modes. There might be 0 or more.
     */
    fun getActiveFilterModes(): Set<FilterMode>

}
