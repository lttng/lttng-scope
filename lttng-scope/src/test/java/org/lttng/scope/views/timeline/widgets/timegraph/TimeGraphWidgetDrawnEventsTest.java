///*
// * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
// *
// * All rights reserved. This program and the accompanying materials are
// * made available under the terms of the Eclipse Public License v1.0 which
// * accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// */
//
//package org.lttng.scope.ui.timeline.widgets.timegraph;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
//import java.util.Collection;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.lttng.scope.ui.timeline.widgets.timegraph.StubDrawnEventProviders.StubDrawnEventProvider1;
//import org.lttng.scope.ui.timeline.widgets.timegraph.StubDrawnEventProviders.StubDrawnEventProvider2;
//
//import com.efficios.jabberwocky.views.timegraph.model.provider.drawnevents.ITimeGraphDrawnEventProvider;
//import com.efficios.jabberwocky.views.timegraph.model.provider.drawnevents.TimeGraphDrawnEventProviderManager;
//
//import javafx.scene.shape.Shape;
//
///**
// * {@link TimeGraphWidget} test suite testing drawn event-related operations.
// */
//public class TimeGraphWidgetDrawnEventsTest extends TimeGraphWidgetTestBase {
//
//    private static final TimeGraphDrawnEventProviderManager MANAGER = TimeGraphDrawnEventProviderManager.instance();
//
//    /**
//     * Test setup
//     */
//    @Before
//    public void setup() {
//        MANAGER.getRegisteredProviders().clear();
//        repaint();
//    }
//
//    /**
//     * Test that with no provider at all, there is no event drawn.
//     */
//    @Test
//    public void testNoProvider() {
//        Collection<Shape> events = getRenderedEvents();
//        assertTrue(events.isEmpty());
//    }
//
//    /**
//     * Test one registered and enabled provider.
//     */
//    @Test
//    public void testRegisteredAndEnabledProvider() {
//        ITimeGraphDrawnEventProvider provider = new StubDrawnEventProvider1();
//        MANAGER.getRegisteredProviders().add(provider);
//        provider.enabledProperty().set(true);
//
//        repaint();
//
//        Collection<Shape> events = getRenderedEvents();
//        assertEquals(StubDrawnEventProvider1.NB_SYMBOLS, events.size());
//    }
//
//    /**
//     * Test that a provider that is registered but not enabled does not paint
//     * its events.
//     */
//    @Test
//    public void testEnabledButNotRegisteredProvider() {
//        ITimeGraphDrawnEventProvider provider = new StubDrawnEventProvider1();
//        provider.enabledProperty().set(true);
//
//        repaint();
//
//        Collection<Shape> events = getRenderedEvents();
//        assertTrue(events.isEmpty());
//    }
//
//    /**
//     * Test that a provider that is not registered but (somehow) enabled does
//     * not paint its event.
//     */
//    @Test
//    public void testRegisteredButNotEnabledProvider() {
//        ITimeGraphDrawnEventProvider provider1 = new StubDrawnEventProvider1();
//        ITimeGraphDrawnEventProvider provider2 = new StubDrawnEventProvider2();
//        MANAGER.getRegisteredProviders().add(provider1);
//        MANAGER.getRegisteredProviders().add(provider2);
//        provider1.enabledProperty().set(false);
//        provider2.enabledProperty().set(true);
//
//        repaint();
//
//        Collection<Shape> events = getRenderedEvents();
//        assertEquals(StubDrawnEventProvider2.NB_SYMBOLS, events.size());
//    }
//
//    /**
//     * Test several enabled+registered providers at the same time. They should
//     * all paint their own events.
//     */
//    @Test
//    public void testManyProviders() {
//        ITimeGraphDrawnEventProvider provider1 = new StubDrawnEventProvider1();
//        ITimeGraphDrawnEventProvider provider2 = new StubDrawnEventProvider2();
//        MANAGER.getRegisteredProviders().add(provider1);
//        MANAGER.getRegisteredProviders().add(provider2);
//        provider1.enabledProperty().set(true);
//        provider2.enabledProperty().set(true);
//
//        repaint();
//
//        Collection<Shape> events = getRenderedEvents();
//        assertEquals(StubDrawnEventProvider1.NB_SYMBOLS + StubDrawnEventProvider2.NB_SYMBOLS,
//                events.size());
//    }
//
//    /**
//     * Test changing the 'enabled' property of a provider from false to true.
//     * This should result in new events being painted.
//     */
//    @Test
//    public void testEnabling() {
//        ITimeGraphDrawnEventProvider provider = new StubDrawnEventProvider1();
//        MANAGER.getRegisteredProviders().add(provider);
//        provider.enabledProperty().set(false);
//        repaint();
//
//        Collection<Shape> events = getRenderedEvents();
//        assertTrue(events.isEmpty());
//
//        provider.enabledProperty().set(true);
//        repaint();
//
//        events = getRenderedEvents();
//        assertEquals(StubDrawnEventProvider1.NB_SYMBOLS, events.size());
//    }
//
//    /**
//     * Test changing the 'enabled' property of a provider from true to false.
//     * This should remove its events from the view.
//     */
//    @Test
//    public void testDisabling() {
//        ITimeGraphDrawnEventProvider provider = new StubDrawnEventProvider1();
//        MANAGER.getRegisteredProviders().add(provider);
//
//        provider.enabledProperty().set(true);
//        repaint();
//
//        Collection<Shape> events = getRenderedEvents();
//        assertEquals(StubDrawnEventProvider1.NB_SYMBOLS, events.size());
//
//        provider.enabledProperty().set(false);
//        repaint();
//
//        events = getRenderedEvents();
//        assertTrue(events.isEmpty());
//    }
//
//    private Collection<Shape> getRenderedEvents() {
//        return getWidget().getDrawnEventLayer().getRenderedEvents();
//    }
//}