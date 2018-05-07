/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.application.actions

import com.efficios.jabberwocky.collection.TraceCollection
import com.efficios.jabberwocky.project.TraceProject
import com.efficios.jabberwocky.trace.Trace
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

/**
 * Methods to serialize/deserialize a project into its on-disk representation.
 */
object ProjectSerialization {

    private const val PROJECT_FILE_SUFFIX = ".lsf"

    /** Base "magic" property to put in the project file, should never change. */
    private const val PROJECT_FILE_TYPE_KEY = "type"
    private const val PROJECT_FILE_TYPE_VALUE = "lttng-scope-project"
    /** Bump this version if the project file contents changes. */
    private const val PROJECT_FILE_VERSION_KEY = "version"
    private const val PROJECT_FILE_VERSION_VALUE = "1.0"

    private const val PROJECT_FILE_NAME_KEY = "name"
    private const val PROJECT_FILE_TRACES_KEY = "traces"
    private const val PROJECT_FILE_TRACES_SEPARATOR = "#"

    /**
     * Take an existing, in-memory project and save it to disk at the given location.
     *
     * 'targetProjectFile' should be a file ending in ".lsf". We will also create a
     * directory with the same name (without the extension) next to it. If either the
     * file or directory already exists, the user should be notified and asked if
     * the existing file/dir should be overwritten.
     */
    fun serializeProject(traceProject: TraceProject<*, *>, targetProjectFile: Path) {
        val projectName = traceProject.name

        val parentPath = targetProjectFile.parent
        val fileName = targetProjectFile.fileName.toString()
        if (!fileName.endsWith(PROJECT_FILE_SUFFIX)) throw IllegalArgumentException("Invalid target file name.")
        val dirName = fileName.removeSuffix(PROJECT_FILE_SUFFIX)
        val dirPath = parentPath.resolve(dirName)

        if (Files.exists(targetProjectFile)
                && Files.exists(dirPath)
                && dirPath == traceProject.directory) {
            /*
             * This project was already being saved in this location. Great!
             * No need to bother the user then.
             */
        } else if (Files.exists(targetProjectFile) || Files.exists(dirPath)) {
            /*
             * There is already something in this location, but it doesn't seem to be related
             * to this project. Ask the user if he wants to delete it.
             */
            // TODO Notify/ask the user to overwrite
            val userWantsToOverwrite = true
            if (userWantsToOverwrite) {
                listOf(targetProjectFile, dirPath)
                        .forEach { if (Files.exists(it)) it.toFile().deleteRecursively() }
            } else {
                return
            }
        }

        // TODO Save the project
    }

    /**
     * Open an existing project saved on disk.
     *
     * 'projectFile' should be the file ending in ".lsf".
     */
    fun deserializeProject(projectFile: Path): TraceProject<*, *> {
        if (!Files.exists(projectFile) || !Files.isRegularFile(projectFile)) throw IllegalArgumentException("Invalid project file selected.")
        val projectName = projectFile.fileName.toString().removeSuffix(PROJECT_FILE_SUFFIX)
        val projectPath = projectFile.parent

        // TODO Load the project accordingly
        // Only using one collection for all traces at the moment.
        val traces = emptyList<Trace<*>>()
        val collection = TraceCollection(traces)
        return TraceProject(projectName, projectPath, listOf(collection))
    }

    private fun generateNewProperties(projectName: String): Properties {
        return Properties().apply {
            setProperty(PROJECT_FILE_TYPE_KEY, PROJECT_FILE_TYPE_VALUE)
            setProperty(PROJECT_FILE_VERSION_KEY, PROJECT_FILE_VERSION_VALUE)
            setProperty(PROJECT_FILE_NAME_KEY, projectName)
        }
    }

}
