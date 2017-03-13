/*******************************************************************************
 * Copyright (c) 2014, 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.lttng.scope.lttng.kernel.core.analysis.os;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.ctf.tmf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.ctf.tmf.core.trace.CtfTmfTrace;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lttng.scope.lttng.kernel.core.tests.shared.LttngKernelTestTraceUtils;
import org.lttng.scope.lttng.kernel.core.trace.LttngKernelTrace;

import ca.polymtl.dorsal.libdelorean.ITmfStateSystem;

/**
 * Test the {@link KernelAnalysisModule} class
 *
 * @author Geneviève Bastien
 */
@NonNullByDefault({})
public class LttngKernelAnalysisTest {

    private LttngKernelTrace fTrace;
    private KernelAnalysisModule fKernelAnalysisModule;

    /**
     * Set-up the test
     */
    @Before
    public void setUp() {
        fKernelAnalysisModule = new KernelAnalysisModule();
        fTrace = LttngKernelTestTraceUtils.getTrace(CtfTestTrace.KERNEL);
    }

    /**
     * Dispose test objects
     */
    @After
    public void tearDown() {
        LttngKernelTestTraceUtils.dispose(CtfTestTrace.KERNEL);
        fKernelAnalysisModule.dispose();
        fTrace = null;
        fKernelAnalysisModule = null;
    }

    /**
     * Test the LTTng kernel analysis execution
     */
    @Test
    public void testAnalysisExecution() {
        fKernelAnalysisModule.setId("test");
        ITmfTrace trace = fTrace;
        assertNotNull(trace);
        try {
            assertTrue(fKernelAnalysisModule.setTrace(trace));
        } catch (TmfAnalysisException e) {
            fail(e.getMessage());
        }
        // Assert the state system has not been initialized yet
        ITmfStateSystem ss = fKernelAnalysisModule.getStateSystem();
        assertNull(ss);

        assertTrue(executeAnalysis(fKernelAnalysisModule));

        ss = fKernelAnalysisModule.getStateSystem();
        assertNotNull(ss);

        List<Integer> quarks = ss.getQuarks("*");
        assertFalse(quarks.isEmpty());
    }

    /**
     * Test the canExecute method on valid and invalid traces
     */
    @Test
    public void testCanExecute() {
        /* Test with a valid kernel trace */
        assertNotNull(fTrace);
        assertTrue(fKernelAnalysisModule.canExecute(fTrace));

        /* Test with a CTF trace that does not have required events */
        CtfTmfTrace trace = CtfTmfTestTraceUtils.getTrace(CtfTestTrace.CYG_PROFILE);
        /*
         * TODO: This should be false, but for now there is no mandatory events
         * in the kernel analysis so it will return true.
         */
        assertTrue(fKernelAnalysisModule.canExecute(trace));
        CtfTmfTestTraceUtils.dispose(CtfTestTrace.CYG_PROFILE);
    }

    /**
     * Calls the {@link TmfAbstractAnalysisModule#executeAnalysis} method of an
     * analysis module. This method does not return until the analysis is
     * completed and it returns the result of the method. It allows to execute
     * the analysis without requiring an Eclipse job and waiting for completion.
     *
     * Note that executing an analysis using this method will not automatically
     * execute the dependent analyses module. The execution of those modules is
     * left to the caller.
     *
     * @param module
     *            The analysis module to execute
     * @return The return value of the
     *         {@link TmfAbstractAnalysisModule#executeAnalysis} method
     */
    private static boolean executeAnalysis(TmfAbstractAnalysisModule module) {
        try {
            Class<?>[] argTypes = new Class[] { IProgressMonitor.class };
            Method method = TmfAbstractAnalysisModule.class.getDeclaredMethod("executeAnalysis", argTypes);
            method.setAccessible(true);
            Object obj = method.invoke(module, new NullProgressMonitor());
            return (Boolean) obj;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            fail(e.toString());
            throw new RuntimeException(e);
        }
    }

}
