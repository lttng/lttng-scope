/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.core;

/**
 * Uncommon math utility methods.
 *
 * @author Alexandre Montplaisir
 */
public final class MathUtils {

    private MathUtils() {}

    /**
     * Find the multiple of 'multipleOf' that is greater but closest to
     * 'number'. If 'number' is already a multiple of 'multipleOf', the same
     * value will be returned.
     *
     * @param number
     *            The starting number
     * @param multipleOf
     *            We want the returned value to be a multiple of this number
     * @return The closest, greater multiple
     */
    public static long roundToClosestHigherMultiple(long number, long multipleOf) {
        return (long) (Math.ceil((double) number / multipleOf) * multipleOf);
    }
}
