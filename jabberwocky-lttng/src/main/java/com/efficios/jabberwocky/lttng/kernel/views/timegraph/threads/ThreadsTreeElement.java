package com.efficios.jabberwocky.lttng.kernel.views.timegraph.threads;

import ca.polymtl.dorsal.libdelorean.IStateSystemReader;
import com.efficios.jabberwocky.lttng.kernel.analysis.os.Attributes;
import com.efficios.jabberwocky.lttng.kernel.analysis.os.KernelThreadInfoKt;
import com.efficios.jabberwocky.trace.event.TraceEvent;
import com.efficios.jabberwocky.views.timegraph.model.provider.statesystem.StateSystemTimeGraphTreeElement;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeElement;
import com.google.common.primitives.Ints;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

public class ThreadsTreeElement extends StateSystemTimeGraphTreeElement {

    private static final String UNKNOWN_THREAD_NAME = "???"; //$NON-NLS-1$

    private final int fTid;
    /** CPU is only defined when fTid == 0 */
    private final @Nullable Integer fCpu;
    private final String fThreadName;

    public ThreadsTreeElement(String tidStr, @Nullable String threadName,
                              List<TimeGraphTreeElement> children, IStateSystemReader ss, int sourceQuark) {
        super(getElementName(tidStr, threadName),
                children,
                ss,
                sourceQuark);

        if (tidStr.startsWith(Attributes.THREAD_0_PREFIX)) {
            fTid = 0;
            String cpuStr = tidStr.substring(Attributes.THREAD_0_PREFIX.length());
            Integer cpu = Ints.tryParse(cpuStr);
            fCpu = (cpu == null ? 0 : cpu);
        } else {
            fTid = Integer.parseInt(tidStr);
            fCpu = null;
        }

        fThreadName = (threadName == null ? UNKNOWN_THREAD_NAME : threadName);
    }

    private static String getElementName(String tidStr, @Nullable String threadName) {
        String tidPart = tidStr;
        if (tidPart.startsWith(Attributes.THREAD_0_PREFIX)) {
            /* Display "0/0" instead of "0_0" */
            tidPart = tidPart.replace('_', '/');
        }

        String threadNamePart = (threadName == null ? UNKNOWN_THREAD_NAME : threadName);
        return (tidPart + " - " + threadNamePart); //$NON-NLS-1$
    }

    public int getTid() {
        return fTid;
    }

    public String getThreadName() {
        return fThreadName;
    }

    @Override
    public @Nullable Predicate<TraceEvent> getEventMatching() {
        /*
         * This tree element represents a thread ID. Return true for events
         * whose TID aspect is the same as the TID of this element.
         */
        return event -> {
            Integer eventTid = KernelThreadInfoKt.getThreadOnCpu(getStateSystem(), event.getCpu(), event.getTimestamp());
            if (eventTid == null) {
                return false;
            }
            if (fTid != 0) {
                return (eventTid.intValue() == fTid);
            }
            /*
             * There are many elements for TID 0. We also need to compare the
             * CPU.
             */
            int elemCpu = requireNonNull(fCpu).intValue();
            int eventCpu = event.getCpu();
            return (eventTid.intValue() == fTid
                    && eventCpu == elemCpu);
        };
    }

}
