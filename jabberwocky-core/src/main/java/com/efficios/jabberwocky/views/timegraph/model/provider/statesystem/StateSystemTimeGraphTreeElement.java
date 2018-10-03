/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package com.efficios.jabberwocky.views.timegraph.model.provider.statesystem;

import ca.polymtl.dorsal.libdelorean.IStateSystemReader;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeElement;

import java.util.List;

/**
 * Implementation of a {@link TimeGraphTreeElement} specific for use by
 * {@link StateSystemModelProvider}. It links a state system quark to the tree
 * element.
 *
 * @author Alexandre Montplaisir
 */
public class StateSystemTimeGraphTreeElement extends TimeGraphTreeElement {

    private final int fSourceQuark;
    private final transient IStateSystemReader fStateSystem;

    /**
     * Constructor
     *
     * @param name
     *            The name this tree element should have.
     * @param children
     *            The children tree elements. You can pass an empty list for no
     *            children.
     * @param ss
     *            Reference to the state system backing this time graph
     * @param sourceQuark
     *            The state system quark wrapped by this tree element
     */
    public StateSystemTimeGraphTreeElement(String name,
            List<TimeGraphTreeElement> children,
                                           IStateSystemReader ss,
            int sourceQuark) {
        super(name, children);
        fStateSystem = ss;
        fSourceQuark = sourceQuark;
    }

    protected IStateSystemReader getStateSystem() {
        return fStateSystem;
    }

    /**
     * Get the quark wrapped by this tree element.
     *
     * @return The source quark
     */
    public int getSourceQuark() {
        return fSourceQuark;
    }

}
