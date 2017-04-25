/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.toolbar;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.lttng.scope.tmf2.views.core.config.ConfigOption;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.states.ITimeGraphModelStateProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.ColorDefinition;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.StateDefinition;
import org.lttng.scope.tmf2.views.ui.jfx.CountingGridPane;
import org.lttng.scope.tmf2.views.ui.jfx.JfxColorFactory;
import org.lttng.scope.tmf2.views.ui.jfx.JfxImageFactory;
import org.lttng.scope.tmf2.views.ui.jfx.JfxUtils;
import org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.StateRectangle;
import org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.TimeGraphWidget;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

/**
 * Button to open the legend mapping states to colors.
 *
 * @author Alexandre Montplaisir
 */
public class ModelConfigButton extends Button {

    private static final String LEGEND_ICON_PATH = "/icons/toolbar/legend.gif"; //$NON-NLS-1$

    private static final Insets PADDING = new Insets(20.0);
    private static final double SPACING = 10.0;

    /**
     * Constructor
     *
     * @param widget
     *            The time graph widget to which this toolbar button is
     *            associated.
     */
    public ModelConfigButton(TimeGraphWidget widget) {
        Image icon = JfxImageFactory.instance().getImageFromResource(LEGEND_ICON_PATH);
        setGraphic(new ImageView(icon));
        setTooltip(new Tooltip(Messages.legendButtonName));

        setOnAction(e -> {
            Dialog<?> dialog = new LegendDialog(widget);
            dialog.show();
            JfxUtils.centerDialogOnScreen(dialog, ModelConfigButton.this);
        });
    }

    private static class LegendDialog extends Dialog<@Nullable Void> {

        public LegendDialog(TimeGraphWidget widget) {
            setTitle("State Model Configuration");
            setHeaderText("State Rectangles Configuration");

            ButtonType resetToDefaultButtonType = new ButtonType("Reset Defaults", ButtonData.LEFT);
            getDialogPane().getButtonTypes().addAll(resetToDefaultButtonType, ButtonType.CLOSE);

            // TODO Allow configuring arrow, etc. providers too (different tabs?)
            ITimeGraphModelStateProvider stateProvider = widget.getControl().getModelRenderProvider().getStateProvider();
            List<ColorDefControl> stateColorSetters = stateProvider.getStateDefinitions().stream()
                    .map(stateDef -> new ColorDefControl(widget, stateDef))
                    .collect(Collectors.toList());

            CountingGridPane grid = new CountingGridPane();
            stateColorSetters.forEach(setter -> grid.appendRow(setter.getNodes()));
            getDialogPane().setContent(grid);

            /*
             * We do not set the dialog's 'resultConverter', there is nothing
             * special to do on close (all changes are immediately valid).
             */

            /* Define how to "Reset Defaults" button works */
            getDialogPane().lookupButton(resetToDefaultButtonType).addEventFilter(ActionEvent.ACTION, e -> {
                /*
                 * This button should not close the dialog. Consuming the event here
                 * will prevent the dialog from closing.
                 */
                e.consume();

                stateColorSetters.forEach(ps -> {
                    ConfigOption<?> option = ps.getOption();
                    option.resetToDefault();
                    ps.load();
                });

                widget.getRenderedStateRectangles().forEach(StateRectangle::updatePaint);
            });

        }
    }

    private static class ColorDefControl {

        private final StateDefinition fStateDef;

        private final Label fLabel;
        private final ColorPicker fColorPicker;

        public ColorDefControl(TimeGraphWidget widget, StateDefinition stateDef) {
            fStateDef = stateDef;

            fLabel = new Label(stateDef.getName() + ":"); //$NON-NLS-1$
            fColorPicker = new ColorPicker();
            fColorPicker.getStyleClass().add(ColorPicker.STYLE_CLASS_BUTTON);
            load();

            /*
             * Whenever a new color is selected in the UI, update the
             * corresponding model color.
             */
            fColorPicker.setOnAction(e -> {
                Color color = fColorPicker.getValue();
                if (color == null) {
                    return;
                }
                /*
                 * ColorDefintion works with integer values 0 to 255, but JavaFX
                 * colors works with doubles 0.0 to 0.1
                 */
                int red = (int) Math.round(color.getRed() * 255);
                int green = (int) Math.round(color.getGreen() * 255);
                int blue = (int) Math.round(color.getBlue() * 255);
                int opacity = (int) Math.round(color.getOpacity() * 255);
                fStateDef.getColor().set(new ColorDefinition(red, green, blue, opacity));

                widget.getRenderedStateRectangles().forEach(StateRectangle::updatePaint);
            });

        }

        public Node[] getNodes() {
            return new Node[] { fLabel, fColorPicker };
        }

        public ConfigOption<?> getOption() {
            return fStateDef.getColor();
        }

        public void load() {
            fColorPicker.setValue(JfxColorFactory.getColorFromDef(fStateDef.getColor().get()));
        }
    }

}
