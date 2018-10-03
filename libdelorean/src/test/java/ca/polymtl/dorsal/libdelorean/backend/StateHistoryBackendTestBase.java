/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 * Copyright (C) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package ca.polymtl.dorsal.libdelorean.backend;

import ca.polymtl.dorsal.libdelorean.interval.StateInterval;
import ca.polymtl.dorsal.libdelorean.statevalue.StateValue;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;


import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Abstract class to test implementations of the {@link IStateHistoryBackend}
 * interface.
 *
 * @author Patrick Tasse
 * @author Alexandre Montplaisir
 */
// TODO Define test parameters at the class level once this is implemented in JUnit 5
public abstract class StateHistoryBackendTestBase {

    private static final long START_TIME = 0;
    private static final long END_TIME = 1000;

    /**
     * The backend fixture to use in tests
     */
    protected IStateHistoryBackend fBackend;

    /**
     * Test parameters
     *
     * @return Parameter arrays
     */
    public static Stream<Arguments> parameters() {
        /*
         * Test the full query method by filling a small tree with intervals placed in a
         * "stair-like" fashion, like this:
         *
         * <pre>
         * |x----x----x---x|
         * |xx----x----x--x|
         * |x-x----x----x-x|
         * |x--x----x----xx|
         * |      ...      |
         * </pre>
         */
        List<StateInterval> cascadingIntervals = new ArrayList<>();
        int cacadingNbAttributes = 10;
        {
            long duration = 10;
            for (long t = START_TIME + 1; t <= END_TIME + duration; t++) {
                cascadingIntervals.add(new StateInterval(
                        Math.max(START_TIME, t - duration),
                        Math.min(END_TIME, t - 1),
                        (int) t % cacadingNbAttributes,
                        StateValue.newValueLong(t)));
            }
        }

        /*
         * Test the full query method by filling a small backend with intervals that
         * take the full time range, like this:
         *
         * <pre>
         * |x-------------x|
         * |x-------------x|
         * |x-------------x|
         * |x-------------x|
         * |      ...      |
         * </pre>
         */
        List<StateInterval> fullWidthIntervals = new ArrayList<>();
        int fullWidthNbdAttributes = 1000;
        {
            for (int attr = 0; attr < fullWidthNbdAttributes; attr++) {
                fullWidthIntervals.add(new StateInterval(
                        START_TIME,
                        END_TIME,
                        attr,
                        StateValue.newValueLong(attr)));
            }
        }

        List<StateInterval> oneInterval = Collections.singletonList(new StateInterval(START_TIME, END_TIME, 0, StateValue.nullValue()));

        return Stream.of(
                Arguments.of("one-interval", oneInterval, 1),
                Arguments.of("cascading", cascadingIntervals, cacadingNbAttributes),
                Arguments.of("full-width", fullWidthIntervals, fullWidthNbdAttributes)
        );
    }

    /**
     * Test setup
     */
    void setup(List<StateInterval> intervals) {
        final IStateHistoryBackend backend = instantiateBackend(START_TIME);

        /* Insert the intervals into the backend */
        intervals.forEach(interval -> {
            backend.insertPastState(interval.getStart(),
                    interval.getEnd(),
                    interval.getAttribute(),
                    interval.getStateValue());
        });
        backend.finishBuilding(Math.max(END_TIME, backend.getEndTime()));

        fBackend = backend;

        afterInsertionCb();
    }

    /**
     * Test cleanup
     */
    @AfterEach
    public void teardown() {
        if (fBackend != null) {
            fBackend.dispose();
        }
    }


    protected abstract IStateHistoryBackend instantiateBackend(long startTime);

    protected abstract void afterInsertionCb();

