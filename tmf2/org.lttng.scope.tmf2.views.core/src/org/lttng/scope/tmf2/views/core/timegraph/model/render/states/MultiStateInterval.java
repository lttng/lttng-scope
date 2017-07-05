/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.core.timegraph.model.render.states;

import java.util.Collections;

import org.lttng.scope.tmf2.views.core.timegraph.model.render.ColorDefinition;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.LineThickness;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.StateDefinition;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeElement;

/**
 * Dummy interval model object representing a "multi-state", which means a case
 * where more than one state exists for a given pixel.
 *
 * @author Alexandre Montplaisir
 */
public final class MultiStateInterval extends BasicTimeGraphStateInterval {

    /**
     * State definition for "multi-states"
     */
    public static final StateDefinition MULTI_STATE_DEFINITION = new StateDefinition("Multi-state", //$NON-NLS-1$
            new ColorDefinition(0, 0, 0),
            LineThickness.NORMAL);

    /**
     * Constructor
     *
     * @param startTime
     *            Start time
     * @param endTime
     *            End time
     * @param treeElement
     *            The tree element to which this interval is associated
     */
    public MultiStateInterval(long startTime, long endTime, TimeGraphTreeElement treeElement) {
        super(startTime,
                endTime,
                treeElement,
                MULTI_STATE_DEFINITION,
                /* Label */
                null,
                /* Properties */
                Collections.emptyMap());
    }

    @Override
    public boolean isMultiState() {
        return true;
    }

}
