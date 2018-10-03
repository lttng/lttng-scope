/*
 * Copyright (C) 2017-2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.common.jfx

import javafx.animation.Animation
import javafx.animation.Interpolator
import javafx.animation.RotateTransition
import javafx.application.Application
import javafx.application.Application.launch
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle
import javafx.stage.Stage
import javafx.util.Duration

fun main(args: Array<String>) {
    launch(Logo::class.java, *args)
}

class Logo : Application() {

    companion object {
        private const val BACKGROUND_STYLE = "-fx-background-color: rgba(255, 255, 255, 255);"
        private val LTTNG_PURPLE: Color = Color.web("#996ABC")
        private val LTTNG_LIGHT_BLUE: Color = Color.web("#C3DEF4")
    }

    override fun start(stage: Stage?) {
        stage ?: return

        val clipRect1 = Rectangle(-130.0, -130.0, 115.0, 115.0)
        val clipRect2 = Rectangle(15.0, -130.0, 115.0, 115.0)
        val clipRect3 = Rectangle(-130.0, 15.0, 115.0, 115.0)
        val clipRect4 = Rectangle(15.0, 15.0, 115.0, 115.0)
        val clip = Group(clipRect1, clipRect2, clipRect3, clipRect4)

        val spinnanCircle = Circle(100.0)
        spinnanCircle.setFill(null);
        spinnanCircle.setStrokeWidth(30.0);
        spinnanCircle.setStroke(LTTNG_PURPLE);
        spinnanCircle.setClip(clip);

        val magCircle = Circle(60.0);
        magCircle.setFill(null);
        magCircle.setStrokeWidth(25.0);
        magCircle.setStroke(LTTNG_LIGHT_BLUE);

        val magHandle = Rectangle(-12.5, 60.0, 25.0, 110.0);
        magHandle.setFill(LTTNG_LIGHT_BLUE);

        val mag = Group(magCircle, magHandle);

        val root = Group(spinnanCircle, mag);
        root.setRotate(30.0)
        root.relocate(0.0, 0.0)

        val pane = Pane(root);
        pane.setStyle(BACKGROUND_STYLE);

        val spinnan = RotateTransition(Duration.seconds(4.0), spinnanCircle);
        spinnan.setByAngle(360.0);
        spinnan.setCycleCount(Animation.INDEFINITE);
        spinnan.setInterpolator(Interpolator.LINEAR);

        val scene = Scene(pane);
        stage.setScene(scene);
        stage.show();

        spinnan.play();
    }

}
