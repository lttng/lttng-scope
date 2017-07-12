/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.lttng.kernel.core.views.timegraph.threads;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.lttng.scope.lttng.kernel.core.analysis.os.Attributes;
import org.lttng.scope.lttng.kernel.core.analysis.os.KernelAnalysis;
import org.lttng.scope.lttng.kernel.core.tests.shared.LttngKernelTestTraceUtils;
import org.lttng.scope.lttng.kernel.core.trace.LttngKernelTrace;
import org.lttng.scope.tmf2.project.core.JabberwockyProjectManager;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateInterval;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeElement;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeRender;

import com.efficios.jabberwocky.common.TimeRange;
import com.efficios.jabberwocky.project.ITraceProject;
import com.google.common.collect.Iterables;

import ca.polymtl.dorsal.libdelorean.ITmfStateSystem;
import ca.polymtl.dorsal.libdelorean.StateSystemUtils;
import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;
import ca.polymtl.dorsal.libdelorean.exceptions.StateSystemDisposedException;
import ca.polymtl.dorsal.libdelorean.interval.ITmfStateInterval;
import ca.polymtl.dorsal.libdelorean.statevalue.ITmfStateValue;

/**
 * Tests for {@link ThreadsModelProvider}.
 *
 * @author Alexandre Montplaisir
 */
public class ThreadsModelProviderTest {

    /** Timeout the tests after 2 minutes */
    @Rule
    public TestRule timeoutRule = new Timeout(2, TimeUnit.MINUTES);

    private static final long NANOS_PER_SECOND = 1000000000L;

    private static final CtfTestTrace TEST_TRACE = CtfTestTrace.KERNEL;

    private static @Nullable ITmfTrace sfTrace;
    private static @Nullable ITmfStateSystem sfSS;

    private ThreadsModelProvider provider = new ThreadsModelProvider();
    {
        provider.disableFilterMode(0);
    }

    /**
     * Test class setup
     */
    @Before
    public void setupClass() {
        LttngKernelTrace trace = LttngKernelTestTraceUtils.getTrace(TEST_TRACE);
        trace.indexTrace(true);

        ITraceProject<?, ?> project = trace.getJwProject();
        JabberwockyProjectManager mgr = JabberwockyProjectManager.instance();
        ITmfStateSystem ss = (ITmfStateSystem) mgr.getAnalysisResults(project, KernelAnalysis.instance());
        assertNotNull(ss);

        sfTrace = trace;
        sfSS = ss;

        provider.setTrace(trace);
    }

    /**
     * Test class teardown
     */
    @After
    public void teardownClass() {
        if (sfTrace != null) {
            /* Trace's dispose will dispose its state systems */
            sfTrace.dispose();
        }

        provider.setTrace(null);
    }

