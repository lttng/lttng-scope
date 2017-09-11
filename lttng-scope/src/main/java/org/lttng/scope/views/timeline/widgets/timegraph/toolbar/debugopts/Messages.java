/*
 * Copyright (C) 2017 EfficiOS Inc.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline.widgets.timegraph.toolbar.debugopts;

/**
 * Message bundle for the package
 *
 * @noreference Messages class
 */
@SuppressWarnings("javadoc")
public class Messages {

    static final String debugOptionsDialogTitle = "Bikeshedding";
    static final String debugOptionsDialogName = "Advanced View Configuration";

    static final String resetDefaultsButtonLabel = "Reset Defaults";

    static final String tabNameGeneral = "General";
    static final String controlPaintingEnabled = "Painting enabled";
    static final String controlEntryPadding = "Entry Padding";
    static final String controlRenderRangePadding = "Render time range padding";
    static final String controlUIUpdateDelay = "UI Update Delay (ms)";
    static final String controlHScrollEnabled = "HScrolling listener enabled";

    static final String tabNameLoadingOverlay = "Loading Overlay";
    static final String controlLoadingOverlayEnabled = "Loading overlay enabled";
    static final String controlLoadingOverlayColor = "Overlay color";
    static final String controlLoadingOverlayFullOpacity = "Full Opacity";
    static final String controlLoadingOverlayTransparentOpacity = "Transparent Opacity";
    static final String controlLoadingOverlayFadeIn = "Fade-in duration (ms)";
    static final String controlLoadingOverlayFadeOut = "Fade-out duration (ms)";

    static final String tabNameZoom = "Zoom";
    static final String controlZoomAnimationDuration = "Zoom animation duration (ms)";
    static final String controlZoomStep = "Zoom step";
    static final String controlZoomPivotOnSelection = "Zoom pivot on selection";
    static final String controlZoomPivotOnMousePosition = "Zoom pivot on mouse position";

    static final String tabNameIntervals = "Intervals";
    static final String controlIntervalOpacity = "State interval opacity";

    static final String tabNameTooltips = "Tooltips";
    static final String controlTooltipFontColor = "Font color";

    private Messages() {
    }
}
