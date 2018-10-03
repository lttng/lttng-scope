/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson, Efficios Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.ttt.ctf;

import java.net.URL;

/**
 * Here is the list of the available test traces for the CTF parser.
 *
 * @author Alexandre Montplaisir
 */
public enum CtfTestTrace {
    /**
     * Example kernel trace
     *
     * <pre>
     * Trace Size: 13 MB
     * Tracer: lttng-modules 2.0.0
     * Event count: 695 319
     * Kernel version: 3.0.0-16-generic-pae
     * Trace length: 10s
     * </pre>
     */
    KERNEL("/kernel", 695319, 10),

    /**
     * Another kernel trace
     *
     * <pre>
     * Trace Size: 14 MB
     * Tracer: lttng-modules 2.0.0
     * Event count: 595 641
     * Kernel version: 3.2.0-18-generic
     * Trace length: 11s
     * </pre>
     */
    TRACE2("/trace2", 595641, 11),

    /**
     * Kernel trace with event contexts: pid, ppid, tid, procname,
     * perf_page_fault, perf_major_faults, perf_minor_faults
     *
     * <pre>
     * Trace Size: 56 MB
     * Tracer: lttng-modules 2.1.0
     * Event count: 714 484
     * Kernel version: 3.8.1
     * Trace length: 29s
     * </pre>
     */
    KERNEL_VM("/kernel_vm", 714484, 29),

    /**
     * Kernel trace with all events enabled. Contains 'inet_sock_local_*' events
     * provided by Francis's Giraldeau lttng-modules addons branch to trace TCP
     * events. Can be used along with {@link CtfTestTrace#SYNC_DEST} for trace
     * synchronization.
     *
     * <pre>
     * Trace Size: 2.4 MB
     * Tracer: lttng-modules 2.1.0
     * Event count: 110 771
     * Kernel version: 3.6.11-1-ARCH
     * Trace length: 23s
     * </pre>
     */
    SYNC_SRC("/synctraces/scp_src", 110771, 23),

    /**
     * Kernel trace with all events enabled. Contains 'inet_sock_local_*' events
     * provided by Francis's Giraldeau lttng-modules addons branch to trace TCP
     * events. Can be used along with {@link CtfTestTrace#SYNC_SRC} for trace
     * synchronization.
     *
     * <pre>
     * Trace Size: 1.9 MB
     * Tracer: lttng-modules 2.1.0
     * Event count: 85 729
     * Kernel version: 3.6.11-1-ARCH
     * Trace length: 17s
     * </pre>
     */
    SYNC_DEST("/synctraces/scp_dest", 85729, 17),

    /**
     * UST trace with lots of lost events
     *
     * <pre>
     * Trace Size: 3.4 MB
     * Tracer: lttng-ust 2.3
     * Event count: 1 000 000, with 967 700 lost events
     * Trace length: 279ms
     * </pre>
     */
    HELLO_LOST("/hello-lost", 1000000, 0),

    /**
     * UST trace with lttng-ust-cyg-profile events (aka -finstrument-functions)
     *
     * <pre>
     * Trace Size: 236 KB
     * Tracer: lttng-ust 2.3
     * Event count: 4 977
     * Trace length: 10s
     * </pre>
     */
    CYG_PROFILE("/cyg-profile/glxgears-cyg-profile", 4977, 10),

    /**
     * UST trace with lttng-ust-cyg-profile-fast events (no address in
     * func_exit)
     *
     * <pre>
     * Trace Size: 184 KB
     * Tracer: lttng-ust 2.3
     * Event count: 5 161
     * Trace length: 11s
     * </pre>
     */
    CYG_PROFILE_FAST("/cyg-profile/glxgears-cyg-profile-fast", 5161, 11),

    /** Trace with non-standard field sizes */
    FUNKY_TRACE("/funky_trace", 100002, 0),

    /**
     * Kernel Trace with 64 bit aligned headers
     *
     * <pre>
     * Trace Size: 1.1 MB
     * Tracer: lttng-kernel 2.5
     * Event count: 31 556
     * Trace length: 6 s
     * </pre>
     */
    ARM_64_BIT_HEADER("/bug446190", 31556, 6),

    /**
     * Kernel Trace with flipping endianness
     *
     * <pre>
     * Trace Size: 327 KB
     * Tracer: lttng-kernel 2.5
     * Event count: 14 310
     * Trace length: 41 s
     * </pre>
     */
    FLIPPING_ENDIANNESS("/flipping-endianness", 14310, 41),

    /**
     * Example dynamic scope, timestamp in field, empty stream trace
     *
     * <pre>
     * Trace Size: 3.5 MB
     * Tracer: generated
     * Event count: 101 003
     * Kernel version: 4.0.6-1
     * Trace length: 1 ms
     * </pre>
     */
    DYNSCOPE("/ctfwriter_JF8nq3", 101003, 1),

	/**
	 * UST Trace with Debug Info information (dev version)
	 *
	 * <pre>
	 * Trace Size: 76 KB (including index/)
	 * Tracer: lttng-ust 2.8~pre
	 * Event count: 41
	 * Trace length: ~1 s
	 * </pre>
	 * 
	 * @deprecated Some event names were changed between the moment this trace
	 *             was taken, and the final LTTng 2.8 release. Please use the
	 *             updated {@link #DEBUG_INFO3} trace instead.
	 */
    @Deprecated
    DEBUG_INFO("/debuginfo-test-app2", 41, 1),

