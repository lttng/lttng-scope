/*******************************************************************************
 * Copyright (c) 2016-2018 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.common.jfx;

import com.efficios.jabberwocky.views.common.ColorDefinition
import javafx.scene.paint.Color
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt

object JfxColorFactory {


    private val COLOR_MAP = ConcurrentHashMap<ColorDefinition, Color>()
    private val DERIVED_COLOR_MAP = ConcurrentHashMap<ColorDefinition, Color>()

    /**
     * Instantiate a {@link Color} from a {@link ColorDefinition} object.
     *
     * @param colorDef
     *            The ColorDefinition
     * @return The Color object
     */
    @JvmStatic
    @Synchronized
    fun getColorFromDef(colorDef: ColorDefinition): Color {
        COLOR_MAP[colorDef]?.let { return it }

        with(Color.rgb(colorDef.red, colorDef.green, colorDef.blue, colorDef.alpha.toDouble() / ColorDefinition.MAX.toDouble())) {
            COLOR_MAP[colorDef] = this
            return this
        }

    }

    @JvmStatic
    @Synchronized
    fun getDerivedColorFromDef(colorDef: ColorDefinition): Color {
        DERIVED_COLOR_MAP[colorDef]?.let { return it }

        with(getColorFromDef(colorDef).desaturate().darker()) {
            DERIVED_COLOR_MAP[colorDef] = this
            return this
        }
    }

    /**
     * Convert a JavaFX {@link Color} to its equivalent {@link ColorDefinition}.
     *
     * @param color
     *            The color to convert
     * @return A corresponding ColorDefinition
     */
    @JvmStatic
    fun colorToColorDef(color: Color): ColorDefinition {
        /*
         * ColorDefintion works with integer values 0 to 255, but JavaFX colors
         * works with doubles 0.0 to 0.1
         */
        val transform = { value: Double -> (value * 255).roundToInt() }

        val red = transform(color.red)
        val green = transform(color.green)
        val blue = transform(color.blue)
        val opacity = transform(color.opacity)
        return ColorDefinition(red, green, blue, opacity)
    }
}
