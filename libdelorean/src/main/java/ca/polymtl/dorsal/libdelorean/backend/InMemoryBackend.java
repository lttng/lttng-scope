/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 * Copyright (C) 2013-2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package ca.polymtl.dorsal.libdelorean.backend;

import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;
import ca.polymtl.dorsal.libdelorean.exceptions.TimeRangeException;
import ca.polymtl.dorsal.libdelorean.interval.StateInterval;
import ca.polymtl.dorsal.libdelorean.statevalue.StateValue;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * State history back-end that stores its intervals in RAM only. It cannot be
 * saved to disk, which means we need to rebuild it every time we re-open a
 * trace. But it's relatively quick to build, so this shouldn't be a problem in
 * most cases.
 *
 * This should only be used with very small state histories (and/or, very small
 * traces). Since it's stored in standard Collections, it's limited to 2^31
 * intervals.
 *
 * @author Alexandre Montplaisir
 */
class InMemoryBackend implements IStateHistoryBackend {

    /**
     * We need to compare the end time and the attribute, because we can have 2
     * intervals with the same end time (for different attributes). And TreeSet
     * needs a unique "key" per element.
     */
    private static final Comparator<StateInterval> END_COMPARATOR = Comparator.comparing(StateInterval::getEnd).thenComparing(StateInterval::getAttribute);

    private final @NotNull String ssid;
    private final TreeSet<StateInterval> intervals;
    private final long startTime;

    private volatile long latestTime;

    /**
     * Constructor
     *
     * @param ssid
     *            The state system's ID
     * @param startTime
     *            The start time of this interval store
     */
    public InMemoryBackend(@NotNull String ssid, long startTime) {
        this.ssid = ssid;
        this.startTime = startTime;
        this.latestTime = startTime;
        this.intervals = new TreeSet<>(END_COMPARATOR);
    }

    @Override
    public String getSSID() {
        return ssid;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public long getEndTime() {
        return latestTime;
    }

    @Override
    public void insertPastState(long stateStartTime, long stateEndTime,
            int quark, StateValue value) throws TimeRangeException {
        /* Make sure the passed start/end times make sense */
        if (stateStartTime > stateEndTime || stateStartTime < startTime) {
            throw new TimeRangeException(ssid + " Interval Start:" + stateStartTime + ", Interval End:" + stateEndTime + ", Backend Start:" + startTime); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        StateInterval interval = new StateInterval(stateStartTime, stateEndTime, quark, value);

        /* Add the interval into the tree */
        synchronized (intervals) {
            intervals.add(interval);
        }

        /* Update the "latest seen time" */
        if (stateEndTime > latestTime) {
            latestTime = stateEndTime;
        }
    }

    @Override
    public void doQuery(List<StateInterval> currentStateInfo, long t)
            throws TimeRangeException {
        if (!checkValidTime(t)) {
            throw new TimeRangeException(ssid + " Time:" + t + ", Start:" + startTime + ", End:" + latestTime); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        /*
         * The intervals are sorted by end time, so we can binary search to get
         * the first possible interval, then only compare their start times.
         */
        synchronized (intervals) {
            Iterator<StateInterval> iter = serachforEndTime(intervals, t);
            for (int modCount = 0; iter.hasNext() && modCount < currentStateInfo.size();) {
                StateInterval entry = iter.next();
                final long entryStartTime = entry.getStart();
                if (entryStartTime <= t) {
                    /* Add this interval to the returned values */
                    currentStateInfo.set(entry.getAttribute(), entry);
                    modCount++;
                }
            }
        }
    }

    @Override
    public StateInterval doSingularQuery(long t, int attributeQuark)
            throws TimeRangeException, AttributeNotFoundException {
        if (!checkValidTime(t)) {
            throw new TimeRangeException(ssid + " Time:" + t + ", Start:" + startTime + ", End:" + latestTime); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        /*
         * The intervals are sorted by end time, so we can binary search to get
         * the first possible interval, then only compare their start times.
         */
        synchronized (intervals) {
            Iterator<StateInterval> iter = serachforEndTime(intervals, t);
            while (iter.hasNext()) {
                StateInterval entry = iter.next();
                final boolean attributeMatches = (entry.getAttribute() == attributeQuark);
                final long entryStartTime = entry.getStart();
                if (attributeMatches) {
                    if (entryStartTime <= t) {
                        /* This is the droid we are looking for */
                        return entry;
                    }
                }
            }
        }
        throw new AttributeNotFoundException(ssid + " Quark:" + attributeQuark); //$NON-NLS-1$
    }

    private boolean checkValidTime(long t) {
        if (t >= startTime && t <= latestTime) {
            return true;
        }
        return false;
    }

    @Override
    public void finishBuilding(long endTime) throws TimeRangeException {
        /* Nothing to do */
    }

    @Override
    public FileInputStream supplyAttributeTreeReader() {
        /* Saving to disk not supported */
        return null;
    }

    @Override
    public File supplyAttributeTreeWriterFile() {
        /* Saving to disk not supported */
        return null;
    }

    @Override
    public long supplyAttributeTreeWriterFilePosition() {
        /* Saving to disk not supported */
        return -1;
    }

    @Override
    public void removeFiles() {
        /* Nothing to do */
    }

    @Override
    public void dispose() {
        /* Nothing to do */
    }

    private static Iterator<StateInterval> serachforEndTime(TreeSet<StateInterval> tree, long time) {
        StateInterval dummyInterval = new StateInterval(-1, time, -1, StateValue.nullValue());
        StateInterval myInterval = tree.lower(dummyInterval);
        if (myInterval == null) {
            return tree.iterator();
        }
        final SortedSet<StateInterval> tailSet = tree.tailSet(myInterval);
        Iterator<StateInterval> retVal = tailSet.iterator();
        retVal.next();
        return retVal;
    }

    // FIXME Needs to be implemented because of https://youtrack.jetbrains.com/issue/KT-4779
    @Override
    public void doPartialQuery(long t, @NotNull Set<Integer> quarks, @NotNull Map<Integer, StateInterval> results) {
        quarks.forEach(quark -> {
            StateInterval interval = doSingularQuery(t, quark);
            if (interval != null) {
                results.put(quark, interval);
            }
        });
    }
}
