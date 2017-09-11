/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline.widgets.timegraph.toolbar.nav;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.lttng.scope.views.timeline.widgets.timegraph.StateRectangle;
import org.lttng.scope.views.timeline.widgets.timegraph.TimeGraphWidget;

import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeElement;

/**
 * Navigation mode using state changes. It goes to the end/start of the current
 * selected state interval, or jumps to the next/previous one if we are already
 * at a border.
 *
 * @author Alexandre Montplaisir
 */
public class NavigationModeFollowStateChanges extends NavigationMode {

    private static final String BACK_ICON_PATH = "/icons/toolbar/nav_statechange_back.gif"; //$NON-NLS-1$
    private static final String FWD_ICON_PATH = "/icons/toolbar/nav_statechange_fwd.gif"; //$NON-NLS-1$

    private static final Comparator<StateRectangle> EARLIEST_START_TIME_COMPARATOR =
            Comparator.comparingLong(rect -> rect.getStateInterval().getStartEvent().getTimestamp());
    private static final Comparator<StateRectangle> LATEST_END_TIME_COMPARATOR =
            Comparator.<StateRectangle> comparingLong(rect -> rect.getStateInterval().getEndEvent().getTimestamp()).reversed();

    /**
     * Constructor
     */
    public NavigationModeFollowStateChanges() {
        super(requireNonNull(Messages.sfFollowStateChangesNavModeName),
                BACK_ICON_PATH,
                FWD_ICON_PATH);
    }

    @Override
    public void navigateBackwards(TimeGraphWidget viewer) {
        navigate(viewer, false);
    }


    @Override
    public void navigateForwards(TimeGraphWidget viewer) {
        navigate(viewer, true);
    }

    private static void navigate(TimeGraphWidget viewer, boolean forward) {
        StateRectangle state = viewer.getSelectedState();
        if (state == null) {
            return;
        }
        long stateStartTime = state.getStateInterval().getStartEvent().getTimestamp();
        long stateEndTime = state.getStateInterval().getEndEvent().getTimestamp();

        /* Aim to go to the start/end of the next/previous interval */
        long targetTimestamp = (forward ? stateEndTime + 1 : stateStartTime - 1);
        TimeGraphTreeElement treeElement = state.getStateInterval().getStartEvent().getTreeElement();
        List<StateRectangle> potentialStates = getPotentialStates(viewer, targetTimestamp, treeElement, forward);

        if (potentialStates.isEmpty()) {
            /*
             * We either reached the end of our model or an edge of the trace.
             * Go to the end/start of the current state.
             */
            long bound = (forward ? stateEndTime : stateStartTime);
            NavUtils.selectNewTimestamp(viewer, bound);
            return;
        }

        /*
         * Also compute the intervals that intersect the target timestamp.
         * We will prefer those, but if there aren't any, we'll pick the
         * best "potential" state.
         */
        List<StateRectangle> intersectingStates = getIntersectingStates(potentialStates, targetTimestamp, forward);

        StateRectangle newState;
        if (intersectingStates.isEmpty()) {
            /*
             * Let's look back into 'potentialStates' (non-intersecting)
             * and pick the interval with the closest bound.
             */
            Optional<StateRectangle> optState = getBestPotentialState(potentialStates, forward);
            if (!optState.isPresent()) {
                /* We did our best and didn't find anything. */
                return;
            }
            newState = optState.get();

        } else if (intersectingStates.size() == 1) {
            /* There is only one match, must be the right one. */
            newState = intersectingStates.get(0);
        } else {
            /*
             * There is more than one match (overlapping intervals, can
             * happen sometimes with multi-states). Pick the one with the
             * earliest start time (for backwards) or latest end time (for
             * forwards), to ensure we "move out" on the next action.
             */
            newState = intersectingStates.stream()
                    .sorted(forward ? LATEST_END_TIME_COMPARATOR : EARLIEST_START_TIME_COMPARATOR)
                    .findFirst().get();
        }

        viewer.setSelectedState(newState, true);
        newState.showTooltip(forward);
        NavUtils.selectNewTimestamp(viewer, targetTimestamp);
    }

    /**
     * Compute all the potentially valid states for a navigate
     * backwards/forwards operation.
     *
     * This means all the states for the given time graph tree element which
     * happen before, or after, the time given timestamp for backwards or
     * forwards operation respectively.
     */
    private static List<StateRectangle> getPotentialStates(TimeGraphWidget viewer, long targetTimestamp,
            TimeGraphTreeElement treeElement, boolean forward) {

        Stream<StateRectangle> potentialStates = viewer.getRenderedStateRectangles().stream()
                /* Keep only the intervals of the current tree element */
                .filter(rect -> rect.getStateInterval().getStartEvent().getTreeElement().equals(treeElement));

        if (forward) {
            /*
             * Keep only those intersecting, or happening after, the target
             * timestamp.
             */
            potentialStates = potentialStates.filter(rect -> rect.getStateInterval().getEndEvent().getTimestamp() >= targetTimestamp);
        } else {
            /*
             * Keep only those intersecting, or happening before, the target
             * timestamp.
             */
            potentialStates = potentialStates.filter(rect -> rect.getStateInterval().getStartEvent().getTimestamp() <= targetTimestamp);
        }

        List<StateRectangle> allStates = potentialStates.collect(Collectors.toList());
        return allStates;
    }

    /**
     * From a list of potential states, generate the list of intersecting
     * states. This means all state intervals that actually cross the target
     * timestamp.
     *
     * Note that we've already verified one of the start/end time for back/forth
     * navigation when generating the potential states, this method only needs
     * to check the other bound.
     */
    private static List<StateRectangle> getIntersectingStates(List<StateRectangle> potentialStates,
            long targetTimestamp, boolean forward) {

        Stream<StateRectangle> intersectingStates = potentialStates.stream();
        if (forward) {
            intersectingStates = intersectingStates.filter(rect -> {
                long start = rect.getStateInterval().getStartEvent().getTimestamp();
                return (targetTimestamp >= start);
            });
        } else {
            intersectingStates = intersectingStates.filter(rect -> {
                long end = rect.getStateInterval().getEndEvent().getTimestamp();
                return (targetTimestamp <= end);
            });
        }
        return intersectingStates.collect(Collectors.toList());
    }

    private static Optional<StateRectangle> getBestPotentialState(List<StateRectangle> potentialStates, boolean forward) {
        return potentialStates.stream()
                .sorted(forward ?  EARLIEST_START_TIME_COMPARATOR : LATEST_END_TIME_COMPARATOR)
                .findFirst();
    }

}
