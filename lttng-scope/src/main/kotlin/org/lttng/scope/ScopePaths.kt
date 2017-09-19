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

    val dataDir: Path
    val configDir: Path
    val cacheDir: Path

    init {
        val homeDirStr = System.getProperty("user.home")
                ?: System.getenv("HOME")
                ?: throw IllegalArgumentException("Cannot find user home directory. Try defining \$HOME.")
        val homeDir = Paths.get(homeDirStr)

        val dataDirStr = System.getenv("XDG_DATA_HOME")
        dataDir = if (dataDirStr == null) {
            homeDir.resolve(Paths.get(".local", "share", appDirSuffix))
        } else {
            Paths.get(dataDirStr, appDirSuffix)
        }

        val configDirStr = System.getenv("XDG_CONFIG_HOME")
        configDir = if (configDirStr == null) {
            homeDir.resolve(Paths.get(".config", appDirSuffix))
        } else {
            Paths.get(configDirStr, appDirSuffix)
        }

        val cacheDirStr = System.getenv("XDG_CACHE_HOME")
        cacheDir = if (cacheDirStr == null) {
            homeDir.resolve(Paths.get(".cache", appDirSuffix))
        } else {
            Paths.get(cacheDirStr, appDirSuffix)
        }
    }
}
