/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.tmf2.views.ui.timegraph.swtjfx;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.lttng.scope.tmf2.views.core.timegraph.model.render.ColorDefinition;

import javafx.scene.paint.Color;

final class JfxColorFactory {

    private JfxColorFactory() {}

    private static final Map<ColorDefinition, Color> COLOR_MAP = new ConcurrentHashMap<>();

    /**
     * Instantiate a {@link Color} from a {@link ColorDefinition} object.
     *
     * @param colorDef
     *            The ColorDefinition
     * @return The Color object
     */
    public static Color getColorFromDef(ColorDefinition colorDef) {
        Color color = COLOR_MAP.get(colorDef);
        if (color == null) {
            color = Color.rgb(colorDef.fRed, colorDef.fGreen, colorDef.fBlue, (double) colorDef.fAlpha / (double) ColorDefinition.MAX);
            COLOR_MAP.put(colorDef, color);
        }
        return color;
    }

}
