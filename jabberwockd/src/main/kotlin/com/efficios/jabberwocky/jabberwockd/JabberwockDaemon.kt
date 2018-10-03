/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

@file:JvmName("JabberwockDaemon")

package com.efficios.jabberwocky.jabberwockd;

import com.efficios.jabberwocky.jabberwockd.handlers.DebugHandler
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress

const val LISTENING_PORT = 8000
const val BASE_URL = ""

/**
 * Trace analysis server daemon.
 *
 * @author Alexandre Montplaisir
 */
fun main(args: Array<String>) {

    val server = HttpServer.create(InetSocketAddress(LISTENING_PORT), 0)
    server.createContext("/test", DebugHandler())
    server.executor = null; // creates a default executor
    server.start();

    println("Server started, listening on port $LISTENING_PORT")
}
