/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0
 * Unported License. To view a copy of this license, visit
 * https://creativecommons.org/licenses/by-sa/3.0/.
 */

package org.lttng.scope.ui.jfx;

import org.eclipse.jdt.annotation.Nullable;

import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Group;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;

/**
 * JavaFX node representing a line with an arrow head at the end point.
 *
 * Initially inspired from http://stackoverflow.com/a/41353991/4227853.
 */
public class Arrow extends Group {

    private static final double ARROW_HEAD_LENGTH = 5;
    private static final double ARROW_HEAD_WIDTH = 3;

    private final Line line;

    /**
     * Constructor specifying the initial coordinates of the arrow. The arrow
     * head is drawn at the end point.
     *
     * @param startX
     *            X of start point
     * @param startY
     *            Y of end point
     * @param endX
     *            X of end point
     * @param endY
     *            Y of end point.
     */
    public Arrow(double startX, double startY, double endX, double endY) {
        this();
        setStartX(startX);
        setStartY(startY);
        setEndX(endX);
        setEndY(endY);
    }

    /**
     * Constructor with default coordinates (usually (0,0)).
     */
    public Arrow() {
        this(new Line(), new Line(), new Line());
    }

    private Arrow(Line line, Line arrow1, Line arrow2) {
        super(line, arrow1, arrow2);
        this.line = line;

        /* The color of the arrow should follow the color of the main line. */
        arrow1.strokeProperty().bind(line.strokeProperty());
        arrow2.strokeProperty().bind(line.strokeProperty());

        /* Listener to redraw the arrow parts when the line position changes. */
        InvalidationListener updater = o -> {
            double ex = getEndX();
            double ey = getEndY();
            double sx = getStartX();
            double sy = getStartY();

            arrow1.setEndX(ex);
            arrow1.setEndY(ey);
            arrow2.setEndX(ex);
            arrow2.setEndY(ey);

            if (ex == sx && ey == sy) {
                /* The line is just a point. Don't draw the arrowhead. */
                arrow1.setStartX(ex);
                arrow1.setStartY(ey);
                arrow2.setStartX(ex);
                arrow2.setStartY(ey);
            } else {
                double factor = ARROW_HEAD_LENGTH / Math.hypot(sx-ex, sy-ey);
                double factorO = ARROW_HEAD_WIDTH / Math.hypot(sx-ex, sy-ey);

                // part in direction of main line
                double dx = (sx - ex) * factor;
                double dy = (sy - ey) * factor;

                // part ortogonal to main line
                double ox = (sx - ex) * factorO;
                double oy = (sy - ey) * factorO;

                arrow1.setStartX(ex + dx - oy);
                arrow1.setStartY(ey + dy + ox);
                arrow2.setStartX(ex + dx + oy);
                arrow2.setStartY(ey + dy - ox);
            }
        };

        /* Attach updater to properties */
        startXProperty().addListener(updater);
        startYProperty().addListener(updater);
        endXProperty().addListener(updater);
        endYProperty().addListener(updater);
        updater.invalidated(null);
    }

    /**
     * Set the value of the property startX.
     *
     * @param value
     *            The new value
     */
    public final void setStartX(double value) {
        line.setStartX(value);
    }

    /**
     * Get the value of the property startX.
     *
     * @return The current value
     */
    public final double getStartX() {
        return line.getStartX();
    }

    /**
     * The startX property
     *
     * @return The startX property
     */
    public final DoubleProperty startXProperty() {
        return line.startXProperty();
    }

    /**
     * Set the value of the property startY.
     *
     * @param value
     *            The new value
     */
    public final void setStartY(double value) {
        line.setStartY(value);
    }

    /**
     * Get the value of the property startY.
     *
     * @return The current value
     */
    public final double getStartY() {
        return line.getStartY();
    }

    /**
     * The startY property
     *
     * @return The startY property
     */
    public final DoubleProperty startYProperty() {
        return line.startYProperty();
    }

    /**
     * Set the value of the property endX.
     *
     * @param value
     *            The new value
     */
    public final void setEndX(double value) {
        line.setEndX(value);
    }

    /**
     * Get the value of the property endX.
     *
     * @return The current value
     */
    public final double getEndX() {
        return line.getEndX();
    }

    /**
     * The endX property
     *
     * @return The endX property
     */
    public final DoubleProperty endXProperty() {
        return line.endXProperty();
    }

    /**
     * Set the value of the property endY.
     *
     * @param value
     *            The new value
     */
    public final void setEndY(double value) {
        line.setEndY(value);
    }

    /**
     * Get the value of the property endY.
     *
     * @return The current value
     */
    public final double getEndY() {
        return line.getEndY();
    }

    /**
     * The endY property
     *
     * @return The endY property
     */
    public final DoubleProperty endYProperty() {
        return line.endYProperty();
    }

    /**
     * Set the value of the stroke property.
     *
     * @param value
     *            The new value
     */
    public final void setStroke(@Nullable Paint value) {
        line.setStroke(value);
    }

    /**
     * Get the value of the stroke property.
     *
     * @return The current value
     */
    public final @Nullable Paint getStroke() {
        return line.getStroke();
    }

    /**
     * The stroke property, which determines the lines' color.
     *
     * @return The stroke property
     */
    public final ObjectProperty<@Nullable Paint> strokeProperty() {
        return line.strokeProperty();
    }

}