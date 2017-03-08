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
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.ImmutableList;

public class TimeGraphTreeRender {

    public static final TimeGraphTreeRender EMPTY_RENDER = new TimeGraphTreeRender(Collections.emptyList());

    private final List<TimeGraphTreeElement> fTreeElements;

    public TimeGraphTreeRender(List<TimeGraphTreeElement> elements) {
        fTreeElements = ImmutableList.copyOf(elements);
    }

    public List<TimeGraphTreeElement> getAllTreeElements() {
        return fTreeElements;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fTreeElements);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TimeGraphTreeRender other = (TimeGraphTreeRender) obj;
        return (Objects.equals(fTreeElements, other.fTreeElements));
    }

}
