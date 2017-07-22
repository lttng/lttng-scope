/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   William Bourque - Initial API and implementation
 *   Francois Chouinard - Cleanup and refactoring
 *   Francois Chouinard - Moved from LTTng to TMF
 *   Patrick Tasse - Update for histogram selection range and tool tip
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.histogram;

import org.eclipse.osgi.util.NLS;

/**
 * Messages file for the histogram widgets.
 * <p>
 *
 * @author Francois Chouinard
 * @noreference Messages class
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$

    public static String HistogramView_showTraces;
    public static String HistogramView_hideLostEvents;
    public static String HistogramView_selectionStartLabel;
    public static String HistogramView_selectionEndLabel;
    public static String HistogramView_windowSpanLabel;
    public static String Histogram_selectionSpanToolTip;
    public static String Histogram_bucketRangeToolTip;
    public static String Histogram_eventCountToolTip;
    public static String Histogram_lostEventCountToolTip;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }

}