    /**
     * Test the full query method
     * {@link IStateHistoryBackend#doQuery(List, long)}, by filling a small tree
     * with the specified intervals and then querying at every single timestamp,
     * making sure all, and only, the expected intervals are returned.
     */
    @ParameterizedTest
    @MethodSource("parameters")
    void testFullQuery(String name, List<StateInterval> intervals, int nbAttributes) {
        setup(intervals);

        IStateHistoryBackend backend = fBackend;
        assertNotNull(backend);

        /*
         * Query at every valid time stamp, making sure only the expected intervals are
         * returned.
         */
        for (long t = backend.getStartTime(); t <= backend.getEndTime(); t++) {
            final long ts = t;

            List<StateInterval> stateInfo = new ArrayList<>(nbAttributes);
            IntStream.range(0, nbAttributes).forEach(i -> stateInfo.add(null));
            backend.doQuery(stateInfo, t);

            stateInfo.forEach(interval -> {
                assertNotNull(interval);
                assertTrue(interval.intersects(ts));
            });
        }

        assertEquals(START_TIME, backend.getStartTime());
        assertEquals(END_TIME, backend.getEndTime());
    }

    /**
     * Test the partial query method (@link IStateHistoryBackend#doPartialQuery}
     * with one quark into the passed Set.
     */
    @ParameterizedTest
    @MethodSource("parameters")
    void testPartialQueryOneQuark(String name, List<StateInterval> intervals, int nbAttributes) {
        setup(intervals);

        IStateHistoryBackend backend = fBackend;
        assertNotNull(backend);

        final int quark = 0;
        final Set<Integer> quarks = ImmutableSet.of(quark);

        for (long t = backend.getStartTime(); t <= backend.getEndTime(); t++) {
            Map<Integer, StateInterval> results = new HashMap<>();
            backend.doPartialQuery(t, quarks, results);

            assertEquals(1, results.size());
            StateInterval interval = results.get(quark);
            assertNotNull(interval);
            assertTrue(interval.intersects(t), interval.toString() + " does not intersect timestamp " + t);
        }
    }

    /**
     * Test the partial query method (@link IStateHistoryBackend#doPartialQuery}
     * with some but not all quarks into the requested set.
     */
    @ParameterizedTest
    @MethodSource("parameters")
    void testPartialQuerySomeQuarks(String name, List<StateInterval> intervals, int nbAttributes) {
        setup(intervals);

        IStateHistoryBackend backend = fBackend;
        assertNotNull(backend);

        /* Take only half the quarks, using even numbers. */
        final Set<Integer> quarks = IntStream.iterate(0, i -> i + 2).limit(nbAttributes / 2)
                .boxed()
                .collect(ImmutableSet.toImmutableSet());

        for (long t = backend.getStartTime(); t <= backend.getEndTime(); t++) {
            final long ts = t;
            Map<Integer, StateInterval> results = new HashMap<>();
            backend.doPartialQuery(t, quarks, results);

            assertEquals(quarks.size(), results.size());
            results.values().forEach(interval -> {
                assertNotNull(interval);
                assertTrue(interval.intersects(ts), interval.toString() + " does not intersect timestamp " + ts);
            });
        }
    }

    /**
     * Test the partial query method (@link IStateHistoryBackend#doPartialQuery}
     * with all quarks into the requested set.
     */
    @ParameterizedTest
    @MethodSource("parameters")
    void testPartialQueryAllQuarks(String name, List<StateInterval> intervals, int nbAttributes) {
        setup(intervals);

        IStateHistoryBackend backend = fBackend;
        assertNotNull(backend);

        /* Generate a set of all the existing quarks. */
        final Set<Integer> quarks = IntStream.range(0, nbAttributes)
                .boxed()
                .collect(ImmutableSet.toImmutableSet());

        for (long t = backend.getStartTime(); t <= backend.getEndTime(); t++) {
            final long ts = t;
            Map<Integer, StateInterval> results = new HashMap<>();
            backend.doPartialQuery(t, quarks, results);

            assertEquals(quarks.size(), results.size());
            results.values().forEach(interval -> {
                assertNotNull(interval);
                assertTrue(interval.intersects(ts), interval.toString() + " does not intersect timestamp " + ts);
            });
        }
    }

    /**
     * Test that the backend time is set correctly.
     */
    @ParameterizedTest
    @MethodSource("parameters")
    void testBackendEndTime(String name, List<StateInterval> intervals, int nbAttributes) {
        setup(intervals);

        long maxIntervalEndTime = intervals.stream()
                .mapToLong(StateInterval::getEnd)
                .max().getAsLong();

        long expectedEndTime = Math.max(maxIntervalEndTime, END_TIME);
        assertEquals(expectedEndTime, fBackend.getEndTime());
    }
}
