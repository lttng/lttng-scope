/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.lttng.kernel.core.views.kernel.resources2;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.lttng.scope.lttng.kernel.core.analysis.os.Attributes;
import org.lttng.scope.lttng.kernel.core.analysis.os.KernelAnalysisModule;
import org.lttng.scope.lttng.kernel.core.views.kernel.resources2.elements.ResourcesTreeCpuElement;
import org.lttng.scope.lttng.kernel.core.views.kernel.resources2.elements.ResourcesTreeIrqElement;
import org.lttng.scope.lttng.kernel.core.views.kernel.resources2.elements.ResourcesTreeIrqElement.IrqType;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.states.ITimeGraphModelStateProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.statesystem.StateSystemModelProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeElement;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeRender;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.primitives.Ints;

import ca.polymtl.dorsal.libdelorean.ITmfStateSystem;

/**
 * Base model provider for the "Resources" time graph. It displays the states of
 * CPUs, as well as the state of IRQs of each CPU.
 *
 * TODO This timegraph only models CPU states. States of IRQs under each CPU
 * should be added at some point.
 *
 * @author Alexandre Montplaisir
 */
public class ResourcesModelProvider extends StateSystemModelProvider {

    private static final Supplier<ITimeGraphModelStateProvider> STATE_PROVIDER = () -> {
        return new ResourcesModelStateProvider();
    };

    // ------------------------------------------------------------------------
    // Tree render
    // ------------------------------------------------------------------------

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

    private static final Comparator<ResourcesTreeIrqElement> IRQ_SORTER = Comparator
            .<ResourcesTreeIrqElement, IrqType> comparing(treeElem -> treeElem.getIrqType())
            .thenComparingInt(treeElem -> treeElem.getIrqNumber());

    /**
     * Get the tree element name for every cpu.
     */
    @VisibleForTesting
    public static final Function<TreeRenderContext, TimeGraphTreeRender> SS_TO_TREE_RENDER_FUNCTION = (treeContext) -> {
        ITmfStateSystem ss = treeContext.ss;

        List<TimeGraphTreeElement> treeElems = ss.getQuarks(CPUS_QUARK_PATTERN).stream()
                .map(cpuQuark -> {
                    String cpuStr = ss.getAttributeName(cpuQuark);
                    Integer cpu = Ints.tryParse(cpuStr);
                    if (cpu == null) {
                        return null;
                    }

                    List<ResourcesTreeIrqElement> children = new LinkedList<>();

                    /* Add the "IRQ" children. */
                    int irqsQuark = ss.getQuarkRelative(cpuQuark, Attributes.IRQS);
                    for (int irqQuark : ss.getSubAttributes(irqsQuark, false)) {
                        int irqNumber = Ints.tryParse(ss.getAttributeName(irqQuark));
                        children.add(new ResourcesTreeIrqElement(IrqType.IRQ, irqNumber, irqQuark));
                    }

                    /* Add the "SoftIRQ" children. */
                    int softIrqsQuark = ss.getQuarkRelative(cpuQuark, Attributes.SOFT_IRQS);
                    for (int softIrqQuark : ss.getSubAttributes(softIrqsQuark, false)) {
                        int irqNumber = Ints.tryParse(ss.getAttributeName(softIrqQuark));
                        children.add(new ResourcesTreeIrqElement(IrqType.SOFTIRQ, irqNumber, softIrqQuark));
                    }

                    Collections.sort(children, IRQ_SORTER);
                    /* Generic types are not covariant :/ Use a raw type instead... */
                    @SuppressWarnings("rawtypes")
                    List children2 = children;
                    return new ResourcesTreeCpuElement(cpu, children2, cpuQuark);
                })
                .filter(Objects::nonNull)
                /*
                 * Sort entries according to their CPU number (not just an
                 * alphabetical sort!)
                 */
                .sorted(Comparator.comparingInt(ResourcesTreeCpuElement::getCpu))
                .collect(Collectors.toList());

        TimeGraphTreeElement rootElement = new TimeGraphTreeElement(treeContext.traceName, treeElems);
        return new TimeGraphTreeRender(rootElement);
    };

    /**
     * Constructor
     */
    public ResourcesModelProvider() {
        super(requireNonNull(Messages.resourcesProviderName),
                null,
                null,
                STATE_PROVIDER.get(),
                null,
                /* Parameters specific to state system render providers */
                KernelAnalysisModule.ID,
                SS_TO_TREE_RENDER_FUNCTION);
    }

}
