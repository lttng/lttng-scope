/*
 * Copyright (C) 2015 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.kernel.analysis.os

/**
 * Definitions of values used in the Linux kernel code.
 *
 * Instead of using "magic numbers" in state providers, the definitions should
 * be added here first.
 */
object LinuxValues {

    /**
     * Process states found in scheduler events.
     *
     * From include/linux/sched.h
     *
     * <pre>
     * #define TASK_RUNNING 0
     * #define TASK_INTERRUPTIBLE 1
     * #define TASK_UNINTERRUPTIBLE 2
     * #define __TASK_STOPPED 4
     * #define __TASK_TRACED 8
     * #define EXIT_DEAD 16
     * #define EXIT_ZOMBIE 32
     * #define EXIT_TRACE (EXIT_ZOMBIE | EXIT_DEAD)
     * #define TASK_DEAD 64
     * #define TASK_WAKEKILL 128
     * #define TASK_WAKING 256
     * #define TASK_PARKED 512
     * #define TASK_NOLOAD 1024
     * #define TASK_STATE_MAX 2048
     * </pre>
     */
    /**
     * The task is running normally, can be interrupted, in a syscall or user
     * mode.
     */
    const val TASK_STATE_RUNNING = 0

    /**
     * The process is in an interruptible sleep, (waiting for an event to
     * complete)
     */
    const val TASK_INTERRUPTIBLE = 1

    /**
     * The process is in an uninteruptible sleep, (usually waiting on IO)
     */
    const val TASK_UNINTERRUPTIBLE = 2

    /**
     * The process is stopped, it is waiting for a SIGCONT
     */
    const val TASK_STOPPED__ = 4

    /**
     * The process is being monitored by other processes like a debugger
     */
    const val TASK_TRACED__ = 8

    /**
     * The task is terminated. It is lingering waiting for a parent to reap it.
     */
    const val EXIT_ZOMBIE = 16

    /**
     * The final state, the process reaches this state when being reaped. This
     * state should not be seen.
     */
    const val EXIT_DEAD = 32

    /**
     * The task is dead, that means the PID can be re-used.
     */
    const val TASK_DEAD = 64

    /**
     * The task will wake up only on kill signals
     */
    const val TASK_WAKEKILL = 128

    /**
     * A task is being woken up, should not appear in sched switch, but if we
     * poll.
     */
    const val TASK_WAKING = 256

    /**
     * A very deep sleep that can only be woken by an unpark wakeup
     */
    const val TASK_PARK = 512

    /**
     * Task that do not contribute to load average (since Linux 4.1)
     */
    const val TASK_NOLOAD = 1024

    /**
     * This is the maximum value + 1 that the task state can be. TASK_STATE_MAX
     * - 1 is useful to mask the task state.
     */
    const val TASK_STATE_MAX = 2048

    /**
     * Process statuses, used in LTTng statedump events.
     *
     * This is LTTng-specific, but the statedump are handled at this level, so
     * it makes sense to add those definitions here.
     *
     * Taken from lttng-module's lttng-statedump-impl.c:
     *
     * <pre>
     * enum lttng_process_status {
     *     LTTNG_UNNAMED = 0,
     *     LTTNG_WAIT_FORK = 1,
     *     LTTNG_WAIT_CPU = 2,
     *     LTTNG_EXIT = 3,
     *     LTTNG_ZOMBIE = 4,
     *     LTTNG_WAIT = 5,
     *     LTTNG_RUN = 6,
     *     LTTNG_DEAD = 7,
     * };
     * </pre>
     */

    /** Task is initially preempted */
    const val STATEDUMP_PROCESS_STATUS_WAIT_CPU = 2

    /** Task is initially blocked */
    const val STATEDUMP_PROCESS_STATUS_WAIT = 5

    /**
     * SoftIRQ definitions
     *
     * From linux/interrupt.h
     *
     * <pre>
     * enum
     * {
     *     HI_SOFTIRQ=0,
     *     TIMER_SOFTIRQ,
     *     NET_TX_SOFTIRQ,
     *     NET_RX_SOFTIRQ,
     *     BLOCK_SOFTIRQ,
     *     BLOCK_IOPOLL_SOFTIRQ,
     *     TASKLET_SOFTIRQ,
     *     SCHED_SOFTIRQ,
     *     HRTIMER_SOFTIRQ,
     *     RCU_SOFTIRQ,
     *     NR_SOFTIRQS // not used as this is the NUMBER of softirqs
     * };
     * </pre>
     */

    /** High-priority tasklet */
    const val SOFTIRQ_HI = 0

    /** Interrupted because of timer */
    const val SOFTIRQ_TIMER = 1

    /** Interrupted because of network transmission */
    const val SOFTIRQ_NET_TX = 2

    /** Interrupted because of network reception */
    const val SOFTIRQ_NET_RX = 3

    /** Interrupted because of block operation */
    const val SOFTIRQ_BLOCK = 4

    /** Interrupted because of block IO */
    const val SOFTIRQ_BLOCK_IOPOLL = 5

    /** Tasklet (differed device interrupt) */
    const val SOFTIRQ_TASKLET = 6

    /** Interrupted because of the scheduler */
    const val SOFTIRQ_SCHED = 7

    /** Interrupted because of HR timer */
    const val SOFTIRQ_HRTIMER = 8

    /** Interrupted because of RCU */
    const val SOFTIRQ_RCU = 9
}
