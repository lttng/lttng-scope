/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0
 * Unported License. To view a copy of this license, visit
 * https://creativecommons.org/licenses/by-sa/3.0/.
 */

package org.lttng.scope.common.jfx

import javafx.beans.InvalidationListener
import javafx.beans.property.DoubleProperty
import javafx.beans.property.ObjectProperty
import javafx.scene.Group
import javafx.scene.paint.Paint
import javafx.scene.shape.Line
import kotlin.math.hypot

/**
 * JavaFX node representing a line with an arrow head at the end point.
 *
 * Initially inspired from http://stackoverflow.com/a/41353991/4227853.
 */
class Arrow private constructor(private val line: Line, arrow1: Line, arrow2: Line): Group(line, arrow1, arrow2) {

    companion object {
        private const val ARROW_HEAD_LENGTH = 5.0
        private const val ARROW_HEAD_WIDTH = 3.0
    }

    /**
     * Constructor specifying the initial coordinates of the arrow. The arrow
     * head is drawn at the end point.
     */
    constructor(startX: Double, startY: Double, endX: Double, endY: Double) : this() {
        this.startX = startX
        this.startY = startY
        this.endX = endX
        this.endY = endY
    }

    /**
     * Constructor with default coordinates (usually (0,0)).
     */
    constructor() : this(Line(), Line(), Line())

    init {
        /* The color of the arrow should follow the color of the main line. */
        arrow1.strokeProperty().bind(line.strokeProperty())
        arrow2.strokeProperty().bind(line.strokeProperty())

        /* Listener to redraw the arrow parts when the line position changes. */
        val updater = InvalidationListener { _ ->
            val ex = endX
            val ey = endY
            val sx = startX
            val sy = startY

            arrow1.endX = ex
            arrow1.endY = ey
            arrow2.endX = ex
            arrow2.endY = ey

            if (ex == sx && ey == sy) {
                /* The line is just a point. Don't draw the arrowhead. */
                arrow1.startX = ex
                arrow1.startY = ey
                arrow2.startX = ex
                arrow2.startY = ey
            } else {
                val factor = ARROW_HEAD_LENGTH / hypot(sx - ex, sy - ey)
                val factorO = ARROW_HEAD_WIDTH / hypot(sx - ex, sy - ey)

                // part in direction of main line
                val dx = (sx - ex) * factor
                val dy = (sy - ey) * factor

                // part ortogonal to main line
                val ox = (sx - ex) * factorO
                val oy = (sy - ey) * factorO

                arrow1.startX = ex + dx - oy
                arrow1.startY = ey + dy + ox
                arrow2.startX = ex + dx + oy
                arrow2.startY = ey + dy - ox
            }
        }

        /* Attach updater to properties */
        startXProperty().addListener(updater)
        startYProperty().addListener(updater)
        endXProperty().addListener(updater)
        endYProperty().addListener(updater)
        updater.invalidated(null)
    }


    fun startXProperty(): DoubleProperty = line.startXProperty()
    var startX: Double
        get() = line.startX
        set(value) {
            line.startX = value
        }

    fun startYProperty(): DoubleProperty = line.startYProperty()
    var startY: Double
        get() = line.startY
        set(value) {
            line.startY = value
        }

    fun endXProperty(): DoubleProperty = line.endXProperty()
    var endX: Double
        get() = line.endX
        set(value) {
            line.endX = value
        }

    fun endYProperty(): DoubleProperty = line.endYProperty()
    var endY: Double
        get() = line.endY
        set(value) {
            line.endY = value
        }

    fun strokeProperty(): ObjectProperty<Paint?> = line.strokeProperty()
    var stroke: Paint?
        get() = line.stroke
        set(value) {
            line.stroke = value
        }

}