    /**
     * UST Trace with Debug Info information, LTTng 2.8-rc2 version
     *
     * <pre>
     * Trace Size: 144 KB (including index/)
     * Tracer: lttng-ust 2.8 RC2
     * Event count: 291
     * Trace length: ~2 s
     * </pre>
     * 
     * @deprecated Event fields has_debug_link and has_build_id have been added
     *             after this trace was taken and before the final release.
     */
    @Deprecated
    DEBUG_INFO3("/debuginfo-test-app3", 291, 2),

    /**
     * UST Trace with Debug Info information, final LTTng 2.8 version
     *
     * <pre>
     * Trace Size: 52 KB (including index/)
     * Tracer: lttng-ust 2.8
     * Event count: 32
     * Trace length: ~1 s
     * </pre>
     */
    DEBUG_INFO4("/debuginfo-test-app4", 32, 1),

    /**
     * Synthetic UST trace simulating a process doing an exec, with debug
     * information events.
     *
     * <pre>
     * Trace Size: 36 KB
     * Tracer: Babeltrace CTF writer trying to impersonate lttng-ust 2.8
     * Event count: 9
     * Trace length: ~1 s
     * </pre>
     */
    DEBUG_INFO_SYNTH_EXEC("/debuginfo-synth-exec", 9, 1),

    /**
     * Synthetic UST trace simulating two processes doing a statedump
     * simultaneously, with debug information events.
     *
     * <pre>
     * Trace Size: 36 KB
     * Tracer: Babeltrace CTF writer trying to impersonate lttng-ust 2.8
     * Event count: 12
     * Trace length: ~1 s
     * </pre>
     */
    DEBUG_INFO_SYNTH_TWO_PROCESSES("/debuginfo-synth-two-processes", 12, 1),

    /**
     * Synthetic UST trace simulating various combination of binaries with build
     * IDs and debug links, with debug information events.
     *
     * <pre>
     * Trace Size: 36 KB
     * Tracer: Babeltrace CTF writer trying to impersonate lttng-ust 2.8
     * Event count: 20
     * Trace length: ~1 s
     * </pre>
     */
    DEBUG_INFO_SYNTH_BUILDID_DEBUGLINK("/debuginfo-synth-buildid-debuglink", 20, 1),

    /**
     * UST Trace with Memory analysis information
     *
     * <pre>
     * Trace Size: 12 MB
     * Tracer: lttng-ust 2.7
     * Event count: 328056
     * Trace length: ~0.53 s
     * </pre>
     */
    MEMORY_ANALYSIS("/memory", 328056, 1),

    /**
     * CTF trace coming from the perf-CTF converter.
     *
     * CPUs are not defined by standard "cpu_id" CTF data, but by event fields
     * called "perf_cpu".
     *
     * <pre>
     * Trace Size: 196 KB
     * Tracer: perf
     * Event count: 1500
     * Trace length: 0.417057183 s
     * </pre>
     */
    PERF_TASKSET2("/perf-taskset2", 1500, 1),

    /**
     * Kernel trace tracing the creation of multiple threads
     *
     * This would typically used to test the vertical scaling of the analysis
     * with many threads.
     *
     * <pre>
     * Trace Size: 8.2M
     * Tracer: lttng-kernel 2.8~pre
     * Event count: 240 644
     * Trace length: 0.432 662 358 s
     * </pre>
     */
    MANY_THREADS("/many-threads", 240644, 1),

    /**
     * Kernel trace of the "context-switch" experiment, which tests the clock
     * offsetting of different traces taken on the same host with different
     * clock offsets.
     *
     * <pre>
     * Trace Size: 4.6 MB
     * Tracer: lttng-kernel 2.6.3
     * Event count: 241 566
     * Trace length: ~92 s
     * </pre>
     */
    CONTEXT_SWITCHES_KERNEL("/context-switches/context-switches-kernel", 241566, 92),

    /**
     * Userspace trace of the "context-switch" experiment, which tests the clock
     * offsetting of different traces taken on the same host with different
     * clock offsets.
     *
     * <pre>
     * Trace Size: 136 KB
     * Tracer: lttng-ust 2.6.3
     * Event count: 3934
     * Trace length: ~49 s
     * </pre>
     */
    CONTEXT_SWITCHES_UST("/context-switches/context-switches-ust", 3934, 49),

    /**
     * Short UST-java trace consisting of a single event.
     */
    ONE_EVENT("/one-event", 1, 0),

    /**
     * Trace with streams of different start/end times, can be used to test the
     * stream intersection operations.
     *
     * <pre>
     * Trace Size: 19 MB
     * Tracer: lttng-kernel 2.10.0
     * Event count: 534619
     * Trace length: ~6 s
     * </pre>
     */
    UNEVEN_STREAMS("/uneven-streams", 534619, 6);


    private final String fTraceName;
    private final int fNbEvent;
    private int fDuration;

    private CtfTestTrace(String traceName, int nbEvent, int time) {
        fTraceName = traceName;
        fNbEvent = nbEvent;
        fDuration = time;
    }

    public URL getTraceURL() {
        URL url = this.getClass().getResource(fTraceName);
        if (url == null) {
            /* Project configuration problem? */
            throw new IllegalStateException("Test trace not found");
        }
        return url;
    }

    /**
     * Get the number of events for a trace
     *
     * @return the number of events, -1 if irrelevant
     */
    public int getNbEvents() {
        return fNbEvent;
    }

    /**
     * Get the duration in seconds of a trace
     *
     * @return the duration in seconds of a trace, -1 if irrelevant
     */
    public int getDuration() {
        return fDuration;
    }
}
