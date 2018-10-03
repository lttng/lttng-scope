/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.jabberwockd

object HttpConstants {

    object Methods {
        const val GET = "GET"
        const val PUT = "PUT"
        const val DELETE = "DELETE"
    }

    object ReturnCodes {
        /** Success */
        const val R_200 = 200
        /** Success, resulted in resource creation */
        const val R_201 = 201

        /** Requested resource not found */
        const val R_404 = 404
        /** Operation not allowed (i.e. deleting a read-only resource) */
        const val R_405 = 405
    }

}