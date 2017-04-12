/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph;

import static java.util.Objects.requireNonNull;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Debug options for the {@link TimeGraphWidget}. Advanced users or unit
 * tests might want to modify these.
 *
 * @author Alexandre Montplaisir
 */
public class DebugOptions {

    public static class ConfigOption<T> extends SimpleObjectProperty<T> {

        private final T fDefaultValue;

        public ConfigOption(T defaultValue) {
            super(defaultValue);
            fDefaultValue = defaultValue;
        }

        public T getDefaultValue() {
            return fDefaultValue;
        }

        public void resetToDefault() {
            set(fDefaultValue);
        }
    }

    /**
     * Constructor using the default options
     */
    public DebugOptions() {
        recomputeEllipsisWidth();
    }

    // ------------------------------------------------------------------------
    // General options
    // ------------------------------------------------------------------------

    /**
     * Painting flag. Indicates if automatic redrawing of the view is enabled
     */
    public final ConfigOption<Boolean> isPaintingEnabled = new ConfigOption<>(true);

    /**
     * Entry padding. Number of tree elements to print above *and* below the
     * visible range
     */
    public final ConfigOption<Integer> entryPadding = new ConfigOption<>(5);

    /**
     * How much "padding" around the current visible window, on the left and
     * right, should be pre-rendered. Expressed as a fraction of the current
     * window (for example, 1.0 would render one "page" on each side).
     */
    public final ConfigOption<Double> renderRangePadding = new ConfigOption<>(0.1);

    /**
     * Time between UI updates, in milliseconds
     */
    public final ConfigOption<Integer> uiUpdateDelay = new ConfigOption<>(250);

    /**
     * Whether the view should respond to vertical or horizontal scrolling
     * actions.
     */
    public final ConfigOption<Boolean> isScrollingListenersEnabled = new ConfigOption<>(true);

    // ------------------------------------------------------------------------
    // Loading overlay
    // ------------------------------------------------------------------------

    public final ConfigOption<Boolean> isLoadingOverlayEnabled = new ConfigOption<>(true);

    public final ConfigOption<Color> loadingOverlayColor = new ConfigOption<>(requireNonNull(Color.GRAY));

    public final ConfigOption<Double> loadingOverlayFullOpacity = new ConfigOption<>(0.3);
    public final ConfigOption<Double> loadingOverlayTransparentOpacity = new ConfigOption<>(0.0);

    public final ConfigOption<Double> loadingOverlayFadeInDuration = new ConfigOption<>(100.0);
    public final ConfigOption<Double> loadingOverlayFadeOutDuration = new ConfigOption<>(100.0);

    // ------------------------------------------------------------------------
    // Zoom animation
    // ------------------------------------------------------------------------

    /**
     * The zoom animation duration, which is the amount of milliseconds it takes
     * to complete the zoom animation (smaller number means a faster animation).
     */
    public final ConfigOption<Integer> zoomAnimationDuration = new ConfigOption<>(50);

    /**
     * Each zoom action (typically, one mouse-scroll == one zoom action) will
     * increase or decrease the current visible time range by this factor.
     */
    public final ConfigOption<Double> zoomStep = new ConfigOption<>(0.08);

    // ------------------------------------------------------------------------
    // State rectangles
    // ------------------------------------------------------------------------

    public final ConfigOption<Double> stateIntervalOpacity = new ConfigOption<>(1.0);

    public final ConfigOption<Paint> multiStatePaint;
    {
        Stop[] stops = new Stop[] { new Stop(0, Color.BLACK), new Stop(1, Color.WHITE) };
        LinearGradient lg = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);
        multiStatePaint = new ConfigOption<>(lg);
    }

    // ------------------------------------------------------------------------
    // State labels
    // ------------------------------------------------------------------------

    public final ConfigOption<Font> stateLabelFont = new ConfigOption<Font>(requireNonNull(new Text().getFont())) {
        @Override
        public void set(Font value) {
            super.set(value);
            recomputeEllipsisWidth();
        }
    };

    public static final String ELLIPSIS_STRING = "..."; //$NON-NLS-1$

    private transient double fEllipsisWidth;

    public double getEllipsisWidth() {
        return fEllipsisWidth;
    }

    private synchronized void recomputeEllipsisWidth() {
        Text text = new Text(ELLIPSIS_STRING);
        text.setFont(stateLabelFont.get());
        text.applyCss();
        fEllipsisWidth = text.getLayoutBounds().getWidth();
    }

    // ------------------------------------------------------------------------
    // Tooltips
    // ------------------------------------------------------------------------

    public final ConfigOption<Font> toolTipFont = new ConfigOption<>(Font.font(14));

    public final ConfigOption<Color> toolTipFontFill = new ConfigOption<>(requireNonNull(Color.WHITE));

}
