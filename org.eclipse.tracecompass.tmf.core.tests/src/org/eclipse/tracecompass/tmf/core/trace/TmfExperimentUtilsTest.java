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

package org.eclipse.tracecompass.tmf.core.trace;

import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.core.tests.stubs.trace.TmfExperimentStub;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperimentUtils;
import org.junit.After;
import org.junit.Before;

/**
 * Test the {@link TmfExperimentUtils} class
 *
 * @author Geneviève Bastien
 */
public class TmfExperimentUtilsTest {

    private static final String EXPERIMENT = "MyExperiment";
    private static int BLOCK_SIZE = 1000;

    private TmfExperimentStub fExperiment;
    private ITmfTrace[] fTraces;

    /**
     * Setup the experiment
     */
    @Before
    public void setupExperiment() {
        fTraces = new ITmfTrace[2];
        fTraces[0] = TmfTestTrace.A_TEST_10K.getTrace();
        fTraces[1] = TmfTestTrace.A_TEST_10K2.getTraceAsStub2();
        /* Re-register the trace to the signal manager */
        TmfSignalManager.register(fTraces[1]);
        fExperiment = new TmfExperimentStub(EXPERIMENT, fTraces, BLOCK_SIZE);
        fExperiment.getIndexer().buildIndex(0, TmfTimeRange.ETERNITY, true);
        fExperiment.broadcast(new TmfTraceOpenedSignal(this, fExperiment, null));
    }

    /**
     * Cleanup after the test
     */
    @After
    public void cleanUp() {
        fExperiment.dispose();
    }

}
