/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.lttng.kernel.core.views.timegraph.resources.elements;

import java.util.List;
import java.util.function.Predicate;

import com.efficios.jabberwocky.timegraph.model.provider.statesystem.StateSystemTimeGraphTreeElement;
import com.efficios.jabberwocky.timegraph.model.render.tree.TimeGraphTreeElement;
import com.efficios.jabberwocky.trace.event.ITraceEvent;

/**
 * Element of the Resources time graph which represents a CPU.
 *
 * @author Alexandre Montplaisir
 */
public class ResourcesCpuTreeElement extends StateSystemTimeGraphTreeElement {

    private final int fCpu;

    /**
     * Constructor
     *
     * @param cpu
     *            The CPU of this CPU tree element
     * @param children
     *            The child elements
     * @param sourceQuark
     *            The corresponding quark (under the "CPUs" sub-tree) in the
     *            state system.
     */
    public ResourcesCpuTreeElement(int cpu,
            List<TimeGraphTreeElement> children, int sourceQuark) {
        super(Messages.treeElementPrefixCpu + ' ' + String.valueOf(cpu),
                children,
                sourceQuark);

        fCpu = cpu;
    }

    /**
     * Get the CPU represented by this tree element
     *
     * @return The CPU number
     */
    public int getCpu() {
        return fCpu;
    }

    @Override
    public Predicate<ITraceEvent> getEventMatching() {
        return (event -> fCpu == event.getCpu());
    }

}
