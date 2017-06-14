/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.lttng.kernel.core.views.kernel.resources2.elements;

import java.util.Collections;

import org.lttng.scope.tmf2.views.core.timegraph.model.provider.statesystem.StateSystemTimeGraphTreeElement;

/**
 * Element of the Resources time graph which represents an IRQ (Software or
 * Hardware).
 *
 * @author Alexandre Montplaisir
 */
public class ResourcesTreeIrqElement extends StateSystemTimeGraphTreeElement {

    /** Type of IRQ */
    public enum IrqType {
        /** Hardware IRQ */
        IRQ,
        /** Software IRQ */
        SOFTIRQ
    }

    private final IrqType fIrqType;
    private final int fIrqNumber;

    /**
     * Constructor
     *
     * @param irqType
     *            Type of IRQ
     * @param irqNumber
     *            IRQ number
     * @param sourceQuark
     *            The corresponding quark (under the "CPUs" sub-tree) in the state
     *            system.
     */
    public ResourcesTreeIrqElement(IrqType irqType, int irqNumber, int sourceQuark) {
        super(getName(irqType, irqNumber),
                Collections.emptyList(),
                sourceQuark);

        fIrqType = irqType;
        fIrqNumber = irqNumber;
    }

    private static final String getName(IrqType irqType, int irqNumber) {
        String prefix;
        switch (irqType) {
        case IRQ:
            prefix = Messages.treeElementPrefixIrq;
            break;
        case SOFTIRQ:
        default:
            prefix = Messages.treeElementPrefixSoftIrq;
            break;
        }
        return prefix + ' ' + String.valueOf(irqNumber);
    }

    /**
     * Get the IRQ type.
     *
     * @return IRQ type
     */
    public IrqType getIrqType() {
        return fIrqType;
    }

    /**
     * Get the IRQ number.
     *
     * @return IRQ number
     */
    public int getIrqNumber() {
        return fIrqNumber;
    }

}
