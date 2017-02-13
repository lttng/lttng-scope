/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.lttng.scope.lttng.kernel.core.analysis.os.internal;

import static java.util.Objects.requireNonNull;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.lttng.scope.lttng.kernel.core.activator.internal.Activator;
import org.lttng.scope.lttng.kernel.core.analysis.os.handlers.internal.IPIEntryHandler;
import org.lttng.scope.lttng.kernel.core.analysis.os.handlers.internal.IPIExitHandler;
import org.lttng.scope.lttng.kernel.core.analysis.os.handlers.internal.IrqEntryHandler;
import org.lttng.scope.lttng.kernel.core.analysis.os.handlers.internal.IrqExitHandler;
import org.lttng.scope.lttng.kernel.core.analysis.os.handlers.internal.KernelEventHandler;
import org.lttng.scope.lttng.kernel.core.analysis.os.handlers.internal.PiSetprioHandler;
import org.lttng.scope.lttng.kernel.core.analysis.os.handlers.internal.ProcessExitHandler;
import org.lttng.scope.lttng.kernel.core.analysis.os.handlers.internal.ProcessForkHandler;
import org.lttng.scope.lttng.kernel.core.analysis.os.handlers.internal.ProcessFreeHandler;
import org.lttng.scope.lttng.kernel.core.analysis.os.handlers.internal.SchedSwitchHandler;
import org.lttng.scope.lttng.kernel.core.analysis.os.handlers.internal.SchedWakeupHandler;
import org.lttng.scope.lttng.kernel.core.analysis.os.handlers.internal.SoftIrqEntryHandler;
import org.lttng.scope.lttng.kernel.core.analysis.os.handlers.internal.SoftIrqExitHandler;
import org.lttng.scope.lttng.kernel.core.analysis.os.handlers.internal.SoftIrqRaiseHandler;
import org.lttng.scope.lttng.kernel.core.analysis.os.handlers.internal.StateDumpHandler;
import org.lttng.scope.lttng.kernel.core.analysis.os.handlers.internal.SysEntryHandler;
import org.lttng.scope.lttng.kernel.core.analysis.os.handlers.internal.SysExitHandler;
import org.lttng.scope.lttng.kernel.core.trace.layout.ILttngKernelEventLayout;

import com.google.common.collect.ImmutableMap;

import ca.polymtl.dorsal.libdelorean.ITmfStateSystemBuilder;
import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;
import ca.polymtl.dorsal.libdelorean.exceptions.StateValueTypeException;
import ca.polymtl.dorsal.libdelorean.exceptions.TimeRangeException;

/**
 * This is the state change input plugin for the state system which handles the
 * kernel traces.
 *
 * Attribute tree:
 *
 * <pre>
 * |- CPUs
 * |  |- <CPU number> -> CPU Status
 * |  |  |- CURRENT_THREAD
 * |  |  |- SOFT_IRQS
 * |  |  |  |- <Soft IRQ number> -> Soft IRQ Status
 * |  |  |- IRQS
 * |  |  |  |- <IRQ number> -> IRQ Status
 * |- THREADS
 * |  |- <Thread number> -> Thread Status
 * |  |  |- PPID
 * |  |  |- EXEC_NAME
 * |  |  |- PRIO
 * |  |  |- SYSTEM_CALL
 * </pre>
 *
 * @author Alexandre Montplaisir
 */
public class KernelStateProvider extends AbstractTmfStateProvider {

    // ------------------------------------------------------------------------
    // Static fields
    // ------------------------------------------------------------------------

    /**
     * Version number of this state provider. Please bump this if you modify the
     * contents of the generated state history in some way.
     */
    private static final int VERSION = 23;

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    private final Map<String, KernelEventHandler> fEventNames;
    private final ILttngKernelEventLayout fLayout;

    private final KernelEventHandler fSysEntryHandler;
    private final KernelEventHandler fSysExitHandler;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Instantiate a new state provider plugin.
     *
     * @param trace
     *            The LTTng 2.0 kernel trace directory
     * @param layout
     *            The event layout to use for this state provider. Usually
     *            depending on the tracer implementation.
     */
    public KernelStateProvider(ITmfTrace trace, ILttngKernelEventLayout layout) {
        super(trace, "Kernel"); //$NON-NLS-1$
        fLayout = layout;
        fEventNames = buildEventNames(layout);

        fSysEntryHandler = new SysEntryHandler(fLayout);
        fSysExitHandler = new SysExitHandler(fLayout);
    }

