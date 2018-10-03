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
import com.google.common.collect.ImmutableSet
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty

/**
 * Base implementation of {@link ITimeGraphModelProvider}.
 */
abstract class TimeGraphModelProvider(override val name: String,
                                      sortingModes: List<ITimeGraphModelProvider.SortingMode>?,
                                      filterModes: List<ITimeGraphModelProvider.FilterMode>?,
                                      final override val stateProvider: TimeGraphModelStateProvider,
                                      arrowProviders: List<TimeGraphModelArrowProvider>?) : ITimeGraphModelProvider {

    companion object {
        /** A "default" sorting mode, for use when only one is needed. */
        protected val DEFAULT_SORTING_MODE = ITimeGraphModelProvider.SortingMode("Default")
    }

    private val traceProjectProperty: ObjectProperty<TraceProject<*, *>?> = SimpleObjectProperty()
    override fun traceProjectProperty() = traceProjectProperty
    override var traceProject
        get() = traceProjectProperty.get()
        set(value) = traceProjectProperty.set(value)

    final override val sortingModes = if (sortingModes == null || sortingModes.isEmpty()) listOf(DEFAULT_SORTING_MODE) else sortingModes
    /* No filter modes active by default */
    final override val filterModes = filterModes ?: emptyList()
    final override val arrowProviders = arrowProviders ?: emptyList()

    private var currentSortingModeVar = this.sortingModes[0]
    private var activeFilterModesVar = mutableSetOf<ITimeGraphModelProvider.FilterMode>()

    init {
        stateProvider.traceProjectProperty().bind(traceProjectProperty)
        this.arrowProviders.forEach { it.traceProjectProperty().bind(traceProjectProperty) }
    }

    // ------------------------------------------------------------------------
    // Render generation methods. Implementation left to subclasses.
    // ------------------------------------------------------------------------

    abstract override fun getTreeRender(): TimeGraphTreeRender

    // ------------------------------------------------------------------------
    // Sorting modes
    // ------------------------------------------------------------------------

    final override fun getCurrentSortingMode(): ITimeGraphModelProvider.SortingMode = currentSortingModeVar

    final override fun setCurrentSortingMode(index: Int) {
        currentSortingModeVar = sortingModes[index]
    }

    // ------------------------------------------------------------------------
    // Filter modes
    // ------------------------------------------------------------------------

    final override fun enableFilterMode(index: Int) {
        activeFilterModesVar.add(filterModes[index])
    }

    final override fun disableFilterMode(index: Int) {
        activeFilterModesVar.remove(filterModes[index])
    }

    final override fun getActiveFilterModes(): Set<ITimeGraphModelProvider.FilterMode> {
        return ImmutableSet.copyOf(activeFilterModesVar)
    }

}
