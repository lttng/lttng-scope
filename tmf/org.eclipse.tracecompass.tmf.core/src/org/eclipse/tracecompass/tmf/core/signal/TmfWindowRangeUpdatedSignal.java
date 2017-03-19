/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Deprecate current time
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.signal;

import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;

import com.google.common.base.MoreObjects;

/**
 * A new range has been selected for the visible (zoom) time range.
 *
 * To update the selection range instead, use
 * {@link TmfSelectionRangeUpdatedSignal}.
 *
 * @author Francois Chouinard
 */
public class TmfWindowRangeUpdatedSignal extends TmfSignal {

    private final TmfTimeRange fCurrentRange;
    private final boolean fEcho;

    /**
     * Constructor
     *
     * @param source
     *            Object sending this signal
     * @param range
     *            The new time range
     */
    public TmfWindowRangeUpdatedSignal(Object source, TmfTimeRange range) {
        this(source, range, false);
    }

    /**
     * Constructor
     *
     * @param source
     *            Object sending this signal
     * @param range
     *            The new time range
     * @param echo
     *            The echo flag. Can checked by recipients to determine if they
     *            should process this signal or not, even though they might be
     *            the source.
     */
    public TmfWindowRangeUpdatedSignal(Object source, TmfTimeRange range, boolean echo) {
        super(source);
        fCurrentRange = range;
        fEcho = echo;
    }

    /**
     * @return This signal's time range
     */
    public TmfTimeRange getCurrentRange() {
        return fCurrentRange;
    }

    /**
     * @return Echo flag
     */
    public boolean echo() {
        return fEcho;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("timerange", fCurrentRange) //$NON-NLS-1$
            .add("echo", fEcho) //$NON-NLS-1$
            .toString();
    }
}
