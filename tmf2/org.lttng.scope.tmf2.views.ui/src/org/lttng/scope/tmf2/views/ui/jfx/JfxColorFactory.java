/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.tmf2.views.ui.jfx;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.efficios.jabberwocky.views.common.ColorDefinition;

import javafx.scene.paint.Color;

public final class JfxColorFactory {

    private JfxColorFactory() {}

    private static final Map<ColorDefinition, Color> COLOR_MAP = new ConcurrentHashMap<>();
    private static final Map<ColorDefinition, Color> DERIVED_COLOR_MAP = new ConcurrentHashMap<>();

    /**
     * Instantiate a {@link Color} from a {@link ColorDefinition} object.
     *
     * @param colorDef
     *            The ColorDefinition
     * @return The Color object
     */
    public static synchronized Color getColorFromDef(ColorDefinition colorDef) {
        Color color = COLOR_MAP.get(colorDef);
        if (color == null) {
            color = Color.rgb(colorDef.getRed(), colorDef.getGreen(), colorDef.getBlue(), (double) colorDef.getAlpha() / (double) ColorDefinition.MAX);
            COLOR_MAP.put(colorDef, color);
        }
        return color;
    }

    public static synchronized Color getDerivedColorFromDef(ColorDefinition colorDef) {
        Color color = DERIVED_COLOR_MAP.get(colorDef);
        if (color == null) {
            color = getColorFromDef(colorDef);
            color = color.desaturate().darker();
           DERIVED_COLOR_MAP.put(colorDef, color);
        }
        return color;
    }

    /**
     * Convert a JavaFX {@link Color} to its equivalent {@link ColorDefinition}.
     *
     * @param color
     *            The color to convert
     * @return A corresponding ColorDefinition
     */
    public static ColorDefinition colorToColorDef(Color color) {
        /*
         * ColorDefintion works with integer values 0 to 255, but JavaFX colors
         * works with doubles 0.0 to 0.1
         */
        int red = (int) Math.round(color.getRed() * 255);
        int green = (int) Math.round(color.getGreen() * 255);
        int blue = (int) Math.round(color.getBlue() * 255);
        int opacity = (int) Math.round(color.getOpacity() * 255);
        return new ColorDefinition(red, green, blue, opacity);
    }
}
