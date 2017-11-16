/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline.widgets.timegraph.toolbar.drawnevents;

import com.efficios.jabberwocky.common.ConfigOption;
import com.efficios.jabberwocky.trace.event.TraceEvent;
import com.efficios.jabberwocky.views.common.ColorDefinition;
import com.efficios.jabberwocky.views.common.EventSymbolStyle;
import com.efficios.jabberwocky.views.timegraph.model.provider.ITimeGraphModelProvider;
import com.efficios.jabberwocky.views.timegraph.model.render.drawnevents.TimeGraphDrawnEventSeries;
import javafx.beans.property.ReadOnlyProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.util.Callback;
import org.lttng.scope.views.jfx.CountingGridPane;
import org.lttng.scope.views.jfx.JfxColorFactory;
import org.lttng.scope.views.timeline.widgets.timegraph.layer.TimeGraphDrawnEventLayer;

import java.util.function.Predicate;

class CreateEventSeriesDialog extends Dialog<PredicateDrawnEventProvider> {

    private static final Color DEFAULT_SYMBOL_COLOR = Color.ORANGE;
    private static final double PADDING = 10;

    private static final Font HEADER_FONT = Font.font(null, FontWeight.BOLD, FontPosture.REGULAR, -1);
    private static final Insets HEADER_PADDING = new Insets(10, 0, 10, 0);

    private final TextField fEventNameField;

    private final ColorPicker fSymbolColorPicker;
    private final ShapePicker fSymbolShapePicker;

    public CreateEventSeriesDialog(ITimeGraphModelProvider modelProvider) {
        setTitle(Messages.createEventSeriesDialogTitle);

        /* Dialog buttons, standard "OK" and "Cancel" */
        getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        /* Dialog contents */
        Label filterHeader = new Label(Messages.createEventSeriesDialogSectionFilterDef);
        filterHeader.setFont(HEADER_FONT);
        filterHeader.setPadding(HEADER_PADDING);

        fEventNameField = new TextField();
        CountingGridPane filterGrid = new CountingGridPane();
        filterGrid.setHgap(PADDING);
        filterGrid.setVgap(PADDING);
        filterGrid.appendRow(new Label(Messages.createEventSeriesDialogFieldEventName), fEventNameField);

        Label symbolHeader = new Label(Messages.createEventSeriesDialogSectionSymbolDef);
        symbolHeader.setFont(HEADER_FONT);
        symbolHeader.setPadding(HEADER_PADDING);

        fSymbolColorPicker = new ColorPicker(DEFAULT_SYMBOL_COLOR);
        fSymbolShapePicker = new ShapePicker(fSymbolColorPicker.valueProperty());
        CountingGridPane symbolGrid = new CountingGridPane();
        symbolGrid.setHgap(PADDING);
        symbolGrid.setVgap(PADDING);
        symbolGrid.appendRow(new Label(Messages.createEventSeriesDialogFieldColor), fSymbolColorPicker);
        symbolGrid.appendRow(new Label(Messages.createEventSeriesDialogFieldShape), fSymbolShapePicker);

        VBox vbox = new VBox(filterHeader, filterGrid, symbolHeader, symbolGrid);
        vbox.setAlignment(Pos.CENTER);
        getDialogPane().setContent(vbox);

        /*
         * Disable the OK button until the input is valid
         *
         * TODO ControlsFX Validation framework might be useful when more
         * fields/options are added.
         */
        final Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);
        fEventNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            okButton.setDisable(newValue.trim().length() <= 0);
        });

        /* What to do when the dialog is closed */
        setResultConverter(dialogButton -> {
            if (dialogButton != ButtonType.OK) {
                return null;
            }

            String eventName = fEventNameField.getText();
            if (eventName == null || eventName.isEmpty()) {
                return null;
            }

            TimeGraphDrawnEventSeries series = generateEventSeries();
            Predicate<TraceEvent> predicate = event -> event.getEventName().equals(eventName);
            return new PredicateDrawnEventProvider(series, modelProvider, predicate);
        });

    }

    /**
     * Generate an event series from the current value of the controls
     *
     * @return The corresponding event series
     */
    private TimeGraphDrawnEventSeries generateEventSeries() {
        String seriesName = fEventNameField.getText();
        ColorDefinition colorDef = JfxColorFactory.colorToColorDef(fSymbolColorPicker.getValue());
        EventSymbolStyle style = fSymbolShapePicker.getSelectionModel().getSelectedItem();

        return new TimeGraphDrawnEventSeries(
                seriesName == null ? "" : seriesName, //$NON-NLS-1$
                new ConfigOption<>(colorDef),
                new ConfigOption<>(style));
    }

    private static class ShapePicker extends ComboBox<EventSymbolStyle> {

        public ShapePicker(ReadOnlyProperty<Color> colorSource) {
            getItems().addAll(EventSymbolStyle.values());

            Callback<ListView<EventSymbolStyle>, ListCell<EventSymbolStyle>> cellFactory =
                    p -> new ListCell<EventSymbolStyle>() {
                        {
                            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                        }

                        @Override
                        protected void updateItem(EventSymbolStyle item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty) {
                                setGraphic(null);
                            } else {
                                Node graphic = getGraphicFromSymbol(item, colorSource);
                                setGraphic(graphic);
                            }
                        }
                    };

            setButtonCell(cellFactory.call(null));
            setCellFactory(cellFactory);

            /* Select the first symbol by default */
            getSelectionModel().select(0);
        }

        private static Node getGraphicFromSymbol(EventSymbolStyle symbol, ReadOnlyProperty<Color> colorSource) {
            Shape graphic = TimeGraphDrawnEventLayer.getShapeFromSymbol(symbol);
            graphic.fillProperty().bind(colorSource);
            return graphic;
        }

    }

}
