/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.ctftestsuite;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.core.trace.CTFTraceReader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Parameterized test class running the CTF Test Suite
 *
 * (from https://github.com/efficios/ctf-testsuite).
 *
 * @author Alexandre Montplaisir
 */
// TODO Extract the test-suite so we can run these tests
@Disabled
class CtfTestSuiteTest {

    private static final Path BASE_PATH = Paths.get("traces", "ctf-testsuite", "tests", "1.8");

    /**
     * Test we know are currently failing. Ignore them so we can at least run
     * the others.
     *
     * TODO Actually fix them!
     */
    private static final Path[] IGNORED_TESTS = {
            BASE_PATH.resolve(Paths.get("regression", "metadata", "pass", "sequence-typedef-length")),
            BASE_PATH.resolve(Paths.get("regression", "stream", "pass", "integer-large-size")),
    };

    // ------------------------------------------------------------------------
    // Methods for the Parametrized runner
    // ------------------------------------------------------------------------

    /**
     * Get the existing trace paths in the CTF-Testsuite git tree.
     *
     * @return The list of CTF traces (directories) to test
     */
    private static Iterable<Arguments> createTracePaths() {
        final List<Arguments> dirs = new LinkedList<>();

        addDirsFrom(dirs, BASE_PATH.resolve(Paths.get("fuzzing", "metadata", "fail")), false);
        addDirsFrom(dirs, BASE_PATH.resolve(Paths.get("fuzzing", "metadata", "pass")), true);
        addDirsFrom(dirs, BASE_PATH.resolve(Paths.get("fuzzing", "stream", "fail")), false);
        addDirsFrom(dirs, BASE_PATH.resolve(Paths.get("fuzzing", "stream", "pass")), true);

        addDirsFrom(dirs, BASE_PATH.resolve(Paths.get("regression", "metadata", "fail")), false);
        addDirsFrom(dirs, BASE_PATH.resolve(Paths.get("regression", "metadata", "pass")), true);
        addDirsFrom(dirs, BASE_PATH.resolve(Paths.get("regression", "stream", "fail")), false);
        addDirsFrom(dirs, BASE_PATH.resolve(Paths.get("regression", "stream", "pass")), true);

        addDirsFrom(dirs, BASE_PATH.resolve(Paths.get("stress", "metadata", "fail")), false);
        addDirsOneLevelDeepFrom(dirs, BASE_PATH.resolve(Paths.get("stress", "metadata", "pass")), true);
        addDirsFrom(dirs, BASE_PATH.resolve(Paths.get("stress", "stream", "fail")), false);
        addDirsOneLevelDeepFrom(dirs, BASE_PATH.resolve(Paths.get("stress", "stream", "pass")), true);

        return dirs;
    }

    private static void addDirsFrom(List<Arguments> dirs, Path path, boolean expectSuccess) {
        if (!Files.exists(path)) {
            /* Some planned directories may not exist yet in the test suite */
            return;
        }
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(path, DIR_FILTER);) {
            for (Path p : ds) {
                /* Add this test case to the list of tests to run */
                Arguments arguments = Arguments.of(p.toString(), expectSuccess);
                dirs.add(arguments);
            }
        } catch (IOException e) {
            /* Something is wrong with the layout of the test suite? */
            e.printStackTrace();
        }
    }

    /**
     * Some test traces are not in pass/trace1, pass/trace2, etc. but rather
     * pass/test1/trace1, pass/test1/trace2, etc.
     *
     * This methods adds the directories one level "down" instead of the very
     * next level.
     */
    private static void addDirsOneLevelDeepFrom(List<Arguments> dirs, Path path, boolean expectSuccess) {
        if (!Files.exists(path)) {
            return;
        }
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(path, DIR_FILTER);) {
            for (Path p : ds) {
                addDirsFrom(dirs, p, expectSuccess);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final DirectoryStream.Filter<Path> DIR_FILTER =
            new DirectoryStream.Filter<Path>() {
                @Override
                public boolean accept(Path entry) {
                    /* Only accept directories and non-blacklisted tests */
                    if (!Files.isDirectory(entry)) {
                        return false;
                    }
                    for (Path ignoredTestPath : IGNORED_TESTS) {
                        if (entry.equals(ignoredTestPath)) {
                            System.err.println("Skipping test " + entry.toString() + " as requested.");
                            return false;
                        }
                    }
                    return true;
                }
            };

    // ------------------------------------------------------------------------
    // Test methods
    // ------------------------------------------------------------------------

    /**
     * Test opening and reading the trace
     */
    @ParameterizedTest
    @MethodSource("createTracePaths")
    void testTrace(String tracePath, boolean expectSuccess) {
        assertTimeout(Duration.ofMinutes(1), () -> {
            try {
                /* Instantiate the trace (which implies parsing the metadata) */
                CTFTrace trace = new CTFTrace(tracePath);
                /* Read the trace until the end */
                try (CTFTraceReader reader = new CTFTraceReader(trace);) {

                    reader.getCurrentEventDef();
                    while (reader.advance()) {
                        assertNotNull(reader.getCurrentEventDef());
                    }

                    checkIfWeShouldSucceed(tracePath, expectSuccess);
                }
            } catch (CTFException | OutOfMemoryError e) {
                checkIfWeShouldFail(tracePath, expectSuccess, e);
            }
        });
    }

    private void checkIfWeShouldSucceed(String tracePath, boolean expectSuccess) {
        if (!expectSuccess) {
            fail("Trace was expected to fail parsing: " + tracePath);
        }
    }

    private void checkIfWeShouldFail(String tracePath, boolean expectSuccess, Throwable e) {
        if (expectSuccess) {
            fail("Trace was expected to succeed, but failed parsing: " +
                    tracePath + " (" + e.getMessage() + ")");
        }
    }
}
