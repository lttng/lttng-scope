/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.tmf2.views.core.timegraph.model.render.tree;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

public class TimeGraphTreeRender {

    public static final TimeGraphTreeRender EMPTY_RENDER = new TimeGraphTreeRender(Collections.emptyList(), 0L, 0L);

    private final List<TimeGraphTreeElement> fTreeElements;
    private final long fStartTime;
    private final long fEndTime;

    public TimeGraphTreeRender(List<TimeGraphTreeElement> elements,
            long startTime, long endTime) {
        fTreeElements = ImmutableList.copyOf(elements);
        fStartTime = startTime;
        fEndTime = endTime;
    }

    public List<TimeGraphTreeElement> getAllTreeElements() {
        return fTreeElements;
    }

    public long getStartTime() {
        return fStartTime;
    }

    public long getEndTime() {
        return fEndTime;
    }
}
