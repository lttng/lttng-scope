/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *   Mathieu Rail - Added tests for getting a module's requirements
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.core.tests.stubs.analysis.TestAnalysis;
import org.eclipse.tracecompass.tmf.core.tests.stubs.analysis.TestAnalysis2;
import org.eclipse.tracecompass.tmf.core.tests.stubs.trace.TmfTraceStub;
import org.eclipse.tracecompass.tmf.core.tests.stubs.trace.TmfTraceStub2;
import org.eclipse.tracecompass.tmf.core.tests.stubs.trace.TmfTraceStub3;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

/**
 * Test suite for the {@link TmfAnalysisModuleHelperConfigElement} class
 *
 * @author Geneviève Bastien
 */
public class AnalysisModuleHelperTest {

    private IAnalysisModuleHelper fModule;
    private IAnalysisModuleHelper fModuleOther;
    private ITmfTrace fTrace;

    private static IAnalysisModuleHelper getModuleHelper(@NonNull String moduleId) {
        Multimap<String, IAnalysisModuleHelper> helpers = TmfAnalysisManager.getAnalysisModules();
        Collection<IAnalysisModuleHelper> applicableHelpers = helpers.get(moduleId);
        assertEquals(1, applicableHelpers.size());
        return Iterables.getOnlyElement(applicableHelpers);
    }

    /**
     * Gets the module helpers for 2 test modules
     */
    @Before
    public void getModules() {
        fModule = getModuleHelper(AnalysisManagerTest.MODULE_PARAM);
        assertNotNull(fModule);
        assertTrue(fModule instanceof TmfAnalysisModuleHelperConfigElement);
        fModuleOther = getModuleHelper(AnalysisManagerTest.MODULE_SECOND);
        assertNotNull(fModuleOther);
        assertTrue(fModuleOther instanceof TmfAnalysisModuleHelperConfigElement);
        fTrace = TmfTestTrace.A_TEST_10K2.getTraceAsStub2();
    }

    /**
     * Some tests use traces, let's clean them here
     */
    @After
    public void cleanupTraces() {
        TmfTestTrace.A_TEST_10K.dispose();
        fTrace.dispose();
    }

    /**
     * Test the helper's getters and setters
     */
    @Test
    public void testHelperGetters() {
        /* With first module */
        assertEquals(AnalysisManagerTest.MODULE_PARAM, fModule.getId());
        assertEquals("Test analysis", fModule.getName());
        assertFalse(fModule.isAutomatic());

        Bundle helperbundle = fModule.getBundle();
        Bundle thisbundle = Platform.getBundle("org.eclipse.tracecompass.tmf.core.tests");
        assertNotNull(helperbundle);
        assertEquals(thisbundle, helperbundle);

        /* With other module */
        assertEquals(AnalysisManagerTest.MODULE_SECOND, fModuleOther.getId());
        assertEquals("Test other analysis", fModuleOther.getName());
        assertTrue(fModuleOther.isAutomatic());
    }

    /**
     * Test the
     * {@link TmfAnalysisModuleHelperConfigElement#appliesToTraceType(Class)}
     * method for the 2 modules
     */
    @Test
    public void testAppliesToTrace() {
        /* stub module */
        assertFalse(fModule.appliesToTraceType(TmfTrace.class));
        assertTrue(fModule.appliesToTraceType(TmfTraceStub.class));
        assertTrue(fModule.appliesToTraceType(TmfTraceStub2.class));
        assertFalse(fModule.appliesToTraceType(TmfTraceStub3.class));
        assertFalse(fModule.appliesToTraceType(TmfExperiment.class));

        /* stub module 2 */
        assertFalse(fModuleOther.appliesToTraceType(TmfTrace.class));
        assertFalse(fModuleOther.appliesToTraceType(TmfTraceStub.class));
        assertTrue(fModuleOther.appliesToTraceType(TmfTraceStub2.class));
        assertTrue(fModuleOther.appliesToTraceType(TmfTraceStub3.class));
        assertTrue(fModuleOther.appliesToTraceType(TmfExperiment.class));
    }

