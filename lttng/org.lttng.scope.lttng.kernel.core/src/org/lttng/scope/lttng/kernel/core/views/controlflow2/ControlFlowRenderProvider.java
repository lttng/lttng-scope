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
import static org.lttng.scope.common.core.NonNullUtils.nullToEmptyString;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.Nullable;
import org.lttng.scope.lttng.kernel.core.analysis.os.Attributes;
import org.lttng.scope.lttng.kernel.core.analysis.os.KernelAnalysisModule;
import org.lttng.scope.lttng.kernel.core.analysis.os.StateValues;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.statesystem.StateSystemModelRenderProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.ColorDefinition;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateInterval.LineThickness;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeElement;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeRender;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import ca.polymtl.dorsal.libdelorean.ITmfStateSystem;
import ca.polymtl.dorsal.libdelorean.StateSystemUtils;
import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;
import ca.polymtl.dorsal.libdelorean.exceptions.StateValueTypeException;
import ca.polymtl.dorsal.libdelorean.interval.ITmfStateInterval;
import ca.polymtl.dorsal.libdelorean.statevalue.ITmfStateValue;

public class ControlFlowRenderProvider extends StateSystemModelRenderProvider {

    // ------------------------------------------------------------------------
    // Tree render
    // ------------------------------------------------------------------------

    private static final List<SortingMode> SORTING_MODES = ImmutableList.of(
            ControlFlowConfigModes.SORTING_BY_TID,
            ControlFlowConfigModes.SORTING_BY_THREAD_NAME);

    private static final List<FilterMode> FILTER_MODES = ImmutableList.of(
            ControlFlowConfigModes.FILTERING_INACTIVE_ENTRIES);

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

    // ------------------------------------------------------------------------
    // State name mapping
    // ------------------------------------------------------------------------

    /**
     * Function mapping state names
     *
     * @param value
     *            State value representing the state
     * @return The state name to display, should be localized
     */
    @VisibleForTesting
    public static String mapStateValueToStateName(int value) {
        try {
            switch (value) {
            case StateValues.PROCESS_STATUS_WAIT_UNKNOWN:
                return nullToEmptyString(Messages.ControlFlowRenderProvider_State_WaitUnknown);
            case StateValues.PROCESS_STATUS_WAIT_BLOCKED:
                return nullToEmptyString(Messages.ControlFlowRenderProvider_State_WaitBlocked);
            case StateValues.PROCESS_STATUS_WAIT_FOR_CPU:
                return nullToEmptyString(Messages.ControlFlowRenderProvider_State_WaitForCpu);
            case StateValues.PROCESS_STATUS_RUN_USERMODE:
                return nullToEmptyString(Messages.ControlFlowRenderProvider_State_UserMode);
            case StateValues.PROCESS_STATUS_RUN_SYSCALL:
                return nullToEmptyString(Messages.ControlFlowRenderProvider_State_Syscall);
            case StateValues.PROCESS_STATUS_INTERRUPTED:
                return nullToEmptyString(Messages.ControlFlowRenderProvider_State_Interrupted);
            default:
                return nullToEmptyString(Messages.ControlFlowRenderProvider_State_Unknown);
            }

        } catch (StateValueTypeException e) {
            return nullToEmptyString(Messages.ControlFlowRenderProvider_State_Unknown);
        }
    }

    private static final Function<StateIntervalContext, String> STATE_NAME_MAPPING_FUNCTION = ssCtx -> {
        int statusQuark = ssCtx.baseTreeElement.getSourceQuark();
        ITmfStateValue val = ssCtx.fullQueryAtIntervalStart.get(statusQuark).getStateValue();
        int status = val.unboxInt();
        return mapStateValueToStateName(status);
    };

    // ------------------------------------------------------------------------
    // Label mapping
    // ------------------------------------------------------------------------

    /** Prefixes to strip from syscall names in the labels */
    // TODO This should be inferred from the kernel event layout
    private static final Collection<String> SYSCALL_PREFIXES = Arrays.asList("sys_", "syscall_entry_"); //$NON-NLS-1$ //$NON-NLS-2$

    private static final Function<StateIntervalContext, @Nullable String> LABEL_MAPPING_FUNCTION = ssCtx -> {
        int statusQuark = ssCtx.baseTreeElement.getSourceQuark();
        ITmfStateValue val = ssCtx.fullQueryAtIntervalStart.get(statusQuark).getStateValue();

        /* If the status is "syscall", use the name of the syscall as label */
        if (!val.equals(StateValues.PROCESS_STATUS_RUN_SYSCALL_VALUE)) {
            return null;
        }

        String syscallName;
        try {
            int syscallQuark = ssCtx.ss.getQuarkRelative(statusQuark, Attributes.SYSTEM_CALL);
            syscallName = ssCtx.fullQueryAtIntervalStart.get(syscallQuark).getStateValue().unboxStr();
        } catch (AttributeNotFoundException | StateValueTypeException e) {
            return null;
        }

        /*
         * Strip the "syscall" prefix part if there is one, it's not useful in
         * the label.
         */
        for (String sysPrefix : SYSCALL_PREFIXES) {
            if (syscallName.startsWith(sysPrefix)) {
                syscallName = syscallName.substring(sysPrefix.length());
            }
        }

        return syscallName;
    };

