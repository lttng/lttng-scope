/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timegraph.swtjfx;

import java.util.Collections;

import org.lttng.scope.tmf2.views.core.timegraph.model.render.ColorDefinition;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.BasicTimeGraphStateInterval;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeElement;

/**
 * Dummy interval model object representing a "multi-state", which means a case
 * where more than one state exists for a given pixel.
 *
 * @author Alexandre Montplaisir
 */
public class MultiStateInterval extends BasicTimeGraphStateInterval {

    private static final String MULTI_STATE_NAME = "Multi-state"; //$NON-NLS-1$
    private static final ColorDefinition MULTI_STATE_COLOR = new ColorDefinition(0, 0, 0);

    /**
     * Constructor
     *
     * @param timestamp
     *            The timestamp of the multi-state interval. Those are normally
     *            valid for only one pixel, so they do not a defined end time,
     *            they just end "one pixel later".
     * @param treeElement
     *            The tree element to which this interval is associated
     * @param lineThickness
     *            The line thickness to use
     */
    public MultiStateInterval(long timestamp, TimeGraphTreeElement treeElement, LineThickness lineThickness) {
        super(timestamp, timestamp + 1, treeElement, MULTI_STATE_NAME, null, MULTI_STATE_COLOR, lineThickness, Collections.emptyMap());
    }

}
