/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.jabberwockd.handlers

import com.sun.net.httpserver.Headers
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler

class DebugHandler : HttpHandler {
    override fun handle(t: HttpExchange) {

        /*
        * <ol><li>{@link #getRequestMethod()} to determine the command
        * <li>{@link #getRequestHeaders()} to examine the request headers (if needed)
        * <li>{@link #getRequestBody()} returns a {@link java.io.InputStream} for reading the request body.
        *     After reading the request body, the stream is close.
        * <li>{@link #getResponseHeaders()} to set any response headers, except content-length
        * <li>{@link #sendResponseHeaders(int,long)} to send the response headers. Must be called before
        * next step.
        * <li>{@link #getResponseBody()} to get a {@link java.io.OutputStream} to send the response body.
        *      When the response body has been written, the stream must be closed to terminate the exchange.
        * </ol>
        */
        val requestMethod = t.requestMethod
        val requestHeaders = t.requestHeaders
        val requestBody = t.requestBody
        val requestBodyString = requestBody.bufferedReader().use { it.readText() }

        println("Request URI = ${t.requestURI}")
        println("Request method = $requestMethod")
        println("Request headers = ${requestHeaders.prettyPrint()}")
        println("Request body = $requestBodyString")

        println("Local address = ${t.localAddress}")
        println("Remote address = ${t.remoteAddress}")
        println("----------")

//        val responseHeaders = t.responseHeaders
        // responseHeaders.set(...)

        val response = "This is the response"
        val responseArray = response.toByteArray()

        t.sendResponseHeaders(200, responseArray.size.toLong())
        val responseBody = t.responseBody
        responseBody.write(responseArray)
        responseBody.close()
    }
}

private fun Headers.prettyPrint(): String {
    return map { "${it.key} = ${it.value}" }.joinToString(separator = ",\n    ")
}