    // ------------------------------------------------------------------------
    // Event names management
    // ------------------------------------------------------------------------

    private static Map<String, KernelEventHandler> buildEventNames(ILttngKernelEventLayout layout) {
        ImmutableMap.Builder<String, KernelEventHandler> builder = ImmutableMap.builder();

        builder.put(layout.eventIrqHandlerEntry(), new IrqEntryHandler(layout));
        builder.put(layout.eventIrqHandlerExit(), new IrqExitHandler(layout));
        builder.put(layout.eventSoftIrqEntry(), new SoftIrqEntryHandler(layout));
        builder.put(layout.eventSoftIrqExit(), new SoftIrqExitHandler(layout));
        builder.put(layout.eventSoftIrqRaise(), new SoftIrqRaiseHandler(layout));
        builder.put(layout.eventSchedSwitch(), new SchedSwitchHandler(layout));
        builder.put(layout.eventSchedPiSetprio(), new PiSetprioHandler(layout));
        builder.put(layout.eventSchedProcessFork(), new ProcessForkHandler(layout));
        builder.put(layout.eventSchedProcessExit(), new ProcessExitHandler(layout));
        builder.put(layout.eventSchedProcessFree(), new ProcessFreeHandler(layout));
        builder.put(layout.eventSchedProcessWaking(), new SchedWakeupHandler(layout));

        for (String s : layout.getIPIIrqVectorsEntries()) {
            builder.put(s, new IPIEntryHandler(layout));
        }
        for (String s : layout.getIPIIrqVectorsExits()) {
            builder.put(s, new IPIExitHandler(layout));
        }

        final String eventStatedumpProcessState = layout.eventStatedumpProcessState();
        if (eventStatedumpProcessState != null) {
            builder.put(eventStatedumpProcessState, new StateDumpHandler(layout));
        }

        for (String eventSchedWakeup : layout.eventsSchedWakeup()) {
            builder.put(eventSchedWakeup, new SchedWakeupHandler(layout));
        }

        return builder.build();
    }

    // ------------------------------------------------------------------------
    // IStateChangeInput
    // ------------------------------------------------------------------------

    @Override
    public int getVersion() {
        return VERSION;
    }

    @Override
    public KernelStateProvider getNewInstance() {
        return new KernelStateProvider(this.getTrace(), fLayout);
    }

    @Override
    protected void eventHandle(@Nullable ITmfEvent event) {
        if (event == null) {
            return;
        }

        final String eventName = event.getName();

        try {
            final ITmfStateSystemBuilder ss = requireNonNull(getStateSystemBuilder());
            /*
             * Feed event to the history system if it's known to cause a state
             * transition.
             */
            KernelEventHandler handler = fEventNames.get(eventName);
            if (handler == null) {
                if (isSyscallExit(eventName)) {
                    handler = fSysExitHandler;
                } else if (isSyscallEntry(eventName)) {
                    handler = fSysEntryHandler;
                }
            }
            if (handler != null) {
                handler.handleEvent(ss, event);
            }

        } catch (AttributeNotFoundException ae) {
            /*
             * This would indicate a problem with the logic of the manager here,
             * so it shouldn't happen.
             */
            Activator.instance().logError("Attribute not found: " + ae.getMessage(), ae); //$NON-NLS-1$

        } catch (TimeRangeException tre) {
            /*
             * This would happen if the events in the trace aren't ordered
             * chronologically, which should never be the case ...
             */
            Activator.instance().logError("TimeRangeExcpetion caught in the state system's event manager.\n" + //$NON-NLS-1$
                    "Are the events in the trace correctly ordered?\n" + tre.getMessage(), tre); //$NON-NLS-1$

        } catch (StateValueTypeException sve) {
            /*
             * This would happen if we were trying to push/pop attributes not of
             * type integer. Which, once again, should never happen.
             */
            Activator.instance().logError("State value error: " + sve.getMessage(), sve); //$NON-NLS-1$
        }
    }

    private boolean isSyscallEntry(String eventName) {
        return (eventName.startsWith(fLayout.eventSyscallEntryPrefix())
                || eventName.startsWith(fLayout.eventCompatSyscallEntryPrefix()));
    }

    private boolean isSyscallExit(String eventName) {
        return (eventName.startsWith(fLayout.eventSyscallExitPrefix()) ||
                eventName.startsWith(fLayout.eventCompatSyscallExitPrefix()));
    }

}
