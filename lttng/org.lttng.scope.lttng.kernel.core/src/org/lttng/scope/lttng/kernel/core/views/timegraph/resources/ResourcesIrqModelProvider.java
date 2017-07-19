/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.lttng.kernel.core.views.timegraph.resources;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.lttng.scope.lttng.kernel.core.analysis.os.Attributes;
import org.lttng.scope.lttng.kernel.core.views.timegraph.resources.elements.ResourcesIrqTreeElement;
import org.lttng.scope.lttng.kernel.core.views.timegraph.resources.elements.ResourcesIrqTreeElement.IrqType;

import com.efficios.jabberwocky.timegraph.model.render.tree.TimeGraphTreeElement;
import com.efficios.jabberwocky.timegraph.model.render.tree.TimeGraphTreeRender;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.primitives.Ints;

import ca.polymtl.dorsal.libdelorean.ITmfStateSystem;

/**
 * View model for a Resources view showing IRQs and SoftIRQs.
 *
 * @author Alexandre Montplaisir
 */
public class ResourcesIrqModelProvider extends ResourcesBaseModelProvider {

    private static final String[] IRQS_QUARK_PATTERN = { Attributes.IRQS, "*" }; //$NON-NLS-1$
    private static final String[] SOFT_IRQS_QUARK_PATTERN = { Attributes.SOFT_IRQS, "*" }; //$NON-NLS-1$

    /**
     * Get the tree element name for every cpu.
     */
    @VisibleForTesting
    public static final Function<TreeRenderContext, TimeGraphTreeRender> SS_TO_TREE_RENDER_FUNCTION = (treeContext) -> {
        ITmfStateSystem ss = treeContext.ss;

        List<ResourcesIrqTreeElement> treeElems = new LinkedList<>();
        /* Create "IRQ *" children */
        ss.getQuarks(IRQS_QUARK_PATTERN).forEach(irqQuark -> {
            String name = ss.getAttributeName(irqQuark);
            Integer irqNumber = Ints.tryParse(name);
            if (irqNumber != null) {
                treeElems.add(new ResourcesIrqTreeElement(IrqType.IRQ, irqNumber, irqQuark));
            }
        });

        /* Create "SoftIRQ *" children */
        ss.getQuarks(SOFT_IRQS_QUARK_PATTERN).forEach(irqQuark -> {
            String name = ss.getAttributeName(irqQuark);
            Integer irqNumber = Ints.tryParse(name);
            if (irqNumber != null) {
                treeElems.add(new ResourcesIrqTreeElement(IrqType.SOFTIRQ, irqNumber, irqQuark));
            }
        });

        Collections.sort(treeElems, IRQ_SORTER);
        @SuppressWarnings("rawtypes")
        List treeElems2 = treeElems;
        TimeGraphTreeElement rootElement = new TimeGraphTreeElement(treeContext.traceName, treeElems2);
        return new TimeGraphTreeRender(rootElement);
    };

    /**
     * Constructor
     */
    public ResourcesIrqModelProvider() {
        super(requireNonNull(Messages.resourcesIrqProviderName), SS_TO_TREE_RENDER_FUNCTION);
    }

}
