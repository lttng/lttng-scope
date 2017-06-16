/*******************************************************************************
 * Copyright (c) 2014, 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.stubs.analysis;

import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import ca.polymtl.dorsal.libdelorean.ITmfStateSystemBuilder;
import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;
import ca.polymtl.dorsal.libdelorean.exceptions.StateValueTypeException;
import ca.polymtl.dorsal.libdelorean.exceptions.TimeRangeException;
import ca.polymtl.dorsal.libdelorean.statevalue.TmfStateValue;

/**
 * Stubs for experiment analysis. This analysis is a state system analysis that
 * simply counts the number of traces for which events were received. The number
 * of traces is the value of attribute
 * {@link TestExperimentAnalysis#TRACE_QUARK_NAME}.
 *
 * @author Geneviève Bastien
 */
public class TestExperimentAnalysis extends TmfStateSystemAnalysisModule {

    /**
     * The quark counting the number of traces
     */
    public static final String TRACE_QUARK_NAME = "Traces";

    @Override
    protected ITmfStateProvider createStateProvider() {
        return new TestExpStateSystemProvider(requireNonNull(getTrace()));
    }

    @Override
    protected StateSystemBackendType getBackendType() {
        return StateSystemBackendType.INMEM;
    }

    private class TestExpStateSystemProvider extends AbstractTmfStateProvider {

        private static final int VERSION = 1;
        private final Set<ITmfTrace> fTraces = new HashSet<>();
        private int fCount = 0;

        /**
         * Constructor
         *
         * @param trace
         *            The LTTng 2.0 kernel trace directory
         */
        public TestExpStateSystemProvider(@NonNull ITmfTrace trace) {
            super(trace, "Stub State System for Experiment");
        }

        @Override
        public int getVersion() {
            return VERSION;
        }

        @Override
        public ITmfStateProvider getNewInstance() {
            return new TestExpStateSystemProvider(this.getTrace());
        }

        @Override
        protected void eventHandle(ITmfEvent event) {
            ITmfStateSystemBuilder ss = requireNonNull(getStateSystemBuilder());
            if (!fTraces.contains(event.getTrace())) {
                try {
                    int quarkId = ss.getQuarkAbsoluteAndAdd(TRACE_QUARK_NAME);
                    ss.modifyAttribute(event.getTimestamp().getValue(), TmfStateValue.newValueInt(++fCount), quarkId);
                    fTraces.add(event.getTrace());
                } catch (TimeRangeException | StateValueTypeException | AttributeNotFoundException e) {

                }
            }
        }
    }

    @Override
    protected void setupAggregationRules(@NonNull ITmfStateSystemBuilder ss) {
    }
}
