/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.common.jfx

import javafx.event.ActionEvent
import javafx.scene.control.Button

class ActionButton(label: String, action: (ActionEvent) -> Unit) : Button(label) {

    init {
        setOnAction(action)
    }

}
