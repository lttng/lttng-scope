/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.lttng.kernel.core.analysis.os;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.primitives.Ints;

import ca.polymtl.dorsal.libdelorean.ITmfStateSystemBuilder;
import ca.polymtl.dorsal.libdelorean.aggregation.AttributePriorityAggregationRule;
import ca.polymtl.dorsal.libdelorean.aggregation.IStateAggregationRule;
import ca.polymtl.dorsal.libdelorean.statevalue.TmfStateValue;

/**
 * FIXME Placeholder for the aggregation that should be used in the kernel state
 * system analysis.
 */
public class KernelStateAggregationRules {

    /**
     * @param ss
     *            Target state system
     */
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
