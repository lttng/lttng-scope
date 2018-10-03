/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.jabberwockd.handlers

import com.efficios.jabberwocky.jabberwockd.HttpConstants
import com.efficios.jabberwocky.jabberwockd.JabberwockLocalPaths
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import java.nio.file.Files
import kotlin.streams.toList

class TracesHandler : HttpHandler {

    companion object {
        const val SUB_PATH = "/traces"
    }

    override fun handle(exchange: HttpExchange) {

        val requestUri = exchange.requestURI.toString()

        /* Handle top-level "/traces" operations */
        if (requestUri == SUB_PATH) {
            when (exchange.requestMethod) {
                HttpConstants.Methods.GET -> TODO() // list traces
                HttpConstants.Methods.PUT -> TODO() // upload a new trace
                HttpConstants.Methods.DELETE -> TODO() // not allowed, return 405
            }
            return
        }

        /* Operations on an individual trace */
        val uriElements = requestUri.split("/")
        val traceId = uriElements[1]
        if (!listAvailableTraces().contains(traceId)) {
            TODO() // return 404
        }
        // TODO Print trace info, etc.

//        val requestMethod = exchange.requestMethod
//        val requestHeaders = exchange.requestHeaders
//        val requestBody = exchange.requestBody
//        val requestBodyString = requestBody.bufferedReader().use { it.readText() }

//        val responseHeaders = exchange.responseHeaders
        // responseHeaders.set(...)

        val response = "This is the response"
        val responseArray = response.toByteArray()

        exchange.sendResponseHeaders(200, responseArray.size.toLong())
        val responseBody = exchange.responseBody
        responseBody.write(responseArray)
        responseBody.close()
    }
}

private fun listAvailableTraces(): List<String> {
    val tracesDir = JabberwockLocalPaths.tracesDir
    if (!Files.exists(tracesDir)) return emptyList()
    return Files.list(tracesDir).toList()
            .filter { Files.isDirectory(it) }
            .map { it.fileName.toString() }
}