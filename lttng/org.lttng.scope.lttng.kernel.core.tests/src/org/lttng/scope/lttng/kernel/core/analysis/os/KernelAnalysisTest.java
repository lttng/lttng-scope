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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.ctf.tmf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lttng.scope.lttng.kernel.core.trace.layout.internal.LttngEventLayout;

import com.efficios.jabberwocky.collection.ITraceCollection;
import com.efficios.jabberwocky.collection.TraceCollection;
import com.efficios.jabberwocky.ctf.trace.generic.GenericCtfTrace;
import com.efficios.jabberwocky.project.ITraceProject;
import com.efficios.jabberwocky.project.TraceProject;
import com.efficios.jabberwocky.trace.ITrace;
import com.google.common.io.MoreFiles;

import ca.polymtl.dorsal.libdelorean.ITmfStateSystem;

/**
 * Test the {@link KernelAnalysis} class
 *
 * @author Geneviève Bastien
 */
@NonNullByDefault({})
@SuppressWarnings("rawtypes")
public class KernelAnalysisTest {

    private static final String PROJECT_NAME = "test-proj";
    private static final KernelAnalysis ANALYSIS = new KernelAnalysis(LttngEventLayout.getInstance());

    /* Resources to dispose */
    private ITmfTrace fKernelTrace;
    private ITmfTrace fNonKernelTrace;

    private Path fProjectPath;

    private ITraceProject fKernelProject;
    private ITraceProject fNonKernelProject;


    /**
     * Set-up the test
     */
    @Before
    public void setUp() {
        try {
            fProjectPath = Files.createTempDirectory(PROJECT_NAME);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        fKernelTrace = CtfTmfTestTraceUtils.getTrace(CtfTestTrace.KERNEL);
        fNonKernelTrace = CtfTmfTestTraceUtils.getTrace(CtfTestTrace.CYG_PROFILE);


        com.efficios.jabberwocky.lttng.kernel.trace.LttngKernelTrace kTrace =
                new com.efficios.jabberwocky.lttng.kernel.trace.LttngKernelTrace(Paths.get(fKernelTrace.getPath()));
        GenericCtfTrace ustTrace = new GenericCtfTrace(Paths.get(fNonKernelTrace.getPath()));
        fKernelProject = newProject(kTrace);
        fNonKernelProject = newProject(ustTrace);
    }

    private ITraceProject newProject(ITrace trace) {
        ITraceCollection coll = new TraceCollection<>(Collections.singleton(trace));
        return new TraceProject(PROJECT_NAME, fProjectPath, Collections.singleton(coll));
    }

    /**
     * Dispose test objects
     */
    @After
    public void tearDown() {
        CtfTmfTestTraceUtils.dispose(CtfTestTrace.KERNEL);
        CtfTmfTestTraceUtils.dispose(CtfTestTrace.CYG_PROFILE);
        fKernelTrace = null;
        fNonKernelTrace = null;

        if (fProjectPath != null) {
            try {
                MoreFiles.deleteRecursively(fProjectPath);
            } catch (IOException e) {
            }
        }

    }

    @Test
    public void testAppliesTo() {
        assertTrue(ANALYSIS.appliesTo(fKernelProject));
        assertFalse(ANALYSIS.appliesTo(fNonKernelProject));
    }

    /**
     * Test the canExecute method on valid and invalid traces
     */
    @Test
    public void testCanExecute() {
        assertTrue(ANALYSIS.canExecute(fKernelProject));
        assertFalse(ANALYSIS.canExecute(fNonKernelProject));
    }

    /**
     * Test the LTTng kernel analysis execution
     */
    @Test
    public void testAnalysisExecution() {
        ITmfStateSystem ss = ANALYSIS.execute(fKernelProject, null, null);
        assertNotNull(ss);

        List<Integer> quarks = ss.getQuarks("*");
        assertFalse(quarks.isEmpty());
    }

}
