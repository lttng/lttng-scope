/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package com.efficios.jabberwocky.views.timegraph.model.render.tree;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import com.efficios.jabberwocky.trace.event.TraceEvent;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;

/**
 * The "tree element" is the unit of a timegraph represented by a single line.
 * State intervals are aligned with tree elements to represent the states of the
 * attribute represented by its tree element.
 *
 * Tree elements can have children, which allows representing them as a tree
 * structure. At the visualization layer, sub-trees could be allowed to be
 * expanded/collapsed, which can then change the number of visible tree elements
 * in the timegraph.
 *
 * @author Alexandre Montplaisir
 */
public class TimeGraphTreeElement {

    /** Non-null reference to a dummy element */
    public static final TimeGraphTreeElement DUMMY_ELEMENT = new TimeGraphTreeElement("Dummy", Collections.emptyList()); //$NON-NLS-1$

    private final String fName;
    private final List<TimeGraphTreeElement> fChildElements;

    /**
     * Constructor, build a tree element by specifying its name and children
     * elements.
     *
     * @param name
     *            The name this tree element should have.
     * @param children
     *            The children tree elements. You can pass an empty list for no
     *            children.
     */
    public TimeGraphTreeElement(String name, List<TimeGraphTreeElement> children) {
        fName = name;
        fChildElements = ImmutableList.copyOf(children);
    }

    /**
     * Get the name of this tree element.
     *
     * @return The element's name
     */
    public String getName() {
        return fName;
    }

    /**
     * Get the child elements of this tree element.
     *
     * @return The child elements
     */
    public List<TimeGraphTreeElement> getChildElements() {
        return fChildElements;
    }

    /**
     * Determine if and how this tree element corresponds to a component of a
     * trace event.
     *
     * For example, if this tree element represents "CPU #2", then the predicate
     * should return true for all trace events belonging to CPU #2.
     *
     * The method returns null if this tree element does not correspond to a
     * particular aspect of trace events.
     *
     * @return The event matching predicate, if there is one
     */
    public @Nullable Predicate<TraceEvent> getEventMatching() {
        /* Sub-classes can override */
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fName, fChildElements);
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
        TimeGraphTreeElement other = (TimeGraphTreeElement) obj;
        return Objects.equals(fName, other.fName)
                && Objects.equals(fChildElements, other.fChildElements);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("fName", fName) //$NON-NLS-1$
            .add("fChildElements", fChildElements.toString()) //$NON-NLS-1$
            .toString();
    }

}
