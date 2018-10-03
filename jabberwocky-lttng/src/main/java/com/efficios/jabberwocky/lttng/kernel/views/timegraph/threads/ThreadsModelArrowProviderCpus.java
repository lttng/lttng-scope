/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.kernel.views.timegraph.threads;

import ca.polymtl.dorsal.libdelorean.IStateSystemReader;
import ca.polymtl.dorsal.libdelorean.StateSystemUtils;
import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;
import ca.polymtl.dorsal.libdelorean.exceptions.StateSystemDisposedException;
import ca.polymtl.dorsal.libdelorean.interval.StateInterval;
import ca.polymtl.dorsal.libdelorean.statevalue.IntegerStateValue;
import ca.polymtl.dorsal.libdelorean.statevalue.StateValue;
import com.efficios.jabberwocky.common.TimeRange;
import com.efficios.jabberwocky.lttng.kernel.analysis.os.Attributes;
import com.efficios.jabberwocky.lttng.kernel.analysis.os.KernelAnalysis;
import com.efficios.jabberwocky.views.common.FlatUIColors;
import com.efficios.jabberwocky.views.timegraph.model.provider.statesystem.StateSystemModelArrowProvider;
import com.efficios.jabberwocky.views.timegraph.model.render.TimeGraphEvent;
import com.efficios.jabberwocky.views.timegraph.model.render.arrows.TimeGraphArrow;
import com.efficios.jabberwocky.views.timegraph.model.render.arrows.TimeGraphArrowRender;
import com.efficios.jabberwocky.views.timegraph.model.render.arrows.TimeGraphArrowSeries;
import com.efficios.jabberwocky.views.timegraph.model.render.arrows.TimeGraphArrowSeries.LineStyle;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeElement;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeRender;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.FutureTask;

import static java.util.Objects.requireNonNull;

public class ThreadsModelArrowProviderCpus extends StateSystemModelArrowProvider {

    private static final TimeGraphArrowSeries ARROW_SERIES = new TimeGraphArrowSeries(
            requireNonNull(Messages.arrowSeriesCPUs),
            FlatUIColors.RED,
            LineStyle.FULL);

    public ThreadsModelArrowProviderCpus() {
        super(ARROW_SERIES, KernelAnalysis.INSTANCE);
    }

    @Override
    public TimeGraphArrowRender getArrowRender(TimeGraphTreeRender treeRender, TimeRange timeRange, @Nullable FutureTask<?> task) {
        IStateSystemReader ss = getStateSystem();
        if (ss == null) {
            return TimeGraphArrowRender.EMPTY_RENDER;
        }

        List<Integer> threadLineQuarks = ss.getQuarks(Attributes.CPUS, "*", Attributes.CURRENT_THREAD); //$NON-NLS-1$
        List<List<TimeGraphArrow>> allArrows = new LinkedList<>();
        try {
            for (int threadLineQuark : threadLineQuarks) {
                List<StateInterval> intervals = StateSystemUtils.queryHistoryRange(ss, threadLineQuark, timeRange.getStartTime(), timeRange.getEndTime(), 1, task);
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

    private List<TimeGraphArrow> getArrowsFromStates(TimeGraphTreeRender treeRender, List<StateInterval> threadTimeline, @Nullable Integer cpu) {
        List<TimeGraphArrow> arrows = new LinkedList<>();
        for (int i = 1; i < threadTimeline.size(); i++) {
            StateInterval interval1 = threadTimeline.get(i - 1);
            StateInterval interval2 = threadTimeline.get(i);

            StateValue sv1 = interval1.getStateValue();
            StateValue sv2 = interval2.getStateValue();

            if (!(sv1 instanceof IntegerStateValue) || !(sv2 instanceof IntegerStateValue)) {
                continue;
            }

            int thread1 = ((IntegerStateValue) sv1).getValue();
            int thread2 = ((IntegerStateValue) sv2).getValue();

            if (thread1 == -1 || thread2 == -1) {
                /* No arrow to draw here */
                continue;
            }

            TimeGraphTreeElement startTreeElem = getTreeElementFromThread(treeRender, thread1, cpu);
            TimeGraphTreeElement endTreeElem = getTreeElementFromThread(treeRender, thread2, cpu);
            TimeGraphEvent startEvent = new TimeGraphEvent(interval1.getEnd(), startTreeElem);
            TimeGraphEvent endEvent = new TimeGraphEvent(interval2.getStart(), endTreeElem);

            TimeGraphArrow arrow = new TimeGraphArrow(startEvent, endEvent, getArrowSeries());
            arrows.add(arrow);
        }
        return arrows;
    }

    private static TimeGraphTreeElement getTreeElementFromThread(TimeGraphTreeRender treeRender, int tid, @Nullable Integer cpu) {
        if (tid != 0) {
            // FIXME Could be improved via indexing, to avoid iterating the
            // whole array for every single tid.
            return Iterables.find(treeRender.getAllTreeElements(), treeElem -> {
                if (!(treeElem instanceof ThreadsTreeElement)) {
                    return false;
                }
                ThreadsTreeElement cfvTreeElem = (ThreadsTreeElement) treeElem;
                return (cfvTreeElem.getTid() == tid);
            });
        }
        if (cpu == null) {
            throw new IllegalStateException();
        }
        String prefix = "0/" + cpu.toString(); //$NON-NLS-1$
        return Iterables.find(treeRender.getAllTreeElements(), treeElem -> {
            if (!(treeElem instanceof ThreadsTreeElement)) {
                return false;
            }
            ThreadsTreeElement cfvTreeElem = (ThreadsTreeElement) treeElem;
            return cfvTreeElem.getName().startsWith(prefix);
        });
    }
}
