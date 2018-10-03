/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.kernel.views.timegraph.resources;

import ca.polymtl.dorsal.libdelorean.IStateSystemQuarkResolver;
import ca.polymtl.dorsal.libdelorean.interval.StateInterval;
import ca.polymtl.dorsal.libdelorean.statevalue.IntegerStateValue;
import ca.polymtl.dorsal.libdelorean.statevalue.StateValue;
import com.efficios.jabberwocky.lttng.kernel.analysis.os.KernelAnalysis;
import com.efficios.jabberwocky.lttng.kernel.analysis.os.StateValues;
import com.efficios.jabberwocky.lttng.kernel.views.timegraph.KernelAnalysisStateDefinitions;
import com.efficios.jabberwocky.views.timegraph.model.provider.statesystem.StateSystemModelStateProvider;
import com.efficios.jabberwocky.views.timegraph.model.provider.statesystem.StateSystemTimeGraphTreeElement;
import com.efficios.jabberwocky.views.timegraph.model.render.StateDefinition;
import com.efficios.jabberwocky.views.timegraph.model.render.states.BasicTimeGraphStateInterval;
import com.efficios.jabberwocky.views.timegraph.model.render.states.TimeGraphStateInterval;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    static final StateDefinition stateValueToStateDef(StateValue val) {
        if (val.isNull()) {
            return KernelAnalysisStateDefinitions.NO_STATE;
        }

        if (!(val instanceof IntegerStateValue)) {
            return KernelAnalysisStateDefinitions.CPU_STATE_UNKNOWN;
        }

        int status = ((IntegerStateValue) val).getValue();
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
    }

    /**
     * Constructor
     */
    public ResourcesModelStateProvider() {
        super(STATE_DEFINITIONS, KernelAnalysis.INSTANCE);
    }

    @Override
    protected Set<Integer> supplyExtraQuarks(IStateSystemQuarkResolver ss,
                                             long ts,
                                             StateInterval stateInterval) {
        /* No extra data for now */
        return Collections.emptySet();
    }

    @Override
    protected TimeGraphStateInterval createInterval(IStateSystemQuarkResolver ss,
                                                    Map<Integer, ? extends StateInterval> ssQueryResult,
                                                    StateSystemTimeGraphTreeElement treeElem,
                                                    StateInterval interval) {

        StateDefinition stateDef = stateValueToStateDef(interval.getStateValue());

        return new BasicTimeGraphStateInterval(
                interval.getStart(),
                interval.getEnd(),
                treeElem,
                stateDef,
                // Label, none for now TODO
                null,
                // Properties
                // TODO Add current thread on this CPU
                Collections.emptyMap());
    }
}
