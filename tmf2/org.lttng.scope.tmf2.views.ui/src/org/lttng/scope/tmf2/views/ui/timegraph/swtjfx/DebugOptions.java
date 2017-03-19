/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timegraph.swtjfx;

import static java.util.Objects.requireNonNull;

import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Debug options for the {@link SwtJfxTimeGraphViewer}. Advanced users or unit
 * tests might want to modify these.
 *
 * @author Alexandre Montplaisir
 */
class DebugOptions {

    private boolean fPaintingEnabled = true;

    private int fEntryPadding = 5;
    private double fRenderRangePadding = 0.1;
    private int fUIUpdateDelay = 250;
    private boolean fScrollingListenersEnabled = true;

    private long fZoomAnimationDuration = 50;
    private double fZoomStep = 0.08;

    private Font fTextFont = requireNonNull(new Text().getFont());
    private String fEllipsisString = "..."; //$NON-NLS-1$
    private transient double fEllipsisWidth;

    /**
     * Constructor using the default options
     */
    public DebugOptions() {
        recomputeEllipsisWidth();
    }

    /**
     * If the automatic redrawing of the view is enabled.
     *
     * @return The repainting status
     */
    public boolean isPaintingEnabled() {
        return fPaintingEnabled;
    }

    void setPaintingEnabled(boolean bool) {
        fPaintingEnabled = bool;
    }

    /**
     * Number of tree elements to print above *and* below the visible range
     *
     * @return The number of entries
     */
    public int getEntryPadding() {
        return fEntryPadding;
    }

    /**
     * How much "padding" around the current visible window, on the left and
     * right, should be pre-rendered. Expressed as a fraction of the current
     * window (for example, 1.0 would render one "page" on each side).
     *
     * @return The fraction of padding on each side
     */
    public double getRenderRangePadding() {
        return fRenderRangePadding;
    }

    /**
     * Time between UI updates, in milliseconds
     *
     * @return The delay in milliseconds
     */
    public int getUIUpdateDelay() {
        return fUIUpdateDelay;
    }

    /**
     * Whether the view should respond to vertical or horizontal scrolling
     * actions.
     *
     * @return If scrolling listeners are enabled
     */
    public boolean isScrollingListenersEnabled() {
        return fScrollingListenersEnabled;
    }

    void setScrollingListenersEnabled(boolean bool) {
        fScrollingListenersEnabled = bool;
    }

    /**
     * The zoom animation duration, which is the amount of milliseconds it takes
     * to complete the zoom animation (smaller number means a faster animation).
     *
     * @return The zoom animation duration in milliseconds
     */
    public long getZoomAnimationDuration() {
        return fZoomAnimationDuration;
    }

    void setZoomAnimationDuration(long duration) {
       fZoomAnimationDuration = duration;
    }

    /**
     * Each zoom action (typically, one mouse-scroll == one zoom action) will
     * increase or decrease the current visible time range by this factor.
     *
     * @return The zoom step
     */
    public double getZoomStep() {
        return fZoomStep;
    }

    void setZoomStep(double step) {
        fZoomStep = step;
    }

    public Font getTextFont() {
        return fTextFont;
    }

    synchronized void setTextFont(Font font) {
        fTextFont = font;
        recomputeEllipsisWidth();
    }

    public String getEllipsisString() {
        return fEllipsisString;
    }

    /* Note, don't use the "…" character, JavaFX seems to not like it */
    synchronized void setEllipsisString(String ellipsisString) {
        fEllipsisString = ellipsisString;
        recomputeEllipsisWidth();
    }

    public double getEllipsisWidth() {
        return fEllipsisWidth;
    }

    private synchronized void recomputeEllipsisWidth() {
        Text text = new Text(getEllipsisString());
        text.setFont(getTextFont());
        text.applyCss();
        fEllipsisWidth = text.getLayoutBounds().getWidth();
    }

}
