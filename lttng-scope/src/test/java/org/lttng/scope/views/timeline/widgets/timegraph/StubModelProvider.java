/*
 * Copyright (C) 2016-2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline.widgets.timegraph;

import com.efficios.jabberwocky.views.timegraph.model.provider.TimeGraphModelProvider;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeElement;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeRender;
import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class StubModelProvider extends TimeGraphModelProvider {

    public static final String ENTRY_NAME_PREFIX = "Entry #";
    public static final TimeGraphTreeElement ROOT_ELEMENT;

    private static final int NB_ENTRIES = 20;

    private static final TimeGraphTreeRender TREE_RENDER;

    static {
        List<TimeGraphTreeElement> treeElements = IntStream.range(1, NB_ENTRIES)
            .mapToObj(i -> new TimeGraphTreeElement(ENTRY_NAME_PREFIX + i, Collections.emptyList()))
            .collect(Collectors.toList());
        ROOT_ELEMENT = new TimeGraphTreeElement("Dummy trace", treeElements);
        TREE_RENDER =  new TimeGraphTreeRender(ROOT_ELEMENT);
    }

    protected StubModelProvider() {
        super("Test",
                /* Sorting modes */
                null,
                /* Filter modes */
                null,
                /* State provider */
                new StubModelStateProvider(),
                /* Arrow providers */
                ImmutableList.of(new StubModelArrowProvider1(), new StubModelArrowProvider2()));
    }

    @Override
    public TimeGraphTreeRender getTreeRender() {
        return TREE_RENDER;
    }

}
