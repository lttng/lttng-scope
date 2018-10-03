/*
 * Copyright (c) 2017 EfficiOS Inc.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.testutils

import org.eclipse.tracecompass.ctf.core.CTFException
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace
import org.lttng.scope.ttt.ctf.CtfTestTrace
import java.io.IOException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarFile

/**
 * Wrapper around [CtfTestTrace] that will extract the resources to a
 * temporary directory, and take care of deleting it once the [.close]
 * method is invoked.

 * If you keep references to these objects, make sure you call [.close]
 * on them, or else you might leave dangling temporary files around!

 * @author Alexandre Montplaisir
 */
internal class CtfTestTraceExtractor
/**
 * Constructor

 * @param tracePath
 * *            Path to the actual trace
 * *
 * @param parentPath
 * *            Top-level extracted directory. Will be deleted on cleanup.
 * *
 * @throws CTFException
 * *             If something goes wrong
 */
private constructor(tracePath: Path, private val parentPath: Path) : AutoCloseable {

    val trace: CTFTrace = CTFTrace(tracePath.toString())

    override fun close() {
        /* Delete the parent directory of this trace */
        parentPath.toFile().deleteRecursively()
    }

    companion object {

        /**
         * Extract the given

         * @param trace
         * *            The test trace to extract
         * *
         * @return The wrapper object you can close() to delete the temporary trace
         */
        fun extractTestTrace(trace: CtfTestTrace): CtfTestTraceExtractor {
            try {
                val parentPath = Files.createTempDirectory("test-trace")
                val tracePath = extractUrlToTempDir(trace.traceURL, parentPath)
                return CtfTestTraceExtractor(tracePath, parentPath)
            } catch (e: IOException) {
                throw IllegalStateException(e)
            } catch (e: CTFException) {
                throw IllegalStateException(e)
            }

        }

        private fun extractUrlToTempDir(sourceURL: URL, destinationPath: Path): Path {
            val urlString = sourceURL.file
            val elems = urlString.split("!")
            // Remove the "file:"
            val jarFileName = elems[0].substring(5)
            // Remove the starting "/"
            val resourcePath = elems[1].substring(1)

            JarFile(jarFileName).use({ jar ->
                jar.entries().toList()
                        /* Get the "top-level" directory of the trace we want to extract */
                        .filter { it.name.startsWith(resourcePath + "/") }
                        .sortedBy { it.name }
                        .forEach { entry ->
                            val name = entry.name
                            try {
                                if (name.endsWith("/")) {
                                    /*
                                     * This a directory. We will create it on the file
                                     * system
                                     */
                                    /*
                                     * Note that in the list of JarEntries, directories are
                                     * always placed first before the files, luckily.
                                     */
                                    val dirName = name.substring(0, name.length)
                                    val dir = destinationPath.resolve(dirName)
                                    Files.createDirectories(dir)
                                } else {
                                    /*
                                     * This is a file, extract it in the corresponding
                                     * location
                                     */
                                    jar.getInputStream(entry).use({ istream ->
                                        val fileName = destinationPath.resolve(name)
                                        Files.copy(istream, fileName)
                                    })
                                }
                            } catch (e: IOException) {
                                System.err.println("Error extracting trace") //$NON-NLS-1$
                                e.printStackTrace()
                            }
                        }
            })

            return destinationPath.resolve(resourcePath)
        }

    }

}
