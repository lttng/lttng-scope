/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.lttng.scope.tmf2.views.core.timegraph.model.render;

/**
 * Definition of UI colors, from https://flatuicolors.com/ .
 */
@SuppressWarnings("javadoc")
public interface FlatUIColors {

    ColorDefinition TURQUOISE       = new ColorDefinition( 26, 188, 156);
    ColorDefinition DARK_TURQUOISE  = new ColorDefinition( 22, 160, 133);
    ColorDefinition GREEN           = new ColorDefinition( 46, 204, 113);
    ColorDefinition DARK_GREEN      = new ColorDefinition( 39, 174,  96);
    ColorDefinition BLUE            = new ColorDefinition( 52, 152, 219);
    ColorDefinition DARK_BLUE       = new ColorDefinition( 41, 128, 185);
    ColorDefinition PURPLE          = new ColorDefinition(155,  89, 182);
    ColorDefinition DARK_PURPLE     = new ColorDefinition(142,  68, 173);
    ColorDefinition BLUE_GRAY       = new ColorDefinition( 52,  73,  94);
    ColorDefinition DARK_BLUE_GRAY  = new ColorDefinition( 44,  62,  80);
    ColorDefinition YELLOW          = new ColorDefinition(241, 196,  15);
    ColorDefinition DARK_YELLOW     = new ColorDefinition(243, 156,  18);
    ColorDefinition ORANGE          = new ColorDefinition(230, 126,  34);
    ColorDefinition DARK_ORANGE     = new ColorDefinition(211,  84,   0);
    ColorDefinition RED             = new ColorDefinition(231,  76,  60);
    ColorDefinition DARK_RED        = new ColorDefinition(192,  57,  43);
    ColorDefinition VERY_LIGHT_GRAY = new ColorDefinition(236, 240, 241);
    ColorDefinition LIGHT_GRAY      = new ColorDefinition(189, 195, 199);
    ColorDefinition GRAY            = new ColorDefinition(149, 165, 166);
    ColorDefinition DARK_GRAY       = new ColorDefinition(127, 140, 141);

}
