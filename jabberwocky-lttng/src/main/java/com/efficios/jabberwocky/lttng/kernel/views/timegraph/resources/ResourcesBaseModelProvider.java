/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.kernel.views.timegraph.resources;

import com.efficios.jabberwocky.lttng.kernel.analysis.os.KernelAnalysis;
import com.efficios.jabberwocky.lttng.kernel.views.timegraph.resources.elements.ResourcesIrqTreeElement;
import com.efficios.jabberwocky.views.timegraph.model.provider.states.TimeGraphModelStateProvider;
import com.efficios.jabberwocky.views.timegraph.model.provider.statesystem.StateSystemModelProvider;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeElement;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeRender;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Base class for Resources (for now, CPUs and IRQs) model providers.
 *
 * Implementations of this class can define the hierarchy the resources are to
 * be presented in, for example CPUs/IRQs or IRQs/CPUs.
 *
 * @author Alexandre Montplaisir
 */
public abstract class ResourcesBaseModelProvider extends StateSystemModelProvider {

    private static final Supplier<TimeGraphModelStateProvider> STATE_PROVIDER = () -> {
        return new ResourcesModelStateProvider();
    };

    /**
     * Comparator to sort IRQ entries in the tree model.
     *
     * Shows (hardware) IRQs first, followed by Soft IRQs. Within each section they
     * are sorted by numerical (not String!) value of their IRQ number.
     */
    // TODO We have to define a Comparator<TimeGraphTreeElement> here because
    // generic types are not covariant. If/when we move this to Kotlin,
    // this could become a Comparator<ResourcesIrqTreeElement> instead.
    protected static final Comparator<TimeGraphTreeElement> IRQ_SORTER = Comparator
            .<TimeGraphTreeElement, ResourcesIrqTreeElement.IrqType> comparing(treeElem -> {
                if (treeElem instanceof ResourcesIrqTreeElement) {
                    return ((ResourcesIrqTreeElement) treeElem).getIrqType();
                } else {
                    return null;
                }
            })
            .thenComparingInt(treeElem -> {
                if (treeElem instanceof ResourcesIrqTreeElement) {
                    return ((ResourcesIrqTreeElement) treeElem).getIrqNumber();
                } else {
                    return -1;
                }
            });

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
                KernelAnalysis.INSTANCE,
                treeRenderFunction);
    }

}
