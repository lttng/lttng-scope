/*
 * Copyright (C) 2017-2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.kernel.analysis.os

import ca.polymtl.dorsal.libdelorean.IStateSystemWriter
import ca.polymtl.dorsal.libdelorean.aggregation.AttributePriorityAggregationRule
import ca.polymtl.dorsal.libdelorean.statevalue.StateValue

/**
 * FIXME Placeholder for the aggregation that should be used in the kernel state
 * system analysis.
 */
fun setupKernelAnalysisAggregationRules(ss: IStateSystemWriter) {
    /* Set up the virtual "IRQs" and "SoftIRQs" sub-trees */
    val irqsQuark = ss.getQuarkAbsoluteAndAdd(Attributes.IRQS).apply {
        if (this == ss.nbAttributes) {
            /*
             * If we just created this attribute, make sure we put a null value into
             * it so that upcoming queries return something. Should be fixed in the state
             * system library.
             */
            ss.modifyAttribute(ss.startTime, StateValue.nullValue(), this)
        }
    }

    val softIrqsQuark = ss.getQuarkAbsoluteAndAdd(Attributes.SOFT_IRQS).apply {
        if (this == ss.nbAttributes) {
            /*
             * If we just created this attribute, make sure we put a null value into
             * it so that upcoming queries return something. Should be fixed in the state
             * system library.
             */
            ss.modifyAttribute(ss.startTime, StateValue.nullValue(), this)
        }
    }

    val cpuQuarks = ss.getSubAttributes(ss.getQuarkAbsoluteAndAdd(Attributes.CPUS), false)

    cpuQuarks
            /* Retrieve all known IRQ numbers */
            .map { cpuQuark -> ss.getQuarkRelativeAndAdd(cpuQuark, Attributes.IRQS) }
            .flatMap { cpuIrqsQuark ->
                ss.getSubAttributes(cpuIrqsQuark, false)
                        .map { irqQuark -> ss.getAttributeName(irqQuark) }
                        .mapNotNull { name -> name.toIntOrNull() }
            }
            .map { it.toString() }
            /* Define the rule to look into each known IRQ using a priority aggregation. */
            .forEach { irqNumber ->
                val irqQuark = ss.getQuarkRelativeAndAdd(irqsQuark, irqNumber)
                val irqPaths = cpuQuarks
                        .map { quark -> ss.getFullAttributePathArray(quark) }
                        .map { cpuQuarkPath ->
                            arrayOf(
                                    cpuQuarkPath[0], // "CPUs"
                                    cpuQuarkPath[1], // CPU number
                                    Attributes.IRQS,
                                    irqNumber
                            )
                        }

                AttributePriorityAggregationRule(ss, irqQuark, irqPaths)
                        .let { ss.addAggregationRule(it) }
            }


    cpuQuarks
            /* Retrieve all known SoftIRQ numbers */
            .map { cpuQuark -> ss.getQuarkRelative(cpuQuark, Attributes.SOFT_IRQS) }
            .flatMap { cpuSoftIrqsQuark ->
                ss.getSubAttributes(cpuSoftIrqsQuark, false)
                        .map { irqQuark -> ss.getAttributeName(irqQuark) }
                        .mapNotNull { name -> name.toIntOrNull() }
            }
            .map { it.toString() }
            /* Define the rule to look into each known SoftIRQ using a priority aggregation. */
            .forEach { softIrqNumber ->
                val softIrqQuark = ss.getQuarkRelativeAndAdd(softIrqsQuark, softIrqNumber)
                val softIrqPaths = cpuQuarks
                        .map { quark -> ss.getFullAttributePathArray(quark) }
                        .map { cpuQuarkPath ->
                            arrayOf(
                                    cpuQuarkPath[0], // "CPUs"
                                    cpuQuarkPath[1], // CPU number
                                    Attributes.SOFT_IRQS,
                                    softIrqNumber
                            )
                        }

                AttributePriorityAggregationRule(ss, softIrqQuark, softIrqPaths)
                        .let { ss.addAggregationRule(it) }
            }

}
