/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.jfx;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.List;

import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Dialog;
import javafx.scene.control.OverrunStyle;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Window;

/**
 * JavaFX-related utilities
 *
 * @author Alexandre Montplaisir
 */
public final class JfxUtils {

    private static final MethodHandles.Lookup LOOKUP = requireNonNull(MethodHandles.lookup());
    private static final MethodHandle COMPUTE_CLIPPED_TEXT_HANDLE;
    static {
        MethodHandle handle = null;
        try {
            Class<?> c = Class.forName("com.sun.javafx.scene.control.skin.Utils"); //$NON-NLS-1$
            Method method = c.getDeclaredMethod("computeClippedText", //$NON-NLS-1$
                    Font.class, String.class, double.class, OverrunStyle.class, String.class);
            method.setAccessible(true);
            handle = LOOKUP.unreflect(method);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException e) {
            /* Should not fail if we did everything correctly. */
        }
        COMPUTE_CLIPPED_TEXT_HANDLE = requireNonNull(handle);
    }

    /**
     * Accessor for the
     * com.sun.javafx.scene.control.skin.Utils.computeClippedText() method.
     *
     * This method implements the logic to clip Label strings. It can be useful
     * for other types, like Text. Unfortunately it is not public, but this
     * accessor allows calling it through reflection. It makes use of
     * {@link MethodHandle#invokeExact}, which should be close to just as fast
     * as a standard compiled method call.
     *
     * @param font
     *            The font of the text that will be used
     * @param text
     *            The string to clip
     * @param width
     *            The maximum width we want to limit the string to
     * @param type
     *            The {@link OverrunStyle}
     * @param ellipsisString
     *            The string to use as ellipsis
     * @return The clipped string, or "ERROR" if an error happened.
     *         Unfortunately we lose the exception typing due to the reflection
     *         call, so we do not want to throw "Throwable" here.
     */
    public static String computeClippedText(Font font, String text, double width,
            OverrunStyle type, String ellipsisString) {
        try {
            String str = (String) COMPUTE_CLIPPED_TEXT_HANDLE.invokeExact(font, text, width, type, ellipsisString);
            return requireNonNull(str);
        } catch (Throwable e) {
            return "ERROR"; //$NON-NLS-1$
        }
    }

    /**
     * Utility method to center a Dialog/Alert on the middle of the current
     * screen. Used to workaround a "bug" with the current version of JavaFX (or
     * the SWT/JavaFX embedding?) where alerts always show on the primary
     * screen, not necessarily the current one.
     *
     * @param dialog
     *            The dialog to reposition. It must be already shown, or else
     *            this will do nothing.
     * @param referenceNode
     *            The dialog should be moved to the same screen as this node's
     *            window.
     */
    public static void centerDialogOnScreen(Dialog<?> dialog, Node referenceNode) {
        Window window = referenceNode.getScene().getWindow();
        Rectangle2D windowRectangle = new Rectangle2D(window.getX(), window.getY(), window.getWidth(), window.getHeight());

        List<Screen> screens = Screen.getScreensForRectangle(windowRectangle);
        Screen screen = screens.stream()
                .findFirst()
                .orElse(Screen.getPrimary());

        Rectangle2D screenBounds = screen.getBounds();
        dialog.setX((screenBounds.getWidth() - dialog.getWidth()) / 2 + screenBounds.getMinX());
//        dialog.setY((screenBounds.getHeight() - dialog.getHeight()) / 2 + screenBounds.getMinY());
    }
}
