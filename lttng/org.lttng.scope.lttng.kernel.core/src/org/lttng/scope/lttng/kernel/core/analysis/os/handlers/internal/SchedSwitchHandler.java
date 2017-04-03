/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.lttng.scope.lttng.kernel.core.analysis.os.handlers.internal;

import static java.util.Objects.requireNonNull;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.lttng.scope.lttng.kernel.core.analysis.os.Attributes;
import org.lttng.scope.lttng.kernel.core.analysis.os.LinuxValues;
import org.lttng.scope.lttng.kernel.core.analysis.os.StateValues;
import org.lttng.scope.lttng.kernel.core.trace.layout.ILttngKernelEventLayout;

import ca.polymtl.dorsal.libdelorean.ITmfStateSystemBuilder;
import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;
import ca.polymtl.dorsal.libdelorean.exceptions.StateValueTypeException;
import ca.polymtl.dorsal.libdelorean.statevalue.ITmfStateValue;
import ca.polymtl.dorsal.libdelorean.statevalue.TmfStateValue;

/**
 * Scheduler switch event handler
 */
public class SchedSwitchHandler extends KernelEventHandler {

    /**
     * Constructor
     *
     * @param layout
     *            event layout
     */
    public SchedSwitchHandler(ILttngKernelEventLayout layout) {
        super(layout);
    }

    @Override
    public void handleEvent(ITmfStateSystemBuilder ss, ITmfEvent event) throws AttributeNotFoundException {
        Integer cpu = KernelEventHandlerUtils.getCpu(event);
        if (cpu == null) {
            return;
        }

        ITmfEventField content = event.getContent();
        String prevProcessName = requireNonNull((String) content.getField(getLayout().fieldPrevComm()).getValue());
        Integer prevTid = ((Long) content.getField(getLayout().fieldPrevTid()).getValue()).intValue();
        Long prevState = requireNonNull((Long) content.getField(getLayout().fieldPrevState()).getValue());
        Integer prevPrio = ((Long) content.getField(getLayout().fieldPrevPrio()).getValue()).intValue();
        String nextProcessName = requireNonNull((String) content.getField(getLayout().fieldNextComm()).getValue());
        Integer nextTid = ((Long) content.getField(getLayout().fieldNextTid()).getValue()).intValue();
        Integer nextPrio = ((Long) content.getField(getLayout().fieldNextPrio()).getValue()).intValue();

        /* Will never return null since "cpu" is null checked */
        String formerThreadAttributeName = Attributes.buildThreadAttributeName(prevTid, cpu);
        String currenThreadAttributeName = Attributes.buildThreadAttributeName(nextTid, cpu);

        int nodeThreads = KernelEventHandlerUtils.getNodeThreads(ss);
        int formerThreadNode = ss.getQuarkRelativeAndAdd(nodeThreads, formerThreadAttributeName);
        int newCurrentThreadNode = ss.getQuarkRelativeAndAdd(nodeThreads, currenThreadAttributeName);

        long timestamp = KernelEventHandlerUtils.getTimestamp(event);
        /*
         * Set the status of the process that got scheduled out. This will also
         * set it's current CPU run queue accordingly.
         */
        setOldProcessStatus(ss, prevState, formerThreadNode, cpu, timestamp);

        /* Set the status of the new scheduled process */
        KernelEventHandlerUtils.setProcessToRunning(timestamp, newCurrentThreadNode, ss);

        /*
         * Set the current CPU run queue of the new process. Should be already
         * set if we've seen the previous sched_wakeup, but doesn't hurt to set
         * it here too.
         */
        int quark = ss.getQuarkRelativeAndAdd(newCurrentThreadNode, Attributes.CURRENT_CPU_RQ);
        ITmfStateValue value = TmfStateValue.newValueInt(cpu);
        ss.modifyAttribute(timestamp, value, quark);

        /* Set the exec name of the former process */
        setProcessExecName(ss, prevProcessName, formerThreadNode, timestamp);

        /* Set the exec name of the new process */
        setProcessExecName(ss, nextProcessName, newCurrentThreadNode, timestamp);

        /* Set the current prio for the former process */
        setProcessPrio(ss, prevPrio, formerThreadNode, timestamp);

        /* Set the current prio for the new process */
        setProcessPrio(ss, nextPrio, newCurrentThreadNode, timestamp);

        /* Set the current scheduled process on the relevant CPU */
        int currentCPUNode = KernelEventHandlerUtils.getCurrentCPUNode(cpu, ss);
        setCpuProcess(ss, nextTid, timestamp, currentCPUNode);

        /* Set the status of the CPU itself */
        setCpuStatus(ss, nextTid, newCurrentThreadNode, timestamp, currentCPUNode);
    }

