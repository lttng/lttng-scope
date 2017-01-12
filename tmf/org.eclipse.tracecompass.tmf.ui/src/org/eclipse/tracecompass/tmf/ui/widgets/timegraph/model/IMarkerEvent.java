/*******************************************************************************
 * Copyright (c) 2015, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.graphics.RGBA;
import org.eclipse.tracecompass.tmf.ui.activator.internal.Messages;

/**
 * Interface for a marker time event that includes a category, a color and
 * an optional label.
 */
public interface IMarkerEvent extends ITimeEvent {

    /** Bookmarks marker category */
    @NonNull String BOOKMARKS = requireNonNull(Messages.MarkerEvent_Bookmarks);

    /**
     * Get this marker's category.
     *
     * @return The category
     */
    String getCategory();

    /**
     * Get this marker's label.
     *
     * @return The label, or null
     */
    String getLabel();

    /**
     * Get this marker's color.
     *
     * @return The color
     */
    RGBA getColor();

    /**
     * Returns true if the marker is drawn in foreground, and false otherwise.
     *
     * @return true if the marker is drawn in foreground, and false otherwise.
     */
    boolean isForeground();
}
