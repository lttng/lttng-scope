/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.ust.analysis.debuginfo;

import ca.polymtl.dorsal.libdelorean.IStateSystemReader;
import ca.polymtl.dorsal.libdelorean.StateSystemUtils;
import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;
import ca.polymtl.dorsal.libdelorean.exceptions.StateSystemDisposedException;
import ca.polymtl.dorsal.libdelorean.exceptions.TimeRangeException;
import ca.polymtl.dorsal.libdelorean.interval.StateInterval;
import ca.polymtl.dorsal.libdelorean.statevalue.IntegerStateValue;
import ca.polymtl.dorsal.libdelorean.statevalue.LongStateValue;
import ca.polymtl.dorsal.libdelorean.statevalue.StateValue;
import ca.polymtl.dorsal.libdelorean.statevalue.StringStateValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Wrapper around a state system produced by the {@link UstDebugInfoAnalysis}.
 */
public class UstDebugInfoAnalysisResults {

    private final IStateSystemReader stateSystem;

    public UstDebugInfoAnalysisResults(IStateSystemReader ss) {
        stateSystem = ss;
    }

    /**
     * Return all the binaries that were detected in the trace.
     *
     * @return The binaries (executables or libraries) referred to in the trace.
     */
    public Collection<UstDebugInfoBinaryFile> getAllBinaries() {
        final IStateSystemReader ss = stateSystem;

        final Set<UstDebugInfoBinaryFile> files = new TreeSet<>();
        ImmutableList.Builder<Integer> builder = ImmutableList.builder();
        List<Integer> vpidQuarks = ss.getSubAttributes(IStateSystemReader.ROOT_ATTRIBUTE, false);
        for (Integer vpidQuark : vpidQuarks) {
            builder.addAll(ss.getSubAttributes(vpidQuark, false));
        }
        List<Integer> baddrQuarks = builder.build();

        try {
            for (Integer baddrQuark : baddrQuarks) {
                int buildIdQuark = ss.getQuarkRelative(baddrQuark, UstDebugInfoAnalysisStateProvider.BUILD_ID_ATTRIB);
                int debugLinkQuark = ss.getQuarkRelative(baddrQuark, UstDebugInfoAnalysisStateProvider.DEBUG_LINK_ATTRIB);
                int pathQuark = ss.getQuarkRelative(baddrQuark, UstDebugInfoAnalysisStateProvider.PATH_ATTRIB);
                int isPICQuark = ss.getQuarkRelative(baddrQuark, UstDebugInfoAnalysisStateProvider.IS_PIC_ATTRIB);
                long ts = ss.getStartTime();

                /*
                 * Iterate over each mapping there ever was at this base
                 * address.
                 */
                StateInterval interval = StateSystemUtils.queryUntilNonNullValue(ss, baddrQuark, ts, Long.MAX_VALUE);
                while (interval != null) {
                    ts = interval.getStart();

                    StringStateValue filePathStateValue = (StringStateValue) ss.querySingleState(ts, pathQuark).getStateValue();
                    String filePath = filePathStateValue.getValue();

                    StateValue buildIdStateValue = ss.querySingleState(ts, buildIdQuark).getStateValue();
                    String buildId = unboxStrOrNull(buildIdStateValue);

                    StateValue debuglinkStateValue = ss.querySingleState(ts, debugLinkQuark).getStateValue();
                    String debugLink = unboxStrOrNull(debuglinkStateValue);

                    IntegerStateValue isPICStateValue = (IntegerStateValue) ss.querySingleState(ts, isPICQuark).getStateValue();
                    Boolean isPIC = isPICStateValue.getValue() != 0;

                    files.add(new UstDebugInfoBinaryFile(filePath, buildId, debugLink, isPIC));

                    /*
                     * Go one past the end of the interval, and perform the
                     * query again to find the next mapping at this address.
                     */
                    ts = interval.getEnd() + 1;
                    interval = StateSystemUtils.queryUntilNonNullValue(ss, baddrQuark, ts, Long.MAX_VALUE);
                }
            }
        } catch (AttributeNotFoundException | TimeRangeException | StateSystemDisposedException e) {
            /* Oh well, such is life. */
        }
        return files;
    }

