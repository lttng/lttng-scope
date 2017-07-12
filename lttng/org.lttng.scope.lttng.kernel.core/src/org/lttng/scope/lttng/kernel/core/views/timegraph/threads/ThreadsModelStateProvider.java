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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.Nullable;
import org.lttng.scope.lttng.kernel.core.analysis.os.Attributes;
import org.lttng.scope.lttng.kernel.core.analysis.os.KernelAnalysis;
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

/**
 * State provider for the "Threads" timegraph model.
 *
 * @author Alexandre Montplaisir
 */
public class ThreadsModelStateProvider extends StateSystemModelStateProvider {

    /** Prefixes to strip from syscall names in the labels */
    // TODO This should be inferred from the kernel event layout
    private static final Collection<String> SYSCALL_PREFIXES = Arrays.asList("sys_", "syscall_entry_"); //$NON-NLS-1$ //$NON-NLS-2$

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

    /**
     * Constructor
     */
    public ThreadsModelStateProvider() {
        super(STATE_DEFINITIONS, KernelAnalysis.instance());
    }

    @Override
    protected TimeGraphStateInterval createInterval(ITmfStateSystem ss,
            StateSystemTimeGraphTreeElement treeElem, ITmfStateInterval interval) {

        int statusQuark = treeElem.getSourceQuark();
        long startTime = interval.getStartTime();
        ITmfStateValue val = interval.getStateValue();

        StateDefinition stateDef = stateValueToStateDef(val);

        /*
         * Prepare the quarks we will need for the additional state system query. We
         * want to add the system call name (if applicable) and the thread's current
         * CPU.
         */
        @Nullable Integer syscallNameQuark;
        if (val.equals(StateValues.PROCESS_STATUS_RUN_SYSCALL_VALUE)) {
            try {
                syscallNameQuark = ss.getQuarkRelative(statusQuark, Attributes.SYSTEM_CALL);
            } catch (AttributeNotFoundException e) {
                syscallNameQuark = null;
            }
        } else {
            syscallNameQuark = null;
        }

        @Nullable Integer cpuQuark;
        try {
            cpuQuark = ss.getQuarkRelative(statusQuark, Attributes.CURRENT_CPU_RQ);
        } catch (AttributeNotFoundException e) {
            cpuQuark = null;
        }

        /* Do the query to the state system. */
        Set<Integer> quarks = Stream.of(syscallNameQuark, cpuQuark)
                .filter(Objects::nonNull).map(Objects::requireNonNull)
                .collect(Collectors.toSet());
        Map<Integer, ITmfStateInterval> results = (quarks.isEmpty() ? Collections.emptyMap() : ss.queryStates(startTime, quarks));

        /* Assign the results */
        String syscallName;
        if (syscallNameQuark == null) {
            syscallName = null;
        } else {
            syscallName = requireNonNull(results.get(syscallNameQuark)).getStateValue().unboxStr();
            /*
             * Strip the "syscall" prefix part if there is one, it's not useful in the
             * label.
             */
            for (String sysPrefix : SYSCALL_PREFIXES) {
                if (syscallName.startsWith(sysPrefix)) {
                    syscallName = syscallName.substring(sysPrefix.length());
                }
            }
        }

        String cpuProperty;
        if (cpuQuark == null) {
            cpuProperty = requireNonNull(Messages.propertyNotAvailable);
        } else {
          ITmfStateValue sv = requireNonNull(results.get(cpuQuark)).getStateValue();
          cpuProperty = (sv.isNull() ? requireNonNull(Messages.propertyNotAvailable) : String.valueOf(sv.unboxInt()));
        }

        /*
         * If there is no syscall name, use the "Not available" message in the property
         * (but not in the label!)
         */
        String syscallProperty = (syscallName == null ? requireNonNull(Messages.propertyNotAvailable) : syscallName);

        Map<String, String> properties = ImmutableMap.of(
                requireNonNull(Messages.propertyNameCpu), cpuProperty,
                requireNonNull(Messages.propertyNameSyscall), syscallProperty);

        return new BasicTimeGraphStateInterval(
                interval.getStartTime(),
                interval.getEndTime(),
                treeElem,
                stateDef,
                syscallName,
                properties);
    }
}
