/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.tmf.core.event.aspect;

import static org.lttng.scope.common.core.NonNullUtils.nullToEmptyString;

import org.eclipse.tracecompass.ctf.tmf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;

/**
 * Channel aspect for CTF traces
 *
 * @author Alexandre Montplaisir
 */
public class CtfChannelAspect implements ITmfEventAspect<String> {

    @Override
    public String getName() {
        return nullToEmptyString(Messages.AspectName_Channel);
    }

    @Override
    public String getHelpText() {
        return nullToEmptyString(Messages.AspectHelpText_Channel);
    }

    @Override
    public String resolve(ITmfEvent event) {
        if (!(event instanceof CtfTmfEvent)) {
            return EMPTY_STRING;
        }
        return ((CtfTmfEvent) event).getChannel();
    }
}