    /**
     * Check that the info in a render for the first second of the trace matches
     * the corresponding info found in the state system.
     */
    @Test
    public void test1s() {
        try {
            final ITmfTrace trace = sfTrace;
            final ITmfStateSystem ss = sfSS;
            assertNotNull(trace);
            assertNotNull(ss);

            final long start = trace.getStartTime().toNanos();
            final long end = start + 1 * NANOS_PER_SECOND;
            final TimeRange range = TimeRange.of(start, end);


            /* Check that the list of attributes (tree render) are the same */
            TimeGraphTreeRender treeRender = provider.getTreeRender();
            List<TimeGraphTreeElement> treeElems = treeRender.getAllTreeElements();

            List<String> tidsFromRender = treeElems.stream()
                    .filter(e -> e instanceof ThreadsTreeElement).map(e -> (ThreadsTreeElement) e)
                    .mapToInt(ThreadsTreeElement::getTid)
                    .mapToObj(tid -> String.valueOf(tid))
                    .sorted()
                    .collect(Collectors.toList());

            int threadsQuark = ss.getQuarkAbsolute(Attributes.THREADS);
            List<String> tidsFromSS = ss.getSubAttributes(threadsQuark, false).stream()
                    .map(quark -> ss.getAttributeName(quark))
                    .map(name -> {
                        if (name.startsWith(Attributes.THREAD_0_PREFIX)) {
                            return "0";
                        }
                        return name;
                    })
                    .sorted()
                    .collect(Collectors.toList());

            assertEquals(tidsFromSS, tidsFromRender);
            // TODO Also verify against known hard-coded list


            /* Check that the state intervals are the same */
            List<String> tidsInSS = ss.getSubAttributes(threadsQuark, false).stream()
                    .map(ss::getAttributeName)
                    .sorted()
                    .collect(Collectors.toList());

            for (String tid : tidsInSS) {
                int threadQuark = ss.getQuarkRelative(threadsQuark, tid);
                List<ITmfStateInterval> intervalsFromSS =
                        StateSystemUtils.queryHistoryRange(ss, threadQuark, start, end);

                TimeGraphTreeElement elem = treeElems.stream()
                        .filter(e -> e instanceof ThreadsTreeElement).map(e -> (ThreadsTreeElement) e)
                        .filter(e -> e.getSourceQuark() == threadQuark)
                        .findFirst()
                        .get();

                TimeGraphStateRender stateRender = provider.getStateProvider().getStateRender(elem, range, 1, null);
                List<TimeGraphStateInterval> intervalsFromRender = stateRender.getStateIntervals();

                verifySameIntervals(intervalsFromSS, intervalsFromRender);
                // TODO Also verify against known hard-coded list
            }

        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Verify that for a known time range, all generated intervals are
     * contiguous but of a different states (multi-states are included in
     * there).
     */
    @Test
    public void testMultiStates() {
        TimeRange range = TimeRange.of(1332170683505733202L, 1332170683603572392L);
        String treeElemName = "0/0 - swapper";
        long viewWidth = 1000;

        long resolution = range.getDuration() / viewWidth;

        TimeGraphTreeElement treeElem = provider.getTreeRender().getAllTreeElements().stream()
                .filter(elem -> elem.getName().equals(treeElemName))
                .findFirst().get();
        TimeGraphStateRender stateRender = provider.getStateProvider().getStateRender(treeElem, range, resolution, null);
        List<TimeGraphStateInterval> intervals = stateRender.getStateIntervals();

        assertTrue(intervals.size() > 2);

        for (int i = 1; i < intervals.size(); i++) {
            TimeGraphStateInterval interval1 = intervals.get(i - 1);
            TimeGraphStateInterval interval2 = intervals.get(i);

            assertEquals(interval1.getEndTime() + 1, interval2.getStartTime());
            assertNotEquals(interval1.getStateName(), interval2.getStateName());
        }
    }

    /**
     * Make sure that if multi-states are present at the beginning or end of a
     * time graph render, they actually start/end at the same timestamps as the
     * full state model.
     */
    @Test
    public void testBounds() {
        final ITmfTrace trace = sfTrace;
        final ITmfStateSystem ss = sfSS;
        assertNotNull(trace);
        assertNotNull(ss);

        /*
         * Note that here, the range of the query is the full range of the
         * trace, so the start/end times of the full state system should match
         * the ones in the model. This might not always be the case with
         * multi-states at the beginning/end, since those may have synthetic
         * start/end times.
         */
        TimeRange range = TimeRange.of(trace.getStartTime().toNanos(), trace.getEndTime().toNanos());
        String treeElemName = "0/0 - swapper";
        long viewWidth = 1000;

        long resolution = range.getDuration() / viewWidth;

        /* Get the intervals from the model */
        TimeGraphTreeElement treeElem = provider.getTreeRender().getAllTreeElements().stream()
                .filter(elem -> elem.getName().equals(treeElemName))
                .findFirst().get();
        TimeGraphStateRender stateRender = provider.getStateProvider().getStateRender(treeElem, range, resolution, null);
        List<TimeGraphStateInterval> intervalsFromRender = stateRender.getStateIntervals();

        /* Get the intervals from the state system */
        int threadsQuark = ss.getQuarkAbsolute(Attributes.THREADS);
        int threadQuark = ss.getQuarkRelative(threadsQuark, "0_0");
        List<ITmfStateInterval> intervalsFromSS;
        try {
            intervalsFromSS = StateSystemUtils.queryHistoryRange(ss, threadQuark, range.getStartTime(), range.getEndTime());
        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            fail(e.getMessage());
            return;
        }

        /* Check that the first intervals start at the same timestamp. */
        long modelStart = intervalsFromRender.get(0).getStartTime();
        long ssStart = intervalsFromSS.get(0).getStartTime();
        assertEquals(ssStart, modelStart);

        /* Check that the last intervals end at the same timestamp too. */
        long modelEnd = Iterables.getLast(intervalsFromRender).getEndTime();
        long ssEnd = Iterables.getLast(intervalsFromSS).getEndTime();
        assertEquals(ssEnd, modelEnd);
    }

    private static void verifySameIntervals(List<ITmfStateInterval> ssIntervals,
            List<TimeGraphStateInterval> renderIntervals) {
        assertEquals(ssIntervals.size(), renderIntervals.size());

        for (int i = 0; i < ssIntervals.size(); i++) {
            ITmfStateInterval ssInterval = ssIntervals.get(i);
            TimeGraphStateInterval renderInterval = renderIntervals.get(i);

            assertEquals(ssInterval.getStartTime(), renderInterval.getStartEvent().getTimestamp());
            assertEquals(ssInterval.getEndTime(), renderInterval.getEndEvent().getTimestamp());

            ITmfStateValue stateValue = ssInterval.getStateValue();
            String stateName = ThreadsModelStateProvider.stateValueToStateDef(stateValue).getName();
            assertEquals(stateName, renderInterval.getStateName());
        }
    }
}