    /**
     * Test the
     * {@link TmfAnalysisModuleHelperConfigElement#newModule(ITmfTrace)} method
     * for the 2 modules
     */
    @Test
    public void testNewModule() {
        /* Test analysis module with traceStub */
        IAnalysisModule module = null;
        try {
            module = fModule.newModule(TmfTestTrace.A_TEST_10K.getTrace());
            assertNotNull(module);
            assertTrue(module instanceof TestAnalysis);
        } catch (TmfAnalysisException e) {
            fail();
        } finally {
            if (module != null) {
                module.dispose();
            }
        }

        /* TestAnalysis2 module with trace, should return an exception */
        try {
            module = fModuleOther.newModule(TmfTestTrace.A_TEST_10K.getTrace());
            assertNull(module);
        } catch (TmfAnalysisException e) {
            fail();
        } finally {
            if (module != null) {
                module.dispose();
            }
        }

        /* TestAnalysis2 module with a TraceStub2 */
        ITmfTrace trace = fTrace;
        assertNotNull(trace);
        try {
            module = fModuleOther.newModule(trace);
            assertNotNull(module);
            assertTrue(module instanceof TestAnalysis2);
        } catch (TmfAnalysisException e) {
            fail();
        } finally {
            if (module != null) {
                module.dispose();
            }
        }
    }

    /**
     * Test for the initialization of parameters from the extension points
     */
    @Test
    public void testParameters() {
        ITmfTrace trace = TmfTestTrace.A_TEST_10K.getTrace();

        /*
         * This analysis has a parameter, but no default value. we should be
         * able to set the parameter
         */
        IAnalysisModuleHelper helper = getModuleHelper(AnalysisManagerTest.MODULE_PARAM);
        assertNotNull(helper);
        IAnalysisModule module = null;
        try {
            module = helper.newModule(trace);
            assertNotNull(module);
            assertNull(module.getParameter(TestAnalysis.PARAM_TEST));
            module.setParameter(TestAnalysis.PARAM_TEST, 1);
            assertEquals(1, module.getParameter(TestAnalysis.PARAM_TEST));

        } catch (TmfAnalysisException e1) {
            fail(e1.getMessage());
            return;
        } finally {
            if (module != null) {
                module.dispose();
            }
        }

        /* This module has a parameter with default value */
        helper = getModuleHelper(AnalysisManagerTest.MODULE_PARAM_DEFAULT);
        assertNotNull(helper);
        try {
            module = helper.newModule(trace);
            assertNotNull(module);
            assertEquals(3, module.getParameter(TestAnalysis.PARAM_TEST));
            module.setParameter(TestAnalysis.PARAM_TEST, 1);
            assertEquals(1, module.getParameter(TestAnalysis.PARAM_TEST));

        } catch (TmfAnalysisException e1) {
            fail(e1.getMessage());
            return;
        } finally {
            if (module != null) {
                module.dispose();
            }
        }

        /*
         * This module does not have a parameter so setting it should throw an
         * error
         */
        helper = getModuleHelper(AnalysisManagerTest.MODULE_SECOND);
        assertNotNull(helper);
        Exception exception = null;
        trace = fTrace;
        assertNotNull(trace);
        try {
            module = helper.newModule(trace);
            assertNotNull(module);
            assertNull(module.getParameter(TestAnalysis.PARAM_TEST));

            try {
                module.setParameter(TestAnalysis.PARAM_TEST, 1);
            } catch (RuntimeException e) {
                exception = e;
            }
        } catch (TmfAnalysisException e1) {
            fail(e1.getMessage());
            return;
        } finally {
            if (module != null) {
                module.dispose();
            }
        }
        assertNotNull(exception);
    }

    /**
     * Test for the
     * {@link TmfAnalysisModuleHelperConfigElement#getValidTraceTypes} method
     */
    @Test
    public void testGetValidTraceTypes() {
        Set<Class<? extends ITmfTrace>> expected = ImmutableSet.of(TmfTraceStub.class, TmfTraceStub2.class);
        Iterable<Class<? extends ITmfTrace>> traceTypes = fModule.getValidTraceTypes();
        assertEquals(expected, traceTypes);
    }
}
