/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.kernel.views.timegraph.resources;

import ca.polymtl.dorsal.libdelorean.IStateSystemReader;
import com.efficios.jabberwocky.lttng.kernel.analysis.os.Attributes;
import com.efficios.jabberwocky.lttng.kernel.views.timegraph.resources.elements.ResourcesCpuTreeElement;
import com.efficios.jabberwocky.lttng.kernel.views.timegraph.resources.elements.ResourcesIrqTreeElement;
import com.efficios.jabberwocky.lttng.kernel.views.timegraph.resources.elements.ResourcesIrqTreeElement.IrqType;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeElement;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeRender;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.primitives.Ints;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * View model for a Resources view, showing CPUs as the first level, then
 * per-cpu IRQs as the second level.
 *
 * @author Alexandre Montplaisir
 */
public class ResourcesCpuIrqModelProvider extends ResourcesBaseModelProvider {

    /**
     * Each "CPU" attribute has the following children:
     *
     * <ul>
     * <li>Current_thread</li>
     * <li>Soft_IRQs</li>
     * <li>IRQs</li>
     * </ul>
     */
    private static final String[] CPUS_QUARK_PATTERN = { Attributes.CPUS, "*" }; //$NON-NLS-1$

    /**
     * Get the tree element name for every cpu.
     */
    @VisibleForTesting
    public static final Function<TreeRenderContext, TimeGraphTreeRender> SS_TO_TREE_RENDER_FUNCTION = (treeContext) -> {
        IStateSystemReader ss = treeContext.ss;

        List<TimeGraphTreeElement> treeElems = ss.getQuarks(CPUS_QUARK_PATTERN).stream()
                .<TimeGraphTreeElement> map(cpuQuark -> {
                    String cpuStr = ss.getAttributeName(cpuQuark);
                    Integer cpu = Ints.tryParse(cpuStr);
                    if (cpu == null) {
                        return null;
                    }

                    List<TimeGraphTreeElement> children = new LinkedList<>();

                    /* Add the "IRQ" children. */
                    int irqsQuark = ss.getQuarkRelative(cpuQuark, Attributes.IRQS);
                    for (int irqQuark : ss.getSubAttributes(irqsQuark, false)) {
                        Integer irqNumber = Ints.tryParse(ss.getAttributeName(irqQuark));
                        if (irqNumber == null) {
                            /* Invalid attribute */
                            continue;
                        }
                        children.add(new ResourcesIrqTreeElement(IrqType.IRQ, irqNumber, ss, irqQuark));
                    }

                    /* Add the "SoftIRQ" children. */
                    int softIrqsQuark = ss.getQuarkRelative(cpuQuark, Attributes.SOFT_IRQS);
                    for (int softIrqQuark : ss.getSubAttributes(softIrqsQuark, false)) {
                        Integer irqNumber = Ints.tryParse(ss.getAttributeName(softIrqQuark));
                        if (irqNumber == null) {
                            /* Invalid attribute */
                            continue;
                        }
                        children.add(new ResourcesIrqTreeElement(IrqType.SOFTIRQ, irqNumber, ss, softIrqQuark));
                    }

                    children.sort(IRQ_SORTER);
                    return new ResourcesCpuTreeElement(cpu, children, ss, cpuQuark);
                })
                .filter(Objects::nonNull)
                /*
                 * Sort entries according to their CPU number (not just an
                 * alphabetical sort!)
                 */
                .sorted(Comparator.comparingInt(elem -> ((ResourcesCpuTreeElement) elem).getCpu()))
                .collect(Collectors.toList());

        TimeGraphTreeElement rootElement = new TimeGraphTreeElement(treeContext.traceName, treeElems);
        return new TimeGraphTreeRender(rootElement);
    };

    /**
     * Constructor
     */
    public ResourcesCpuIrqModelProvider() {
        super(requireNonNull(Messages.resourcesCpuIrqProviderName), SS_TO_TREE_RENDER_FUNCTION);
    }

}