    // ------------------------------------------------------------------------
    // Color mapping
    // ------------------------------------------------------------------------

    private static final ColorDefinition NO_COLOR           = new ColorDefinition(  0,   0,   0,  0);
    private static final ColorDefinition COLOR_UNKNOWN      = new ColorDefinition(100, 100, 100);
    private static final ColorDefinition COLOR_WAIT_UNKNOWN = new ColorDefinition(200, 200, 200);
    private static final ColorDefinition COLOR_WAIT_BLOCKED = new ColorDefinition(200, 200,   0);
    private static final ColorDefinition COLOR_WAIT_FOR_CPU = new ColorDefinition(200, 100,   0);
    private static final ColorDefinition COLOR_USERMODE     = new ColorDefinition(  0, 200,   0);
    private static final ColorDefinition COLOR_SYSCALL      = new ColorDefinition(  0,   0, 200);
    private static final ColorDefinition COLOR_INTERRUPTED  = new ColorDefinition(200,   0, 100);

    private static final Function<StateIntervalContext, ColorDefinition> COLOR_MAPPING_FUNCTION = ssCtx -> {
        try {
            int statusQuark = ssCtx.baseTreeElement.getSourceQuark();
            ITmfStateValue val = ssCtx.fullQueryAtIntervalStart.get(statusQuark).getStateValue();

            if (val.isNull()) {
                return NO_COLOR;
            }

            int status = val.unboxInt();
            switch (status) {
            case StateValues.PROCESS_STATUS_WAIT_UNKNOWN:
                return COLOR_WAIT_UNKNOWN;
            case StateValues.PROCESS_STATUS_WAIT_BLOCKED:
                return COLOR_WAIT_BLOCKED;
            case StateValues.PROCESS_STATUS_WAIT_FOR_CPU:
                return COLOR_WAIT_FOR_CPU;
            case StateValues.PROCESS_STATUS_RUN_USERMODE:
                return COLOR_USERMODE;
            case StateValues.PROCESS_STATUS_RUN_SYSCALL:
                return COLOR_SYSCALL;
            case StateValues.PROCESS_STATUS_INTERRUPTED:
                return COLOR_INTERRUPTED;
            default:
                return COLOR_UNKNOWN;
            }

        } catch (StateValueTypeException e) {
            return COLOR_UNKNOWN;
        }
    };

    // ------------------------------------------------------------------------
    // Line thickness
    // ------------------------------------------------------------------------

    /* No variation for now */
    private static final Function<StateIntervalContext, LineThickness> LINE_THICKNESS_MAPPING_FUNCTION = ssCtx -> {
//        int statusQuark = ssCtx.baseTreeElement.getSourceQuark();
//        ITmfStateValue val = ssCtx.fullQueryAtIntervalStart.get(statusQuark).getStateValue();
//
//        // For demo purposes only!
//        if (val.equals(StateValues.PROCESS_STATUS_RUN_SYSCALL_VALUE)) {
//            return LineThickness.SMALL;
//        }

        return LineThickness.NORMAL;
    };

    // ------------------------------------------------------------------------
    // Properties
    // ------------------------------------------------------------------------

    private static final Function<StateIntervalContext, Map<String, String>> PROPERTIES_MAPPING_FUNCTION = ssCtx -> {
        /* Include properties for CPU and syscall name. */
        int baseQuark = ssCtx.baseTreeElement.getSourceQuark();

        String cpu;
        try {
            int cpuQuark = ssCtx.ss.getQuarkRelative(baseQuark, Attributes.CURRENT_CPU_RQ);
            ITmfStateValue sv = ssCtx.fullQueryAtIntervalStart.get(cpuQuark).getStateValue();
            cpu = (sv.isNull() ? requireNonNull(Messages.propertyNotAvailable) : String.valueOf(sv.unboxInt()));
        } catch (AttributeNotFoundException e) {
            cpu = requireNonNull(Messages.propertyNotAvailable);
        }

        String syscall;
        try {
            int syscallNameQuark = ssCtx.ss.getQuarkRelative(baseQuark, Attributes.SYSTEM_CALL);
            ITmfStateValue sv = ssCtx.fullQueryAtIntervalStart.get(syscallNameQuark).getStateValue();
            syscall = (sv.isNull() ? requireNonNull(Messages.propertyNotAvailable) : sv.unboxStr());
        } catch (AttributeNotFoundException e) {
            syscall = requireNonNull(Messages.propertyNotAvailable);
        }

        return ImmutableMap.of(requireNonNull(Messages.propertyNameCpu), cpu,
                requireNonNull(Messages.propertyNameSyscall), syscall);
    };

    /**
     * Constructor
     */
    public ControlFlowRenderProvider() {
        super(SORTING_MODES,
                FILTER_MODES,
                /* Parameters specific to state system render providers */
                KernelAnalysisModule.ID,
                SS_TO_TREE_RENDER_FUNCTION,
                STATE_NAME_MAPPING_FUNCTION,
                LABEL_MAPPING_FUNCTION,
                COLOR_MAPPING_FUNCTION,
                LINE_THICKNESS_MAPPING_FUNCTION,
                PROPERTIES_MAPPING_FUNCTION);

        enableFilterMode(0);
    }
}
