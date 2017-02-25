/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.tmf2.views.core.timegraph.model.render;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.Nullable;

public class ColorDefinition {

    public static final int MIN = 0;
    public static final int MAX = 255;

    public final int fRed;
    public final int fGreen;
    public final int fBlue;
    public final int fAlpha;

    public ColorDefinition(int red, int green, int blue) {
        this(red, green, blue, MAX);
    }

    public ColorDefinition(int red, int green, int blue, int alpha) {
        checkValue(red);
        checkValue(green);
        checkValue(blue);
        checkValue(alpha);

        fRed = red;
        fGreen = green;
        fBlue = blue;
        fAlpha = alpha;
    }

    private static void checkValue(int value) throws IllegalArgumentException {
        if (value < MIN || value > MAX) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(fRed, fGreen, fBlue, fAlpha);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ColorDefinition other = (ColorDefinition) obj;
        if (fAlpha != other.fAlpha ||
                fBlue != other.fBlue ||
                fGreen != other.fGreen ||
                fRed != other.fRed) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return IntStream.of(fRed, fGreen, fBlue, fAlpha)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(", ", "[", "]")); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
    }

}
