/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.arrows.ITimeGraphModelArrowProvider;
import org.lttng.scope.tmf2.views.ui.jfx.Arrow;

/**
 * {@link TimeGraphWidget} test suite testing arrow-related operations.
 */
public class TimeGraphWidgetArrowsTest extends TimeGraphWidgetTestBase {

    /**
     * Test setup
     */
    @Before
    public void setup() {
        /* Disable all arrow providers initially */
        getWidget().getControl().getModelRenderProvider().getArrowProviders()
                .forEach(ap -> ap.enabledProperty().set(false));

        /*
         * Do a paint first, ensuring that the tree pane is painted and the tree
         * elements are available.
         */
        repaint();
    }

    /**
     * Test that no arrows are shown when all providers are disabled.
     */
    @Test
    public void testArrowDefault() {
        Collection<Arrow> arrows = getWidget().getArrowLayer().getRenderedArrows();
        assertTrue(arrows.isEmpty());
    }

    /**
     * Test that enabling one series shows those arrows, and only those.
     */
    @Test
    public void testArrowOneSeries() {
        ITimeGraphModelArrowProvider providerRed = getWidget().getControl().getModelRenderProvider().getArrowProviders().stream()
                .filter(ap -> ap.getArrowSeries().getSeriesName().equals(StubModelArrowProvider1.SERIES_NAME))
                .findFirst().get();
        providerRed.enabledProperty().set(true);

        repaint();

        Collection<Arrow> arrows = getWidget().getArrowLayer().getRenderedArrows();
        assertEquals(3, arrows.size());
    }

    /**
     * Test enabling all series
     */
    @Test
    public void testArrowsAllSeries() {
        getWidget().getControl().getModelRenderProvider().getArrowProviders()
                .forEach(ap -> ap.enabledProperty().set(true));

        repaint();

        Collection<Arrow> arrows = getWidget().getArrowLayer().getRenderedArrows();
        assertEquals(5, arrows.size());
    }

    /**
     * Test enabling a series, then disabling it
     */
    @Test
    public void testArrowsEnableThenDisable() {
        getWidget().getControl().getModelRenderProvider().getArrowProviders()
                .forEach(ap -> ap.enabledProperty().set(true));

        repaint();

        ITimeGraphModelArrowProvider providerRed = getWidget().getControl().getModelRenderProvider().getArrowProviders().stream()
                .filter(ap -> ap.getArrowSeries().getSeriesName().equals(StubModelArrowProvider1.SERIES_NAME))
                .findFirst().get();
        providerRed.enabledProperty().set(false);

        repaint();

        Collection<Arrow> arrows = getWidget().getArrowLayer().getRenderedArrows();
        assertEquals(2, arrows.size());
    }

}
