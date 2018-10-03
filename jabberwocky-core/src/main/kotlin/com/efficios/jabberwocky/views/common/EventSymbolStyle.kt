/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.common

/**
 * Symbol that can be used to represent single events.
 *
 * They are defined outside of any specific view, so that multiple views can use the same symbol
 * for the same event definition.
 */
enum class EventSymbolStyle {
    CIRCLE,
    CROSS,
    STAR,
    SQUARE,
    DIAMOND,
    TRIANGLE;
}