    private static void setOldProcessStatus(ITmfStateSystemBuilder ss,
            long prevState, int formerThreadNode, int cpu, long timestamp) {
        ITmfStateValue value;
        boolean staysOnRunQueue = false;
        /*
         * Empirical observations and look into the linux code have
         * shown that the TASK_STATE_MAX flag is used internally and
         * |'ed with other states, most often the running state, so it
         * is ignored from the prevState value.
         *
         * Since Linux 4.1, the TASK_NOLOAD state was created and
         * TASK_STATE_MAX is now 2048. We use TASK_NOLOAD as the new max
         * because it does not modify the displayed state value.
         */
        int state = (int) (prevState & (LinuxValues.TASK_NOLOAD - 1));

        if (isRunning(state)) {
            value = StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE;
            staysOnRunQueue = true;
        } else if (isWaiting(state)) {
            value = StateValues.PROCESS_STATUS_WAIT_BLOCKED_VALUE;
        } else if (isDead(state)) {
            value = TmfStateValue.nullValue();
        } else {
            value = StateValues.PROCESS_STATUS_WAIT_UNKNOWN_VALUE;
        }
        ss.modifyAttribute(timestamp, value, formerThreadNode);

        int quark = ss.getQuarkRelativeAndAdd(formerThreadNode, Attributes.CURRENT_CPU_RQ);
        if (staysOnRunQueue) {
            /*
             * Set the thread's run queue. This will often be redundant with
             * previous events, but it may be the first time we see the
             * information too.
             */
            value = TmfStateValue.newValueInt(cpu);
        } else {
            value = TmfStateValue.nullValue();
        }
        ss.modifyAttribute(timestamp, value, quark);
    }

    private static boolean isDead(int state) {
        return (state & LinuxValues.TASK_DEAD) != 0;
    }

    private static boolean isWaiting(int state) {
        return (state & (LinuxValues.TASK_INTERRUPTIBLE | LinuxValues.TASK_UNINTERRUPTIBLE)) != 0;
    }

    private static boolean isRunning(int state) {
        // special case, this means ALL STATES ARE 0
        // this is effectively an anti-state
        return state == 0;
    }

    private static void setCpuStatus(ITmfStateSystemBuilder ss, Integer nextTid, Integer newCurrentThreadNode, long timestamp, int currentCPUNode)
            throws StateValueTypeException, AttributeNotFoundException {
        int quark;
        ITmfStateValue value;
        if (nextTid > 0) {
            /* Check if the entering process is in kernel or user mode */
            quark = ss.getQuarkRelativeAndAdd(newCurrentThreadNode, Attributes.SYSTEM_CALL);
            ITmfStateValue queryOngoingState = ss.queryOngoingState(quark);
            if (queryOngoingState.isNull()) {
                value = StateValues.CPU_STATUS_RUN_USERMODE_VALUE;
            } else {
                value = StateValues.CPU_STATUS_RUN_SYSCALL_VALUE;
            }
        } else {
            value = StateValues.CPU_STATUS_IDLE_VALUE;
        }
        ss.modifyAttribute(timestamp, value, currentCPUNode);
    }

    private static void setCpuProcess(ITmfStateSystemBuilder ss, Integer nextTid, long timestamp, int currentCPUNode)
            throws StateValueTypeException, AttributeNotFoundException {
        int quark;
        ITmfStateValue value;
        quark = ss.getQuarkRelativeAndAdd(currentCPUNode, Attributes.CURRENT_THREAD);
        value = TmfStateValue.newValueInt(nextTid);
        ss.modifyAttribute(timestamp, value, quark);
    }

    private static void setProcessPrio(ITmfStateSystemBuilder ss, Integer prio, Integer threadNode, long timestamp)
            throws StateValueTypeException, AttributeNotFoundException {
        int quark;
        ITmfStateValue value;
        quark = ss.getQuarkRelativeAndAdd(threadNode, Attributes.PRIO);
        value = TmfStateValue.newValueInt(prio);
        ss.modifyAttribute(timestamp, value, quark);
    }

    private static void setProcessExecName(ITmfStateSystemBuilder ss, String processName, Integer threadNode, long timestamp)
            throws StateValueTypeException, AttributeNotFoundException {
        int quark;
        ITmfStateValue value;
        quark = ss.getQuarkRelativeAndAdd(threadNode, Attributes.EXEC_NAME);
        value = TmfStateValue.newValueString(processName);
        ss.modifyAttribute(timestamp, value, quark);
    }

}
