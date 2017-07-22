/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.ui.timeline.widgets.timegraph.toolbar.modelconfig;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.lttng.scope.ui.jfx.CountingGridPane;
import org.lttng.scope.ui.jfx.JfxColorFactory;
import org.lttng.scope.ui.timeline.widgets.timegraph.StateRectangle;
import org.lttng.scope.ui.timeline.widgets.timegraph.TimeGraphWidget;

import com.efficios.jabberwocky.config.ConfigOption;
import com.efficios.jabberwocky.timegraph.model.provider.states.ITimeGraphModelStateProvider;
import com.efficios.jabberwocky.timegraph.model.render.LineThickness;
import com.efficios.jabberwocky.timegraph.model.render.StateDefinition;
import com.efficios.jabberwocky.timegraph.model.render.states.MultiStateInterval;
import com.efficios.jabberwocky.views.common.ColorDefinition;

import javafx.beans.property.ReadOnlyProperty;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

class ModelConfigDialog extends Dialog<@Nullable Void> {

    private static final double H_GAP = 10;

    public ModelConfigDialog(TimeGraphWidget widget) {
        setTitle(Messages.modelConfigDialogTitle);
        setHeaderText(Messages.modelConfigDialogHeader);

        ButtonType resetToDefaultButtonType = new ButtonType(Messages.modelConfigDialogResetDefaultsButton, ButtonData.LEFT);
        getDialogPane().getButtonTypes().addAll(resetToDefaultButtonType, ButtonType.CLOSE);

        // TODO Allow configuring arrow, etc. providers too (different tabs?)
        ITimeGraphModelStateProvider stateProvider = widget.getControl().getModelRenderProvider().getStateProvider();
        List<ColorDefControl> stateControls = stateProvider.getStateDefinitions().stream()
                .map(stateDef -> new ColorDefControl(widget, stateDef))
                .collect(Collectors.toList());

        /* Setup the GridPane which will be the contents of the dialog */
        CountingGridPane grid = new CountingGridPane();
        grid.setHgap(H_GAP);
        /* Header row */
        grid.appendRow(new Text(Messages.modelConfigDialogRowHeaderState),
                new Text(Messages.modelConfigDialogRowHeaderColor),
                new Text(Messages.modelConfigDialogRowHeaderLineThickness));

        stateControls.forEach(setter -> grid.appendRow(setter.getNodes()));

        /* Add an "empty row", then the control for multi-state intervals */
        LineThicknessMenuButton multiStateThicknessButton = new LineThicknessMenuButton(widget,
                MultiStateInterval.MULTI_STATE_DEFINITION.getLineThickness(),
                widget.getDebugOptions().multiStatePaint);
        grid.appendRow(new Text("")); //$NON-NLS-1$
        grid.appendRow(new Label("Multi-states"), new Text(), multiStateThicknessButton);

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

            stateControls.forEach(sc -> {
                sc.getOptions().forEach(ConfigOption::resetToDefault);
                sc.load();
            });
            multiStateThicknessButton.getOption().resetToDefault();
            multiStateThicknessButton.load();

            repaintAllRectangles(widget);
        });

    }

    // TODO Repaint the rectangles of the whole Timeline view, not just 1 widget
    private static void repaintAllRectangles(TimeGraphWidget widget) {
        widget.getRenderedStateRectangles().forEach(StateRectangle::updatePaint);
    }

    private static class ColorDefControl {

        private final TimeGraphWidget fWidget;
        private final StateDefinition fStateDef;

        private final Label fLabel;
        private final ColorPicker fColorPicker;
        private final LineThicknessMenuButton fLineThicknessButton;

        /**
         * Constructor
         *
         * TODO Use a "PaintPicker" dialog instead, so a full Paint object can
         * be configured from the UI. There is apparently one in SceneBuilder.
         */
        public ColorDefControl(TimeGraphWidget widget, StateDefinition stateDef) {
            fWidget = widget;
            fStateDef = stateDef;

            fLabel = new Label(stateDef.getName());
            fColorPicker = new ColorPicker();
            fColorPicker.getStyleClass().add(ColorPicker.STYLE_CLASS_BUTTON);
            fLineThicknessButton = new LineThicknessMenuButton(widget, stateDef.getLineThickness(), fColorPicker.valueProperty());
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
                ColorDefinition colorDef = JfxColorFactory.colorToColorDef(color);
                fStateDef.getColor().set(colorDef);

                repaintAllRectangles(widget);
            });

        }

        public Node[] getNodes() {
            return new Node[] { fLabel, fColorPicker, fLineThicknessButton };
        }

        public Iterable<ConfigOption<?>> getOptions() {
            return Arrays.asList(fStateDef.getColor(), fStateDef.getLineThickness());
        }

        public void load() {
            Color color = JfxColorFactory.getColorFromDef(fStateDef.getColor().get());
            fColorPicker.setValue(color);

            fLineThicknessButton.load();
        }
    }

    private static class LineThicknessMenuButton extends MenuButton {

        private static final double RECTANGLE_WIDTH = 30;

        private final ConfigOption<LineThickness> fOption;
        private final ReadOnlyProperty<? extends Paint> fColorSource;

        public LineThicknessMenuButton(TimeGraphWidget widget,
                ConfigOption<LineThickness> option,
                ReadOnlyProperty<? extends Paint> colorSource) {
            fOption = option;
            fColorSource = colorSource;

            ToggleGroup tg = new ToggleGroup();
            List<LineThicknessMenuButtonItem> items = Arrays.stream(LineThickness.values())
                    .map(lt -> {
                        LineThicknessMenuButtonItem rmi = new LineThicknessMenuButtonItem(lt);
                        rmi.setGraphic(getRectangleForThickness(lt));
                        rmi.setToggleGroup(tg);

                        LineThickness currentThickness = option.get();
                        rmi.setSelected(lt == currentThickness);

                        rmi.setOnAction(e -> {
                            option.set(lt);
                            LineThicknessMenuButton.this.setGraphic(getRectangleForThickness(lt));
                            repaintAllRectangles(widget);
                        });
                        return rmi;
                    })
                    .collect(Collectors.toList());

            /* Initial value shown in the button */
            setGraphic(getRectangleForThickness(option.get()));
            getItems().addAll(items);
        }

        private Rectangle getRectangleForThickness(LineThickness lt) {
            Rectangle rectangle = new Rectangle(RECTANGLE_WIDTH, StateRectangle.getHeightFromThickness(lt));
            rectangle.fillProperty().bind(fColorSource);
            return rectangle;
        }

        public ConfigOption<LineThickness> getOption() {
            return fOption;
        }

        public void load() {
            LineThickness lt = fOption.get();
            setGraphic(getRectangleForThickness(lt));
            getItems().stream()
                    .map(item -> (LineThicknessMenuButtonItem) item)
                    .filter(item -> item.getLineThickness() == lt)
                    .findFirst().get().setSelected(true);
        }
    }

    private static class LineThicknessMenuButtonItem extends RadioMenuItem {

        private final LineThickness fLineThickness;

        public LineThicknessMenuButtonItem(LineThickness lt) {
            fLineThickness = lt;
        }

        public LineThickness getLineThickness() {
            return fLineThickness;
        }
    }

}