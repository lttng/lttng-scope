/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.lttng.kernel.core.views.timegraph.resources;

import java.util.Collections;
import java.util.List;

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

import ca.polymtl.dorsal.libdelorean.ITmfStateSystem;
import ca.polymtl.dorsal.libdelorean.exceptions.StateValueTypeException;
import ca.polymtl.dorsal.libdelorean.interval.ITmfStateInterval;
import ca.polymtl.dorsal.libdelorean.statevalue.ITmfStateValue;

/**
 * State provider of the Resources time graph.
 *
 * @author Alexandre Montplaisir
 */
public class ResourcesModelStateProvider extends StateSystemModelStateProvider {

    // ------------------------------------------------------------------------
    // Color mapping, line thickness
    // ------------------------------------------------------------------------

    /**
     * State definitions used in this provider.
     */
    private static final List<StateDefinition> STATE_DEFINITIONS = ImmutableList.of(
            KernelAnalysisStateDefinitions.CPU_STATE_IDLE,
            KernelAnalysisStateDefinitions.THREAD_STATE_SYSCALL,
            KernelAnalysisStateDefinitions.THREAD_STATE_USERMODE,
            KernelAnalysisStateDefinitions.CPU_STATE_IRQ_ACTIVE,
            KernelAnalysisStateDefinitions.CPU_STATE_SOFTIRQ_ACTIVE,
            KernelAnalysisStateDefinitions.CPU_STATE_SOFTIRQ_RAISED);

    @VisibleForTesting
    static final StateDefinition stateValueToStateDef(ITmfStateValue val) {
        if (val.isNull()) {
            return KernelAnalysisStateDefinitions.NO_STATE;
        }

        try {
            int status = val.unboxInt();
            switch (status) {
            case StateValues.CPU_STATUS_IDLE:
                return KernelAnalysisStateDefinitions.CPU_STATE_IDLE;
            case StateValues.CPU_STATUS_RUN_SYSCALL:
                return KernelAnalysisStateDefinitions.THREAD_STATE_SYSCALL;
            case StateValues.CPU_STATUS_RUN_USERMODE:
                return KernelAnalysisStateDefinitions.THREAD_STATE_USERMODE;
            case StateValues.CPU_STATUS_IRQ:
                return KernelAnalysisStateDefinitions.CPU_STATE_IRQ_ACTIVE;
            case StateValues.CPU_STATUS_SOFTIRQ:
                return KernelAnalysisStateDefinitions.CPU_STATE_SOFTIRQ_ACTIVE;
            case StateValues.CPU_STATUS_SOFT_IRQ_RAISED:
                return KernelAnalysisStateDefinitions.CPU_STATE_SOFTIRQ_RAISED;
            default:
                return KernelAnalysisStateDefinitions.CPU_STATE_UNKNOWN;
            }

        } catch (StateValueTypeException e) {
            return KernelAnalysisStateDefinitions.CPU_STATE_UNKNOWN;
        }
    }

    /**
     * Constructor
     */
    public ResourcesModelStateProvider() {
        super(STATE_DEFINITIONS, KernelAnalysisModule.ID);
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
                // Label, none for now TODO
                null,
                // Properties
                // TODO Add current thread on this CPU
                Collections.emptyMap());
    }
}
