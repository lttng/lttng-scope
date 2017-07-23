/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.lttng.ust.core.analysis.debuginfo;

import java.util.Collection;
import java.util.List;
import java.util.OptionalLong;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import ca.polymtl.dorsal.libdelorean.ITmfStateSystem;
import ca.polymtl.dorsal.libdelorean.StateSystemUtils;
import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;
import ca.polymtl.dorsal.libdelorean.exceptions.StateSystemDisposedException;
import ca.polymtl.dorsal.libdelorean.exceptions.TimeRangeException;
import ca.polymtl.dorsal.libdelorean.interval.ITmfStateInterval;
import ca.polymtl.dorsal.libdelorean.statevalue.ITmfStateValue;

/**
 * Wrapper around a state system produced by the {@link UstDebugInfoAnalysis}.
 */
public class UstDebugInfoAnalysisResults {

    private final ITmfStateSystem stateSystem;

    public UstDebugInfoAnalysisResults(ITmfStateSystem ss) {
        stateSystem = ss;
    }

    /**
     * Return all the binaries that were detected in the trace.
     *
     * @return The binaries (executables or libraries) referred to in the trace.
     */
    public Collection<UstDebugInfoBinaryFile> getAllBinaries() {
        final ITmfStateSystem ss = stateSystem;

        final @NonNull Set<UstDebugInfoBinaryFile> files = new TreeSet<>();
        ImmutableList.Builder<Integer> builder = ImmutableList.builder();
        List<Integer> vpidQuarks = ss.getSubAttributes(ITmfStateSystem.ROOT_ATTRIBUTE, false);
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
                ITmfStateInterval interval = StateSystemUtils.queryUntilNonNullValue(ss, baddrQuark, ts, Long.MAX_VALUE);
                while (interval != null) {
                    ts = interval.getStartTime();

                    ITmfStateValue filePathStateValue = ss.querySingleState(ts, pathQuark).getStateValue();
                    String filePath = filePathStateValue.unboxStr();

                    ITmfStateValue buildIdStateValue = ss.querySingleState(ts, buildIdQuark).getStateValue();
                    String buildId = unboxStrOrNull(buildIdStateValue);

                    ITmfStateValue debuglinkStateValue = ss.querySingleState(ts, debugLinkQuark).getStateValue();
                    String debugLink = unboxStrOrNull(debuglinkStateValue);

                    ITmfStateValue isPICStateValue = ss.querySingleState(ts, isPICQuark).getStateValue();
                    Boolean isPIC = isPICStateValue.unboxInt() != 0;

                    files.add(new UstDebugInfoBinaryFile(filePath, buildId, debugLink, isPIC));

                    /*
                     * Go one past the end of the interval, and perform the
                     * query again to find the next mapping at this address.
                     */
                    ts = interval.getEndTime() + 1;
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
     * @noreference Meant to be used internally by
     *              {@link UstDebugInfoBinaryAspect} only.
     */
    @VisibleForTesting
    public @Nullable UstDebugInfoLoadedBinaryFile getMatchingFile(long ts, long vpid, long ip) {
        try {
            final ITmfStateSystem ss = stateSystem;

            List<Integer> possibleBaddrQuarks = ss.getQuarks(String.valueOf(vpid), "*"); //$NON-NLS-1$
            List<ITmfStateInterval> state = ss.queryFullState(ts);

            /* Get the most probable base address from all the known ones */
            OptionalLong potentialBaddr = possibleBaddrQuarks.stream()
                    .filter(quark -> {
                        /* Keep only currently (at ts) mapped objects. */
                        ITmfStateValue value = state.get(quark).getStateValue();
                        return value.getType() == ITmfStateValue.Type.INTEGER && value.unboxInt() == 1;
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
            final long memsz = state.get(memszQuark).getStateValue().unboxLong();

            /* Make sure the 'ip' fits the range of this object. */
            if (!(ip < baddr + memsz)) {
                /*
                 * Not the correct memory range after all. We do not have
                 * information about the library that was loaded here.
                 */
                return null;
            }

            final int pathQuark = ss.getQuarkRelative(baddrQuark, UstDebugInfoAnalysisStateProvider.PATH_ATTRIB);
            String filePath = state.get(pathQuark).getStateValue().unboxStr();

            final int buildIdQuark = ss.getQuarkRelative(baddrQuark, UstDebugInfoAnalysisStateProvider.BUILD_ID_ATTRIB);
            ITmfStateValue buildIdValue = state.get(buildIdQuark).getStateValue();
            String buildId = unboxStrOrNull(buildIdValue);

            final int debugLinkQuark = ss.getQuarkRelative(baddrQuark, UstDebugInfoAnalysisStateProvider.DEBUG_LINK_ATTRIB);
            ITmfStateValue debugLinkValue = state.get(debugLinkQuark).getStateValue();
            String debugLink = unboxStrOrNull(debugLinkValue);

            final int isPicQuark = ss.getQuarkRelative(baddrQuark, UstDebugInfoAnalysisStateProvider.IS_PIC_ATTRIB);
            boolean isPic = state.get(isPicQuark).getStateValue().unboxInt() != 0;

            return new UstDebugInfoLoadedBinaryFile(baddr, filePath, buildId, debugLink, isPic);

        } catch (AttributeNotFoundException | TimeRangeException | StateSystemDisposedException e) {
            /* Either the data is not available yet, or incomplete. */
            return null;
        }
    }

    private static @Nullable String unboxStrOrNull(ITmfStateValue value) {
        return (value.isNull() ? null : value.unboxStr());
    }
}
