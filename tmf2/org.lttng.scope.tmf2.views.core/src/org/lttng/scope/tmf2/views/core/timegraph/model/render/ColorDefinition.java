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

/**
 * Basic headless color definition, using RGB(A) values.
 *
 * @author Alexandre Montplaisir
 */
public class ColorDefinition {

    /** Minimum value for the parameters */
    public static final int MIN = 0;
    /** Maximum value for the parameters */
    public static final int MAX = 255;

    /** Red RGB component */
    public final int fRed;
    /** Green RGB component */
    public final int fGreen;
    /** Blue RGB component */
    public final int fBlue;
    /** Alpha component */
    public final int fAlpha;

    /**
     * Specify a color by RGB values, using maximum (full) alpha.
     *
     * @param red
     *            Red component, from 0 to 255
     * @param green
     *            Green component, from 0 to 255
     * @param blue
     *            Blue component, from 0 to 255
     */
    public ColorDefinition(int red, int green, int blue) {
        this(red, green, blue, MAX);
    }

    /**
     * Specify a color by RGBA values.
     *
     * @param red
     *            Red component, from 0 to 255
     * @param green
     *            Green component, from 0 to 255
     * @param blue
     *            Blue component, from 0 to 255
     * @param alpha
     *            Alpha (opacity) component, from 0 to 255
     */
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
        return (fRed == other.fRed
                && fGreen == other.fGreen
                && fBlue == other.fBlue
                && fAlpha == other.fAlpha);
    }

    @Override
    public String toString() {
        return IntStream.of(fRed, fGreen, fBlue, fAlpha)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(", ", "[", "]")); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
    }

}
