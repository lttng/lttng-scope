/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.ui.jfx;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import javafx.scene.image.Image;

/**
 * Factory for JavaFX {@link Image}s. This will allow caching the Image objects,
 * allowing any class to re-use already read images.
 *
 * @author Alexandre Montplaisir
 * @noreference This cache is only valid for classes within the same jar/plugin.
 *              The resource paths would not work from different plugins.
 */
public final class JfxImageFactory {

    private static final JfxImageFactory INSTANCE = new JfxImageFactory();

    private JfxImageFactory() {}

    /**
     * Get the singleton instance of this factory.
     *
     * @return The instance
     */
    public static JfxImageFactory instance() {
        return INSTANCE;
    }

    private final Map<String, Image> fImages = new HashMap<>();

    /**
     * Get the {@link Image} for a given path within the jar's resources.
     *
     * @param resourcePath
     *            The path to the image resource. It should be a standard
     *            .gif/.png/.jpg etc. file.
     * @return The corresponding Image.
     */
    public synchronized @Nullable Image getImageFromResource(String resourcePath) {
        Image image = fImages.get(resourcePath);
        if (image == null) {
            try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
                if (is == null) {
                    /* The image was not found, the path is invalid */
                    return null;
                }
                image = new Image(is);
            } catch (IOException e) {
                return null;
            }
            fImages.put(resourcePath, image);
        }
        return image;
    }

}
