/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.lttng.kernel.core.views.timegraph.threads;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.lttng.scope.lttng.kernel.core.analysis.os.Attributes;
import org.lttng.scope.lttng.kernel.core.analysis.os.KernelAnalysisModule;
import org.lttng.scope.lttng.kernel.core.analysis.os.StateValues;
import org.lttng.scope.lttng.kernel.core.views.timegraph.KernelAnalysisStateDefinitions;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.statesystem.StateSystemModelStateProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.statesystem.StateSystemTimeGraphTreeElement;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.StateDefinition;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.BasicTimeGraphStateInterval;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateInterval;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import ca.polymtl.dorsal.libdelorean.ITmfStateSystem;
import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;
import ca.polymtl.dorsal.libdelorean.exceptions.StateValueTypeException;
import ca.polymtl.dorsal.libdelorean.interval.ITmfStateInterval;
import ca.polymtl.dorsal.libdelorean.statevalue.ITmfStateValue;

public class ThreadsModelStateProvider extends StateSystemModelStateProvider {

    // ------------------------------------------------------------------------
    // Label mapping
    // ------------------------------------------------------------------------

    /** Prefixes to strip from syscall names in the labels */
    // TODO This should be inferred from the kernel event layout
    private static final Collection<String> SYSCALL_PREFIXES = Arrays.asList("sys_", "syscall_entry_"); //$NON-NLS-1$ //$NON-NLS-2$

    private static @Nullable String getStateLabel(ITmfStateSystem ss, int sourceQuark, ITmfStateInterval interval) {
        int statusQuark = sourceQuark;
        long startTime = interval.getStartTime();
        ITmfStateValue val = interval.getStateValue();

        /* If the status is "syscall", use the name of the syscall as label */
        if (!val.equals(StateValues.PROCESS_STATUS_RUN_SYSCALL_VALUE)) {
            return null;
        }

        String syscallName;
        try {
            int syscallQuark = ss.getQuarkRelative(statusQuark, Attributes.SYSTEM_CALL);
            syscallName = ss.querySingleState(startTime, syscallQuark).getStateValue().unboxStr();
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
    }

    // ------------------------------------------------------------------------
    // Color mapping, line thickness
    // ------------------------------------------------------------------------

    /**
     * State definitions used in this provider.
     */
    private static final List<StateDefinition> STATE_DEFINITIONS = ImmutableList.of(
            KernelAnalysisStateDefinitions.THREAD_STATE_UNKNOWN,
            KernelAnalysisStateDefinitions.THREAD_STATE_WAIT_UNKNOWN,
            KernelAnalysisStateDefinitions.THREAD_STATE_WAIT_BLOCKED,
            KernelAnalysisStateDefinitions.THREAD_STATE_WAIT_FOR_CPU,
            KernelAnalysisStateDefinitions.THREAD_STATE_USERMODE,
            KernelAnalysisStateDefinitions.THREAD_STATE_SYSCALL,
            KernelAnalysisStateDefinitions.THREAD_STATE_INTERRUPTED);

    @VisibleForTesting
    static final StateDefinition stateValueToStateDef(ITmfStateValue val) {
        if (val.isNull()) {
            return KernelAnalysisStateDefinitions.NO_STATE;
        }

        try {
            int status = val.unboxInt();
            switch (status) {
            case StateValues.PROCESS_STATUS_WAIT_UNKNOWN:
                return KernelAnalysisStateDefinitions.THREAD_STATE_WAIT_UNKNOWN;
            case StateValues.PROCESS_STATUS_WAIT_BLOCKED:
                return KernelAnalysisStateDefinitions.THREAD_STATE_WAIT_BLOCKED;
            case StateValues.PROCESS_STATUS_WAIT_FOR_CPU:
                return KernelAnalysisStateDefinitions.THREAD_STATE_WAIT_FOR_CPU;
            case StateValues.PROCESS_STATUS_RUN_USERMODE:
                return KernelAnalysisStateDefinitions.THREAD_STATE_USERMODE;
            case StateValues.PROCESS_STATUS_RUN_SYSCALL:
                return KernelAnalysisStateDefinitions.THREAD_STATE_SYSCALL;
            case StateValues.PROCESS_STATUS_INTERRUPTED:
                return KernelAnalysisStateDefinitions.THREAD_STATE_INTERRUPTED;
            default:
                return KernelAnalysisStateDefinitions.THREAD_STATE_UNKNOWN;
            }

        } catch (StateValueTypeException e) {
            return KernelAnalysisStateDefinitions.THREAD_STATE_UNKNOWN;
        }
    }

    // ------------------------------------------------------------------------
    // Properties
    // ------------------------------------------------------------------------

    private static Map<String, String> getStateProperties(ITmfStateSystem ss, int sourceQuark, ITmfStateInterval interval) {
        /* Include properties for CPU and syscall name. */
        int baseQuark = sourceQuark;
        long startTime = interval.getStartTime();

        String cpu;
        try {
            int cpuQuark = ss.getQuarkRelative(baseQuark, Attributes.CURRENT_CPU_RQ);
            ITmfStateValue sv = ss.querySingleState(startTime, cpuQuark).getStateValue();
            cpu = (sv.isNull() ? requireNonNull(Messages.propertyNotAvailable) : String.valueOf(sv.unboxInt()));
        } catch (AttributeNotFoundException e) {
            cpu = requireNonNull(Messages.propertyNotAvailable);
        }

        String syscall;
        try {
            int syscallNameQuark = ss.getQuarkRelative(baseQuark, Attributes.SYSTEM_CALL);
            ITmfStateValue sv = ss.querySingleState(startTime, syscallNameQuark).getStateValue();
            syscall = (sv.isNull() ? requireNonNull(Messages.propertyNotAvailable) : sv.unboxStr());
        } catch (AttributeNotFoundException e) {
            syscall = requireNonNull(Messages.propertyNotAvailable);
        }

        return ImmutableMap.of(requireNonNull(Messages.propertyNameCpu), cpu,
                requireNonNull(Messages.propertyNameSyscall), syscall);
    }

    /**
     * Constructor
     */
    public ThreadsModelStateProvider() {
        super(STATE_DEFINITIONS,
                KernelAnalysisModule.ID);
    }

    @Override
    protected TimeGraphStateInterval createInterval(ITmfStateSystem ss,
            StateSystemTimeGraphTreeElement treeElem, ITmfStateInterval interval) {

        StateDefinition stateDef = stateValueToStateDef(interval.getStateValue());

        return new BasicTimeGraphStateInterval(
                interval.getStartTime(),
                interval.getEndTime(),
                treeElem,
                stateDef,
                getStateLabel(ss, treeElem.getSourceQuark(), interval),
                getStateProperties(ss, treeElem.getSourceQuark(), interval));
    }
}
