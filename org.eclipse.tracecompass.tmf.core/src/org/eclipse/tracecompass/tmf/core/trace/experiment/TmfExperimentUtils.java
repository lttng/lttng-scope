/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.trace.experiment;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * This utility class contains some utility methods to retrieve specific traces
 * or analysis in an experiment.
 *
 * @author Geneviève Bastien
 */
@NonNullByDefault
public final class TmfExperimentUtils {

    private TmfExperimentUtils() {

    }

    // ------------------------------------------------------------------------
    // Utility methods for analysis modules
    // ------------------------------------------------------------------------

    private static Iterable<ITmfTrace> getTracesFromHost(TmfExperiment experiment, String hostId) {
        Collection<ITmfTrace> hostTraces = new HashSet<>();
        for (ITmfTrace trace : experiment.getTraces()) {
            if (trace.getHostId().equals(hostId)) {
                hostTraces.add(trace);
            }
        }
        return hostTraces;
    }

}
