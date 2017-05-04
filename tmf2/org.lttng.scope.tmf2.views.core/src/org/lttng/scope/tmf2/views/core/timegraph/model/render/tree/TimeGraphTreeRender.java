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

/**
 * Render of a tree of the timegraph. Contains the tree elements that compose
 * the current tree.
 *
 * In a timegraph, the "tree" part is usually shown on the left-hand side, and
 * lists the tree elements, which represent attributes of a model. A tree render
 * is a "snapshot" of this tree that is valid for a given timestamp or
 * timerange.
 *
 * Some timegraphs may use a tree that is valid for the whole time range of a
 * trace. Other timegraphs may display a different tree for different parts of
 * the trace.
 *
 * @author Alexandre Montplaisir
 */
public class TimeGraphTreeRender {

    /**
     * A static reference to an empty render, which can be used to represent an
     * uninitialized state for example (by comparing with ==).
     */
    public static final TimeGraphTreeRender EMPTY_RENDER = new TimeGraphTreeRender(Collections.emptyList());

    private final List<TimeGraphTreeElement> fTreeElements;

    /**
     * Constructor
     *
     * @param elements
     *            The elements that are part of this tree render
     */
    public TimeGraphTreeRender(List<TimeGraphTreeElement> elements) {
        fTreeElements = ImmutableList.copyOf(elements);
    }

    /**
     * Get a list of all the tree elements in this render.
     *
     * This should also contains all the child elements that are also contained
     * in each element's {@link TimeGraphTreeElement#getChildElements()}. It can
     * be used to run an action on all elements of a render.
     *
     * @return A list of all the tree elements
     */
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
