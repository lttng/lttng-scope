/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package com.efficios.jabberwocky.views.timegraph.model.render;

import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeElement;

import java.util.Objects;

/**
 * A time graph event is a virtual representation of a location on the time
 * graph, defined by its timestamp and tree element.
 *
 * It does not have a directly corresponding UI representation, but is used as
 * an intermediate construct in intervals and drawn events, for example.
 *
 * @author Alexandre Montplaisir
 */
public class TimeGraphEvent {

    private final long fTimestamp;
    private final TimeGraphTreeElement fTreeElement;

    /**
     * Constructor
     *
     * @param timestamp
     *            The timestamp of the event
     * @param treeElement
     *            The tree element to which this even belong
     */
    public TimeGraphEvent(long timestamp, TimeGraphTreeElement treeElement) {
        fTimestamp = timestamp;
        fTreeElement = treeElement;
    }

    /**
     * Get the timestamp of this event.
     *
     * @return The timestamp
     */
    public long getTimestamp() {
        return fTimestamp;
    }

    /**
     * Get the tree element of this event.
     *
     * @return The tree element
     */
    public TimeGraphTreeElement getTreeElement() {
        return fTreeElement;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fTimestamp, fTreeElement);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TimeGraphEvent other = (TimeGraphEvent) obj;
        return (fTimestamp == other.fTimestamp
                && Objects.equals(fTreeElement, other.fTreeElement));
    }
}
