/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.lttng.kernel.core.views.controlflow2;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.lttng.scope.lttng.kernel.core.analysis.os.Attributes;
import org.lttng.scope.lttng.kernel.core.analysis.os.KernelAnalysisModule;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.states.ITimeGraphModelStateProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.statesystem.StateSystemModelProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeElement;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeRender;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import ca.polymtl.dorsal.libdelorean.ITmfStateSystem;
import ca.polymtl.dorsal.libdelorean.StateSystemUtils;
import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;
import ca.polymtl.dorsal.libdelorean.interval.ITmfStateInterval;

public class ControlFlowModelProvider extends StateSystemModelProvider {

    private static final Supplier<ITimeGraphModelStateProvider> STATE_PROVIDER = () -> {
        return new ControlFlowModelStateProvider();
    };

    private static final List<SortingMode> SORTING_MODES = ImmutableList.of(
            ControlFlowConfigModes.SORTING_BY_TID,
            ControlFlowConfigModes.SORTING_BY_THREAD_NAME);

    private static final List<FilterMode> FILTER_MODES = ImmutableList.of(
            ControlFlowConfigModes.FILTERING_INACTIVE_ENTRIES);

    // ------------------------------------------------------------------------
    // Tree render
    // ------------------------------------------------------------------------

    /**
     * State values that are considered inactive, for purposes of filtering out
     * when the "filter inactive entries" mode is enabled.
     */
//    private static final Set<ITmfStateValue> INACTIVE_STATE_VALUES = ImmutableSet.of(
//            TmfStateValue.nullValue(),
//            StateValues.PROCESS_STATUS_UNKNOWN_VALUE,
//            StateValues.PROCESS_STATUS_WAIT_UNKNOWN_VALUE,
//            StateValues.PROCESS_STATUS_WAIT_BLOCKED_VALUE
//            );

    /**
     * Each "Thread" attribute has the following children:
     *
     * <ul>
     * <li>Prio</li>
     * <li>System_call</li>
     * <li>Exec_name</li>
     * <li>PPID</li>
     * </ul>
     *
     * The "Thread" is considered the base quark.
     */
    private static final String[] BASE_QUARK_PATTERN = { Attributes.THREADS, "*" }; //$NON-NLS-1$

    /**
     * Get the tree element name for every thread. It consists of the TID
     * followed by the first available exec_name for this thread.
     *
     * FIXME This implies a static tree definition for every TID, which does not
     * handle TID re-use correctly. The state system structure should be updated
     * accordingly.
     */
    @VisibleForTesting
    public static final Function<TreeRenderContext, TimeGraphTreeRender> SS_TO_TREE_RENDER_FUNCTION = (treeContext) -> {
        ITmfStateSystem ss = treeContext.ss;
//        List<ITmfStateInterval> fullState = treeContext.fullQueryAtRangeStart;

        Stream<ControlFlowTreeElement> treeElems = ss.getQuarks(BASE_QUARK_PATTERN).stream()
                .map(baseQuark -> {
                    String tid = ss.getAttributeName(baseQuark);

                    String threadName;
                    try {
                        int execNameQuark = ss.getQuarkRelative(baseQuark, Attributes.EXEC_NAME);
                        // TODO We should look starting at
                        // treeContext.renderTimeRangeStart first, and if we
                        // don't find anything use ss.getStartTime(), so that we
                        // catch subsequent process name changes
                        ITmfStateInterval firstInterval = StateSystemUtils.queryUntilNonNullValue(ss,
                                execNameQuark, ss.getStartTime(), Long.MAX_VALUE);
                        if (firstInterval == null) {
                            threadName = null;
                        } else {
                            threadName = firstInterval.getStateValue().unboxStr();
                        }
                    } catch (AttributeNotFoundException e) {
                        threadName = null;
                    }

                    return new ControlFlowTreeElement(tid, threadName, Collections.emptyList(), baseQuark);
                });

        /* Run the entries through the active filter modes */
//        Set<FilterMode> filterModes = treeContext.filterModes;
//        if (filterModes.contains(ControlFlowConfigModes.FILTERING_INACTIVE_ENTRIES)) {
//            /*
//             * Filter out the tree elements whose state is considered inactive
//             * for the whole duration of the configured time range.
//             */
//            treeElems = treeElems.filter(elem -> {
//                ITmfStateInterval interval = fullState.get(elem.getSourceQuark());
//                if (interval.getEndTime() > treeContext.renderTimeRangeEnd &&
//                        INACTIVE_STATE_VALUES.contains(interval.getStateValue())) {
//                    return false;
//                }
//                return true;
//            });
//        }

        /* Sort entries according to the active sorting mode */
        SortingMode sortingMode = treeContext.sortingMode;
        if (sortingMode == ControlFlowConfigModes.SORTING_BY_TID) {
            treeElems = treeElems.sorted(Comparator.comparingInt(ControlFlowTreeElement::getTid));
        } else if (sortingMode == ControlFlowConfigModes.SORTING_BY_THREAD_NAME) {
            treeElems = treeElems.sorted((elem1, elem2) -> {
                return elem1.getThreadName().compareToIgnoreCase(elem2.getThreadName());
            });
        }

        List<TimeGraphTreeElement> treeElemsList = treeElems.collect(Collectors.toList());
        return new TimeGraphTreeRender(treeElemsList);
    };

    /**
     * Constructor
     */
    public ControlFlowModelProvider() {
        super(requireNonNull(Messages.threadsProviderName),
                SORTING_MODES,
                FILTER_MODES,
                STATE_PROVIDER.get(),
                null,
                /* Parameters specific to state system render providers */
                KernelAnalysisModule.ID,
                SS_TO_TREE_RENDER_FUNCTION);

        enableFilterMode(0);
    }
}
