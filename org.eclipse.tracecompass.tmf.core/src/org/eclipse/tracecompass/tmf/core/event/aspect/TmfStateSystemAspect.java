/*******************************************************************************
 * Copyright (c) 2014, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event.aspect;

import static java.util.Objects.requireNonNull;
import static org.lttng.scope.common.core.NonNullUtils.nullToEmptyString;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

import ca.polymtl.dorsal.libdelorean.IStateSystemReader;
import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;
import ca.polymtl.dorsal.libdelorean.exceptions.StateSystemDisposedException;
import ca.polymtl.dorsal.libdelorean.statevalue.StateValue;

/**
 * Aspect representing a query in a given state system, at the timestamp of the
 * event.
 *
 * This is a good example of how aspects can be "indirect" with regards to their
 * events.
 *
 * @author Alexandre Montplaisir
 */
public class TmfStateSystemAspect implements ITmfEventAspect<String> {

    private final @Nullable String fName;
    private final IStateSystemReader fSS;
    private final int fAttribute;

    /**
     * Constructor
     *
     * @param name
     *            The name of this aspect. You can use 'null' to use the
     *            default name, which is the (base) name of the attribute.
     * @param ss
     *            The state system in which we want to query
     * @param attributeQuark
     *            The quark of the attribute in the state system to look for
     */
    public TmfStateSystemAspect(@Nullable String name, IStateSystemReader ss, int attributeQuark) {
        fName = name;
        fSS = ss;
        fAttribute = attributeQuark;
    }

    @Override
    public String getName() {
        String name = fName;
        if (name != null) {
            return name;
        }

        name = fSS.getFullAttributePath(fAttribute);
        return name;
    }

    @Override
    public @NonNull String getHelpText() {
        return nullToEmptyString(NLS.bind(Messages.AspectHelpText_Statesystem,
                fSS.getSSID(), fSS.getFullAttributePath(fAttribute)));
    }

    @Override
    public @Nullable String resolve(ITmfEvent event) {
        try {
            StateValue value = fSS.querySingleState(event.getTimestamp().getValue(), fAttribute).getStateValue();
            return requireNonNull(value.toString());
        } catch (StateSystemDisposedException | AttributeNotFoundException e) {
            return null;
        }
    }
}
