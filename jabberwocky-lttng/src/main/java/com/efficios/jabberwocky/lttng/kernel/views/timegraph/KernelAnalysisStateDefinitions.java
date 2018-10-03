/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.kernel.views.timegraph;

import com.efficios.jabberwocky.views.common.FlatUIColors;
import com.efficios.jabberwocky.views.timegraph.model.render.LineThickness;
import com.efficios.jabberwocky.views.timegraph.model.render.StateDefinition;
import com.efficios.jabberwocky.views.common.ColorDefinition;

/**
 * State definitions used in the views of the kernel analysis.
 *
 * @author Alexandre Montplaisir
 */
@SuppressWarnings("javadoc")
public final class KernelAnalysisStateDefinitions {

    private KernelAnalysisStateDefinitions() {}

    public static final StateDefinition NO_STATE = new StateDefinition(Messages.noState, new ColorDefinition(0, 0, 0, 0), LineThickness.NORMAL);

    public static final StateDefinition THREAD_STATE_UNKNOWN      = new StateDefinition(Messages.threadStateUnknown,     FlatUIColors.DARK_GRAY,  LineThickness.TINY);
    public static final StateDefinition THREAD_STATE_WAIT_UNKNOWN = new StateDefinition(Messages.threadStateWaitUnknown, FlatUIColors.LIGHT_GRAY, LineThickness.TINY);
    public static final StateDefinition THREAD_STATE_WAIT_BLOCKED = new StateDefinition(Messages.threadStateWaitBlocked, FlatUIColors.YELLOW,     LineThickness.TINY);
    public static final StateDefinition THREAD_STATE_WAIT_FOR_CPU = new StateDefinition(Messages.threadStateWaitForCpu,  FlatUIColors.ORANGE,     LineThickness.NORMAL);
    public static final StateDefinition THREAD_STATE_USERMODE     = new StateDefinition(Messages.threadSateUsermode,     FlatUIColors.DARK_GREEN, LineThickness.NORMAL);
    public static final StateDefinition THREAD_STATE_SYSCALL      = new StateDefinition(Messages.threadStateSyscall,     FlatUIColors.DARK_BLUE,  LineThickness.NORMAL);
    public static final StateDefinition THREAD_STATE_INTERRUPTED  = new StateDefinition(Messages.threadStateInterrupted, FlatUIColors.PURPLE,     LineThickness.NORMAL);

    public static final StateDefinition CPU_STATE_UNKNOWN         = new StateDefinition(Messages.cpuStateUnknown,        FlatUIColors.DARK_GRAY,    LineThickness.NORMAL);
    public static final StateDefinition CPU_STATE_IDLE            = new StateDefinition(Messages.cpuStateIdle,           FlatUIColors.GRAY,         LineThickness.TINY);
    public static final StateDefinition CPU_STATE_IRQ_ACTIVE      = new StateDefinition(Messages.cpuStateIrqActive,      FlatUIColors.DARK_PURPLE,  LineThickness.NORMAL);
    public static final StateDefinition CPU_STATE_SOFTIRQ_ACTIVE  = new StateDefinition(Messages.cpuStateSoftIrqActive,  FlatUIColors.DARK_ORANGE,  LineThickness.NORMAL);
    public static final StateDefinition CPU_STATE_SOFTIRQ_RAISED  = new StateDefinition(Messages.cpuStateSoftIrqRaised,  FlatUIColors.DARK_YELLOW,  LineThickness.NORMAL);

}