    /**
     * Get the binary file (executable or library) that corresponds to a given
     * instruction pointer, at a given time.
     *
     * @param ts
     *            The timestamp
     * @param vpid
     *            The VPID of the process we are querying for
     * @param ip
     *            The instruction pointer of the trace event. Normally comes
     *            from a 'ip' context.
     * @return A {@link UstDebugInfoLoadedBinaryFile} object, describing the
     *         binary file and its base address.
     */
    @VisibleForTesting
    public @Nullable UstDebugInfoLoadedBinaryFile getMatchingFile(long ts, long vpid, long ip) {
        try {
            final IStateSystemReader ss = stateSystem;

            List<Integer> possibleBaddrQuarks = ss.getQuarks(String.valueOf(vpid), "*"); //$NON-NLS-1$
            List<StateInterval> state = ss.queryFullState(ts);

            /* Get the most probable base address from all the known ones */
            OptionalLong potentialBaddr = possibleBaddrQuarks.stream()
                    .filter(quark -> {
                        /* Keep only currently (at ts) mapped objects. */
                        StateValue value = state.get(quark).getStateValue();
                        return (value instanceof IntegerStateValue
                                && ((IntegerStateValue) value).getValue() == 1);
                    })
                    .map(quark -> ss.getAttributeName(quark.intValue()))
                    .mapToLong(baddrStr -> Long.parseLong(baddrStr))
                    .filter(baddr -> baddr <= ip)
                    .max();

            if (!potentialBaddr.isPresent()) {
                return null;
            }

            long baddr = potentialBaddr.getAsLong();
            final int baddrQuark = ss.getQuarkAbsolute(String.valueOf(vpid),
                                                       String.valueOf(baddr));

            final int memszQuark = ss.getQuarkRelative(baddrQuark, UstDebugInfoAnalysisStateProvider.MEMSZ_ATTRIB);
            final long memsz = ((LongStateValue) state.get(memszQuark).getStateValue()).getValue();

            /* Make sure the 'ip' fits the range of this object. */
            if (!(ip < baddr + memsz)) {
                /*
                 * Not the correct memory range after all. We do not have
                 * information about the library that was loaded here.
                 */
                return null;
            }

            final int pathQuark = ss.getQuarkRelative(baddrQuark, UstDebugInfoAnalysisStateProvider.PATH_ATTRIB);
            String filePath = ((StringStateValue) state.get(pathQuark).getStateValue()).getValue();

            final int buildIdQuark = ss.getQuarkRelative(baddrQuark, UstDebugInfoAnalysisStateProvider.BUILD_ID_ATTRIB);
            StateValue buildIdValue = state.get(buildIdQuark).getStateValue();
            String buildId = unboxStrOrNull(buildIdValue);

            final int debugLinkQuark = ss.getQuarkRelative(baddrQuark, UstDebugInfoAnalysisStateProvider.DEBUG_LINK_ATTRIB);
            StateValue debugLinkValue = state.get(debugLinkQuark).getStateValue();
            String debugLink = unboxStrOrNull(debugLinkValue);

            final int isPicQuark = ss.getQuarkRelative(baddrQuark, UstDebugInfoAnalysisStateProvider.IS_PIC_ATTRIB);
            boolean isPic = ((IntegerStateValue) state.get(isPicQuark).getStateValue()).getValue() != 0;

            return new UstDebugInfoLoadedBinaryFile(baddr, filePath, buildId, debugLink, isPic);

        } catch (AttributeNotFoundException | TimeRangeException | StateSystemDisposedException e) {
            /* Either the data is not available yet, or incomplete. */
            return null;
        }
    }

    private static @Nullable String unboxStrOrNull(StateValue value) {
        return (value.isNull() ? null : ((StringStateValue) value).getValue());
    }
}
