package org.lttng.scope.lttng.kernel.core.views.kernel.controlflow2;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.lttng.scope.lttng.kernel.core.analysis.os.Attributes;
import org.lttng.scope.lttng.kernel.core.event.aspect.KernelTidAspect;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.statesystem.StateSystemTimeGraphTreeElement;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeElement;

public class ControlFlowTreeElement extends StateSystemTimeGraphTreeElement {

    private static final String UNKNOWN_THREAD_NAME = "???"; //$NON-NLS-1$

    private final int fTid;
    private final String fThreadName;

    public ControlFlowTreeElement(String tidStr, @Nullable String threadName,
            List<TimeGraphTreeElement> children, int sourceQuark) {
        super(getElementName(tidStr, threadName),
                children,
                sourceQuark);

        if (tidStr.startsWith(Attributes.THREAD_0_PREFIX)) {
            fTid = 0;
        } else {
            fTid = Integer.parseInt(tidStr);
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
    public @Nullable Predicate<ITmfEvent> getEventMatching() {
        /*
         * This tree element represents a thread ID. Return true for events
         * whose TID aspect is the same as the TID of this element.
         */
        return event -> {
            Integer eventTid = KernelTidAspect.INSTANCE.resolve(event);
            return (eventTid != null && eventTid.intValue() == fTid);
        };
    }

}
