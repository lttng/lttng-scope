/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.tmf2.views.core.timegraph.model.render.tree;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

public class TimeGraphTreeElement {

    private final String fName;
    private final List<TimeGraphTreeElement> fChildElements;

    public TimeGraphTreeElement(String name, List<TimeGraphTreeElement> children) {
        fName = name;
        fChildElements = ImmutableList.copyOf(children);
    }

    public String getName() {
        return fName;
    }

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
    public @Nullable Predicate<ITmfEvent> getEventMatching() {
        /* Sub-classes can override */
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fName, fChildElements);
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
