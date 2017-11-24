/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope

import java.nio.file.Path
import java.nio.file.Paths

/**
 * File system paths that will be used by the application.
 *
 * They are loaded once at start time, then stay constant for the lifetime
 * of the application.
 *
 * TODO Define paths to use for Windows and macOS
 */
object ScopePaths {

    private const val appDirSuffix = "lttng-scope"
    private const val projectsDirSuffix = "projects"

    val homeDir = System.getProperty("user.home")?.let { Paths.get(it) }
            ?: System.getenv("HOME")?.let { Paths.get(it) }
            ?: throw IllegalArgumentException("Cannot find user home directory. Try defining \$HOME.")

    val dataDir = System.getenv("XDG_DATA_HOME")
            ?.let { Paths.get(it, appDirSuffix) }
            ?: homeDir.resolve(Paths.get(".local", "share", appDirSuffix))

    val configDir = System.getenv("XDG_CONFIG_HOME")
            ?.let { Paths.get(it, appDirSuffix) }
            ?: homeDir.resolve(Paths.get(".config", appDirSuffix))

    val cacheDir = System.getenv("XDG_CACHE_HOME")
            ?.let { Paths.get(it, appDirSuffix) }
            ?: homeDir.resolve(Paths.get(".cache", appDirSuffix))

    /** Subdirectory to store Jabberwocky trace projects. Should go under 'dataDir' */
    val projectsDir: Path = dataDir.resolve(projectsDirSuffix)
}
