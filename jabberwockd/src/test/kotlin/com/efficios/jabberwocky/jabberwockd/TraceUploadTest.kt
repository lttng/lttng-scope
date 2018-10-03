/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.jabberwockd

import com.efficios.jabberwocky.utils.using
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.nio.file.Files

class TraceUploadTest {

    companion object {
        private const val SERVER_URL = "http://localhost:$LISTENING_PORT/test"

        private val UTF8 = Charsets.UTF_8
        private const val CRLF = "\r\n" // Line separator required by multipart/form-data.
    }

    @Test
    @Disabled("NYI")
    fun testTraceUpload() {
        val param = "value"
        val textFile = File("/path/to/file.txt")
        val binaryFile = File("/path/to/file.bin")
        val boundary = System.currentTimeMillis().toString(16) // Just generate some unique random value.


        val connection = URL(SERVER_URL).openConnection()
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

        using {
            val output = connection.getOutputStream().autoClose()
            val writer = output.bufferedWriter().autoClose()

            with(writer) {
                // Send normal param.
                append("--$boundary").append(CRLF)
                append("Content-Disposition: form-data; name=\"param\"").append(CRLF)
                append("Content-Type: text/plain; charset=" + UTF8).append(CRLF)
                append(CRLF).append(param).append(CRLF).flush()

                // Send text file.
                append("--$boundary").append(CRLF)
                append("Content-Disposition: form-data; name=\"textFile\"; filename=\"${textFile.name}\"").append(CRLF)
                append("Content-Type: text/plain; charset=" + UTF8).append(CRLF) // Text file itself must be saved in this charset!
                append(CRLF).flush()
                Files.copy(textFile.toPath(), output)
                output.flush()
                append(CRLF).flush()

                // Send binary file.
                append("--$boundary").append(CRLF)
                append("Content-Disposition: form-data; name=\"binaryFile\"; filename=\"${binaryFile.name}\"").append(CRLF)
                append("Content-Type: " + URLConnection.guessContentTypeFromName(binaryFile.name)).append(CRLF)
                append("Content-Transfer-Encoding: binary").append(CRLF)
                append(CRLF).flush()
                Files.copy(binaryFile.toPath(), output)
                output.flush()
                append(CRLF).flush()

                // End of multipart/form-data.
                append("--$boundary--").append(CRLF).flush()
            }
        }

        // Request is lazily fired whenever you need to obtain information about response.
        val responseCode = (connection as HttpURLConnection).responseCode
        System.out.println(responseCode) // Should be 200
    }
}