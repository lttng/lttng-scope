/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.lttng.kernel.core.views.kernel.resources2;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.jdt.annotation.Nullable;
import org.lttng.scope.lttng.kernel.core.analysis.os.KernelAnalysisModule;
import org.lttng.scope.lttng.kernel.core.analysis.os.StateValues;
import org.lttng.scope.lttng.kernel.core.views.kernel.KernelAnalysisStateDefinitions;
import org.lttng.scope.tmf2.views.core.config.ConfigOption;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.statesystem.StateSystemModelStateProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.ColorDefinition;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.LineThickness;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.StateDefinition;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import ca.polymtl.dorsal.libdelorean.exceptions.StateValueTypeException;
import ca.polymtl.dorsal.libdelorean.statevalue.ITmfStateValue;

/**
 * State provider of the Resources time graph.
 *
 * @author Alexandre Montplaisir
 */
public class ResourcesModelStateProvider extends StateSystemModelStateProvider {

    // ------------------------------------------------------------------------
    // Label mapping
    // ------------------------------------------------------------------------

    private static final Function<StateIntervalContext, @Nullable String> LABEL_MAPPING_FUNCTION = ssCtx -> null;

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

    private static final Function<StateIntervalContext, StateDefinition> STATE_DEF_MAPPING_FUNCTION = ssCtx -> {
        ITmfStateValue val = ssCtx.sourceInterval.getStateValue();
        return stateValueToStateDef(val);
    };

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

    private static final Function<StateIntervalContext, String> STATE_NAME_MAPPING_FUNCTION = ssCtx -> STATE_DEF_MAPPING_FUNCTION.apply(ssCtx).getName();

    private static final Function<StateIntervalContext, ConfigOption<ColorDefinition>> COLOR_MAPPING_FUNCTION = ssCtx -> STATE_DEF_MAPPING_FUNCTION.apply(ssCtx).getColor();

    private static final Function<StateIntervalContext, ConfigOption<LineThickness>> LINE_THICKNESS_MAPPING_FUNCTION = ssCtx -> STATE_DEF_MAPPING_FUNCTION.apply(ssCtx).getLineThickness();

    // ------------------------------------------------------------------------
    // Properties
    // ------------------------------------------------------------------------

    private static final Function<StateIntervalContext, Map<String, String>> PROPERTIES_MAPPING_FUNCTION = ssCtx -> {
        // TODO Add current thread on this CPU
        return Collections.emptyMap();
    };

    /**
     * Constructor
     */
    public ResourcesModelStateProvider() {
        super(STATE_DEFINITIONS,
                KernelAnalysisModule.ID,
                STATE_NAME_MAPPING_FUNCTION,
                LABEL_MAPPING_FUNCTION,
                COLOR_MAPPING_FUNCTION,
                LINE_THICKNESS_MAPPING_FUNCTION,
                PROPERTIES_MAPPING_FUNCTION);
    }
}
