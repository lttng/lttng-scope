/*
 * Copyright (C) 2017-2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.common.jfx

import javafx.scene.image.Image
import java.io.IOException

/**
 * Factory for JavaFX [Image]s. This will allow caching the Image objects,
 * allowing any class to re-use already read images.
 *
 * @author Alexandre Montplaisir
 */
object JfxImageFactory {

    private val images = mutableMapOf<String, Image>()

    /**
     * Get the {@link Image} for a given path within the jar's resources.
     *
     * @param resourcePath
     *            The path to the image resource. It should be a standard
     *            .gif/.png/.jpg etc. file.
     * @return The corresponding Image.
     */
    @JvmStatic
    @Synchronized
    fun getImageFromResource(resourcePath: String): Image? {
        images[resourcePath]?.let { return it }

        val image: Image = try {
            javaClass.getResourceAsStream(resourcePath).use { it?.let { Image(it) } ?: return null }
        } catch (e: IOException) {
            return null
        }

        images[resourcePath] = image
        return image
    }

}
