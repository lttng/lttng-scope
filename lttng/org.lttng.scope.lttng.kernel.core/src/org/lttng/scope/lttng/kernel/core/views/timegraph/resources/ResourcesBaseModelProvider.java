/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.lttng.kernel.core.views.timegraph.resources;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Supplier;

import org.lttng.scope.lttng.kernel.core.views.timegraph.resources.elements.ResourcesIrqTreeElement;
import org.lttng.scope.lttng.kernel.core.views.timegraph.resources.elements.ResourcesIrqTreeElement.IrqType;

import com.efficios.jabberwocky.lttng.kernel.analysis.os.KernelAnalysis;
import com.efficios.jabberwocky.timegraph.model.provider.states.ITimeGraphModelStateProvider;
import com.efficios.jabberwocky.timegraph.model.provider.statesystem.StateSystemModelProvider;
import com.efficios.jabberwocky.timegraph.model.render.tree.TimeGraphTreeRender;

/**
 * Base class for Resources (for now, CPUs and IRQs) model providers.
 *
 * Implementations of this class can define the hierarchy the resources are to
 * be presented in, for example CPUs/IRQs or IRQs/CPUs.
 *
 * @author Alexandre Montplaisir
 */
public abstract class ResourcesBaseModelProvider extends StateSystemModelProvider {

    private static final Supplier<ITimeGraphModelStateProvider> STATE_PROVIDER = () -> {
        return new ResourcesModelStateProvider();
    };

    /**
     * Comparator to sort IRQ entries in the tree model.
     *
     * Shows (hardware) IRQs first, followed by Soft IRQs. Within each section they
     * are sorted by numerical (not String!) value of their IRQ number.
     */
    protected static final Comparator<ResourcesIrqTreeElement> IRQ_SORTER = Comparator
            .<ResourcesIrqTreeElement, IrqType> comparing(treeElem -> treeElem.getIrqType())
            .thenComparingInt(treeElem -> treeElem.getIrqNumber());

    /**
     * Constructor
     *
     * @param providerName
     *            Name of this provider
     * @param treeRenderFunction
     *            Function to generate a tree render from a given tree context
     */
    public ResourcesBaseModelProvider(String providerName, Function<TreeRenderContext, TimeGraphTreeRender> treeRenderFunction) {
        super(providerName,
                null,
                null,
                STATE_PROVIDER.get(),
                null,
                /* Parameters specific to state system render providers */
                KernelAnalysis.instance(),
                treeRenderFunction);
    }

}
