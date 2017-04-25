/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.lttng.kernel.core.views.kernel.controlflow2;

import static java.util.Objects.requireNonNull;
import static org.lttng.scope.common.core.NonNullUtils.nullToEmptyString;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.jdt.annotation.Nullable;
import org.lttng.scope.lttng.kernel.core.analysis.os.Attributes;
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
import com.google.common.collect.ImmutableMap;

import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;
import ca.polymtl.dorsal.libdelorean.exceptions.StateValueTypeException;
import ca.polymtl.dorsal.libdelorean.statevalue.ITmfStateValue;

public class ControlFlowModelStateProvider extends StateSystemModelStateProvider {

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

    private static final Function<StateIntervalContext, StateDefinition> STATE_DEF_MAPPING_FUNCTION = ssCtx -> {
        try {
            ITmfStateValue val = ssCtx.sourceInterval.getStateValue();
            if (val.isNull()) {
                return KernelAnalysisStateDefinitions.NO_STATE;
            }

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
    };

    private static final Function<StateIntervalContext, ConfigOption<ColorDefinition>> COLOR_MAPPING_FUNCTION = ssCtx -> STATE_DEF_MAPPING_FUNCTION.apply(ssCtx).getColor();

    private static final Function<StateIntervalContext, ConfigOption<LineThickness>> LINE_THICKNESS_MAPPING_FUNCTION = ssCtx -> STATE_DEF_MAPPING_FUNCTION.apply(ssCtx).getLineThickness();

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
    public ControlFlowModelStateProvider() {
        super(STATE_DEFINITIONS,
                KernelAnalysisModule.ID,
                STATE_NAME_MAPPING_FUNCTION,
                LABEL_MAPPING_FUNCTION,
                COLOR_MAPPING_FUNCTION,
                LINE_THICKNESS_MAPPING_FUNCTION,
                PROPERTIES_MAPPING_FUNCTION);
    }
}
