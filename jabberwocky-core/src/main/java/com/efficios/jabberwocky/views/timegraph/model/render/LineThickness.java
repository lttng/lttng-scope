/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.timegraph.model.render;

/**
 * Headless definitions of possible values of state interval line thicknesses
 */
public enum LineThickness {

    /** Normal, full thickness */
    NORMAL,
    /**
     * Small thickness, should be between {@link #NORMAL} and {@link #TINY} and
     * distinguishable from those two.
     */
    SMALL,
    /**
     * Tiny line thickness. The line should be as small as possible but still
     * have the color distinguishable.
     */
    TINY

}