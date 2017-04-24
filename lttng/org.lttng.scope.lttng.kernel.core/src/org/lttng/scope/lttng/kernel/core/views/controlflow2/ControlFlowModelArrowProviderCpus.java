/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.lttng.kernel.core.views.controlflow2;

import static java.util.Objects.requireNonNull;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.FutureTask;

import org.eclipse.jdt.annotation.Nullable;
import org.lttng.scope.lttng.kernel.core.analysis.os.Attributes;
import org.lttng.scope.lttng.kernel.core.analysis.os.KernelAnalysisModule;
import org.lttng.scope.tmf2.views.core.TimeRange;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.statesystem.StateSystemModelArrowProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.FlatUIColors;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.TimeGraphEvent;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.arrows.TimeGraphArrow;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.arrows.TimeGraphArrowRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.arrows.TimeGraphArrowSeries;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.arrows.TimeGraphArrowSeries.LineStyle;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeElement;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeRender;

import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;

import ca.polymtl.dorsal.libdelorean.ITmfStateSystem;
import ca.polymtl.dorsal.libdelorean.StateSystemUtils;
import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;
import ca.polymtl.dorsal.libdelorean.exceptions.StateSystemDisposedException;
import ca.polymtl.dorsal.libdelorean.interval.ITmfStateInterval;

public class ControlFlowModelArrowProviderCpus extends StateSystemModelArrowProvider {

    private static final TimeGraphArrowSeries ARROW_SERIES = new TimeGraphArrowSeries(
            requireNonNull(Messages.arrowSeriesCPUs),
            FlatUIColors.RED,
            LineStyle.FULL);

    public ControlFlowModelArrowProviderCpus() {
        super(ARROW_SERIES, KernelAnalysisModule.ID);
    }

    @Override
    public TimeGraphArrowRender getArrowRender(TimeGraphTreeRender treeRender, TimeRange timeRange, @Nullable FutureTask<?> task) {
        ITmfStateSystem ss = getStateSystem();
        if (ss == null) {
            return TimeGraphArrowRender.EMPTY_RENDER;
        }

        List<Integer> threadLineQuarks = ss.getQuarks(Attributes.CPUS, "*", Attributes.CURRENT_THREAD); //$NON-NLS-1$
        List<List<TimeGraphArrow>> allArrows = new LinkedList<>();
        try {
            for (int threadLineQuark : threadLineQuarks) {
                List<ITmfStateInterval> intervals = StateSystemUtils.queryHistoryRange(ss, threadLineQuark, timeRange.getStart(), timeRange.getEnd(), 1, task);
                if (task != null && task.isCancelled()) {
                    return TimeGraphArrowRender.EMPTY_RENDER;
                }
                if (intervals.size() < 2) {
                    /* Not enough states to establish a timeline */
                    continue;
                }

                String cpuName = ss.getAttributeName(ss.getParentAttributeQuark(threadLineQuark));
                Integer cpu = Ints.tryParse(cpuName);
                List<TimeGraphArrow> arrows = getArrowsFromStates(treeRender, intervals, cpu);
                allArrows.add(arrows);
            }

        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            e.printStackTrace();
            return TimeGraphArrowRender.EMPTY_RENDER;
        }

        Iterable<TimeGraphArrow> flattenedArrows = Iterables.concat(allArrows);
        return new TimeGraphArrowRender(timeRange, flattenedArrows);
    }

    private List<TimeGraphArrow> getArrowsFromStates(TimeGraphTreeRender treeRender, List<ITmfStateInterval> threadTimeline, @Nullable Integer cpu) {
        List<TimeGraphArrow> arrows = new LinkedList<>();
        for (int i = 1; i < threadTimeline.size(); i++) {
            ITmfStateInterval interval1 = threadTimeline.get(i - 1);
            ITmfStateInterval interval2 = threadTimeline.get(i);
            int thread1 = interval1.getStateValue().unboxInt();
            int thread2 = interval2.getStateValue().unboxInt();

            if (thread1 == -1 || thread2 == -1) {
                /* No arrow to draw here */
                continue;
            }

            TimeGraphTreeElement startTreeElem = getTreeElementFromThread(treeRender, thread1, cpu);
            TimeGraphTreeElement endTreeElem = getTreeElementFromThread(treeRender, thread2, cpu);
            TimeGraphEvent startEvent = new TimeGraphEvent(interval1.getEndTime(), startTreeElem);
            TimeGraphEvent endEvent  = new TimeGraphEvent(interval2.getStartTime(), endTreeElem);

            TimeGraphArrow arrow = new TimeGraphArrow(startEvent, endEvent, getArrowSeries());
            arrows.add(arrow);
        }
        return arrows;
    }

    private static TimeGraphTreeElement getTreeElementFromThread(TimeGraphTreeRender treeRender, int tid, @Nullable Integer cpu) {
        if (tid != 0) {
            // FIXME Could be improved via indexing, to avoid iterating the
            // whole
            // array for every single tid.
            return Iterables.find(treeRender.getAllTreeElements(), treeElem -> {
                ControlFlowTreeElement cfvTreeElem = (ControlFlowTreeElement) treeElem;
                return (cfvTreeElem.getTid() == tid);
            });
        }
        if (cpu == null) {
            throw new IllegalStateException();
        }
        String prefix = "0/" + cpu.toString(); //$NON-NLS-1$
        return Iterables.find(treeRender.getAllTreeElements(), treeElem -> {
            ControlFlowTreeElement cfvTreeElem = (ControlFlowTreeElement) treeElem;
            return cfvTreeElem.getName().startsWith(prefix);
        });
    }
}
