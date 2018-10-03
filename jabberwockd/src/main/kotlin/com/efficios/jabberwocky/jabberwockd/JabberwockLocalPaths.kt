/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.jabberwockd

import java.nio.file.Path
import java.nio.file.Paths

/**
 * Local file system paths that will be used by the daemon.
 *
 * TODO Define paths to use for Windows and macOS
 */
object JabberwockLocalPaths {

    private const val APP_DIR_SUFFIX = "jabberwockd"
    private const val TRACES_DIR_SUFFIX = "traces"
    private const val PROJECTS_DIR_SUFFIX = "projects"

    val homeDir = System.getProperty("user.home")?.let { Paths.get(it) }
            ?: System.getenv("HOME")?.let { Paths.get(it) }
            ?: throw IllegalArgumentException("Cannot find user home directory. Try defining \$HOME.")

    val dataDir: Path = System.getenv("XDG_DATA_HOME")
            ?.let { Paths.get(it, APP_DIR_SUFFIX) }
            ?: homeDir.resolve(Paths.get(".local", "share", APP_DIR_SUFFIX))

    val configDir: Path = System.getenv("XDG_CONFIG_HOME")
            ?.let { Paths.get(it, APP_DIR_SUFFIX) }
            ?: homeDir.resolve(Paths.get(".config", APP_DIR_SUFFIX))

    val cacheDir: Path = System.getenv("XDG_CACHE_HOME")
            ?.let { Paths.get(it, APP_DIR_SUFFIX) }
            ?: homeDir.resolve(Paths.get(".cache", APP_DIR_SUFFIX))

    /** Subdirectory to store input traces. Should go under 'dataDir' */
    val tracesDir: Path = dataDir.resolve(TRACES_DIR_SUFFIX)

    /** Subdirectory to store Jabberwocky trace projects. Should go under 'dataDir' */
    val projectsDir: Path = dataDir.resolve(PROJECTS_DIR_SUFFIX)
}
