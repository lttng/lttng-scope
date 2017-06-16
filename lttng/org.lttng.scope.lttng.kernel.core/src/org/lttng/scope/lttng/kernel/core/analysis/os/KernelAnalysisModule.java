/*******************************************************************************
 * Copyright (c) 2013, 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *   Mathieu Rail - Provide the requirements of the analysis
 *******************************************************************************/

package org.lttng.scope.lttng.kernel.core.analysis.os;

import static java.util.Objects.requireNonNull;
import static org.lttng.scope.common.core.NonNullUtils.nullToEmptyString;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.lttng.scope.lttng.kernel.core.analysis.os.internal.KernelStateProvider;
import org.lttng.scope.lttng.kernel.core.trace.IKernelTrace;
import org.lttng.scope.lttng.kernel.core.trace.layout.ILttngKernelEventLayout;
import org.lttng.scope.lttng.kernel.core.trace.layout.internal.LttngEventLayout;

import com.google.common.primitives.Ints;

import ca.polymtl.dorsal.libdelorean.ITmfStateSystemBuilder;
import ca.polymtl.dorsal.libdelorean.aggregation.AttributePriorityAggregationRule;
import ca.polymtl.dorsal.libdelorean.aggregation.IStateAggregationRule;
import ca.polymtl.dorsal.libdelorean.statevalue.TmfStateValue;

/**
 * State System Module for lttng kernel traces
 *
 * @author Geneviève Bastien
 */
public class KernelAnalysisModule extends TmfStateSystemAnalysisModule {

    /** The ID of this analysis module */
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.kernel"; //$NON-NLS-1$

    @Override
    protected @NonNull ITmfStateProvider createStateProvider() {
        ITmfTrace trace = requireNonNull(getTrace());
        ILttngKernelEventLayout layout;

        if (trace instanceof IKernelTrace) {
            layout = ((IKernelTrace) trace).getKernelEventLayout();
        } else {
            /* Fall-back to the base LttngEventLayout */
            layout = LttngEventLayout.getInstance();
        }

        return new KernelStateProvider(trace, layout);
    }

    @Override
    protected String getFullHelpText() {
        return nullToEmptyString(Messages.LttngKernelAnalysisModule_Help);
    }

    @Override
    protected void setupAggregationRules(ITmfStateSystemBuilder ss) {
        /* Set up the virtual "IRQs" and "SoftIRQs" sub-trees */
        final int cpusQuark = ss.getQuarkAbsoluteAndAdd(Attributes.CPUS);
        final List<Integer> cpuQuarks = ss.getSubAttributes(cpusQuark, false);

        Set<Integer> irqNumbers = cpuQuarks.stream()
            .flatMap(quark -> {
                int irqsQuark = ss.getQuarkRelative(quark, Attributes.IRQS);
                List<Integer> irqQuarks = ss.getSubAttributes(irqsQuark, false);
                return irqQuarks.stream()
                    .map(irqQuark -> ss.getAttributeName(irqQuark))
                    .map(name -> Ints.tryParse(name))
                    .filter(Objects::nonNull);
            })
            .collect(Collectors.toSet());

        Set<Integer> softIrqNumbers = cpuQuarks.stream()
                .flatMap(quark -> {
                    int irqsQuark = ss.getQuarkRelative(quark, Attributes.SOFT_IRQS);
                    List<Integer> irqQuarks = ss.getSubAttributes(irqsQuark, false);
                    return irqQuarks.stream()
                        .map(irqQuark -> ss.getAttributeName(irqQuark))
                        .map(name -> Ints.tryParse(name))
                        .filter(Objects::nonNull);
                })
                .collect(Collectors.toSet());

        int irqsQuark = ss.getQuarkAbsoluteAndAdd(Attributes.IRQS);
        if (irqsQuark == ss.getNbAttributes()) {
            /*
             * FIXME If we just created this attribute, make sure we put a null value into
             * it so that upcoming queries return something. Should be fixed in the state
             * system library.
             */
            ss.modifyAttribute(ss.getStartTime(), TmfStateValue.nullValue(), irqsQuark);
        }
        for (int irqNumber : irqNumbers) {
            int irqQuark = ss.getQuarkRelativeAndAdd(irqsQuark, String.valueOf(irqNumber));
            List<String[]> irqPaths = cpuQuarks.stream()
                .map(quark -> {
                    String[] cpuQuarkPath = ss.getFullAttributePathArray(quark);
                    String[] irqAttributePath = new String[4];
                    irqAttributePath[0] = cpuQuarkPath[0];
                    irqAttributePath[1] = cpuQuarkPath[1];
                    irqAttributePath[2] = Attributes.IRQS;
                    irqAttributePath[3] = String.valueOf(irqNumber);
                    return irqAttributePath;
                })
                .collect(Collectors.toList());

            IStateAggregationRule rule = new AttributePriorityAggregationRule(ss, irqQuark, irqPaths);
            ss.addAggregationRule(rule);
        }

        int softIrqsQuark = ss.getQuarkAbsoluteAndAdd(Attributes.SOFT_IRQS);
        if (softIrqsQuark == ss.getNbAttributes()) {
            /*
             * FIXME If we just created this attribute, make sure we put a null value into
             * it so that upcoming queries return something. Should be fixed in the state
             * system library.
             */
            ss.modifyAttribute(ss.getStartTime(), TmfStateValue.nullValue(), softIrqsQuark);
        }
        for (int softIrqNumber : softIrqNumbers) {
            int softIrqQuark = ss.getQuarkRelativeAndAdd(softIrqsQuark, String.valueOf(softIrqNumber));
            List<String[]> softIrqPaths = cpuQuarks.stream()
                .map(quark -> {
                    String[] cpuQuarkPath = ss.getFullAttributePathArray(quark);
                    String[] irqAttributePath = new String[4];
                    irqAttributePath[0] = cpuQuarkPath[0];
                    irqAttributePath[1] = cpuQuarkPath[1];
                    irqAttributePath[2] = Attributes.SOFT_IRQS;
                    irqAttributePath[3] = String.valueOf(softIrqNumber);
                    return irqAttributePath;
                })
                .collect(Collectors.toList());

            ss.addAggregationRule(new AttributePriorityAggregationRule(ss, softIrqQuark, softIrqPaths));
        }

    }
}
