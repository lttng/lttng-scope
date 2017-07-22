/*
 * Copyright (C) 2017 EfficiOS Inc.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.toolbar.debugopts;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for the package
 *
 * @noreference Messages class
 */
@NonNullByDefault({})
@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$

    public static String debugOptionsDialogTitle;
    public static String debugOptionsDialogName;

    public static String resetDefaultsButtonLabel;

    public static String tabNameGeneral;
    public static String controlPaintingEnabled;
    public static String controlEntryPadding;
    public static String controlRenderRangePadding;
    public static String controlUIUpdateDelay;
    public static String controlHScrollEnabled;

    public static String tabNameLoadingOverlay;
    public static String controlLoadingOverlayEnabled;
    public static String controlLoadingOverlayColor;
    public static String controlLoadingOverlayFullOpacity;
    public static String controlLoadingOverlayTransparentOpacity;
    public static String controlLoadingOverlayFadeIn;
    public static String controlLoadingOverlayFadeOut;

    public static String tabNameZoom;
    public static String controlZoomAnimationDuration;
    public static String controlZoomStep;
    public static String controlZoomPivotOnSelection;
    public static String controlZoomPivotOnMousePosition;

    public static String tabNameIntervals;
    public static String controlIntervalOpacity;

    public static String tabNameTooltips;
    public static String controlTooltipFontColor;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
