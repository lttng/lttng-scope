/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.project.filter;

import com.efficios.jabberwocky.views.common.EventSymbolStyle
import javafx.beans.property.ReadOnlyProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.*
import javafx.scene.text.Font
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight
import javafx.util.Callback
import org.lttng.scope.views.jfx.CountingGridPane
import org.lttng.scope.views.jfx.JfxColorFactory

private const val createEventSeriesDialogTitle = "Create New Filter"
private const val createEventSeriesDialogSectionFilterDef = "Filter Definition"
private const val createEventSeriesDialogFieldEventName = "Event Name"
private const val createEventSeriesDialogSectionSymbolDef = "Symbol"
private const val createEventSeriesDialogFieldColor = "Color"
private const val createEventSeriesDialogFieldShape = "Shape"

/**
 * Dialog to create project filter objects.
 *
 * For now it only asks and checks against the event name. Other criteria should be added.
 */
class CreateEventFilterDialog : Dialog<EventFilterDefinition>() {

    companion object {
        private val DEFAULT_SYMBOL_COLOR = Color.ORANGE
        private const val PADDING = 10.0
        private val HEADER_FONT = Font.font(null, FontWeight.BOLD, FontPosture.REGULAR, -1.0)
        private val HEADER_PADDING = Insets(10.0, 0.0, 10.0, 0.0)
    }

    private val eventNameField = TextField()
    private val symbolColorPicker = ColorPicker(DEFAULT_SYMBOL_COLOR)
    private val symbolShapePicker = ShapePicker(symbolColorPicker.valueProperty())

    init {
        title = createEventSeriesDialogTitle

        /* Dialog buttons, standard "OK" and "Cancel" */
        dialogPane.buttonTypes.addAll(ButtonType.CANCEL, ButtonType.OK)

        /* Dialog contents */
        val filterHeader = Label(createEventSeriesDialogSectionFilterDef).apply {
            font = HEADER_FONT;
            padding = HEADER_PADDING;
        }

        val filterGrid = CountingGridPane().apply {
            hgap = PADDING
            vgap = PADDING
            appendRow(Label(createEventSeriesDialogFieldEventName), eventNameField)
        }

        val symbolHeader = Label(createEventSeriesDialogSectionSymbolDef).apply {
            font = HEADER_FONT
            padding = HEADER_PADDING
        }

        val symbolGrid = CountingGridPane().apply {
            hgap = PADDING
            vgap = PADDING
            appendRow(Label(createEventSeriesDialogFieldColor), symbolColorPicker)
            appendRow(Label(createEventSeriesDialogFieldShape), symbolShapePicker)
        }

        dialogPane.content = VBox(filterHeader, filterGrid, symbolHeader, symbolGrid).apply {
            alignment = Pos.CENTER
        }

        /*
         * Disable the OK button until the input is valid
         *
         * TODO ControlsFX Validation framework might be useful when more
         * fields/options are added.
         */
        val okButton = (dialogPane.lookupButton(ButtonType.OK) as Button).apply {
            isDisable = true
        }
        eventNameField.textProperty().addListener { _, _, newValue -> okButton.isDisable = newValue.trim().isEmpty() }

        /* What to do when the dialog is closed */
        setResultConverter { dialogButton ->
            val eventName = eventNameField.text
            if (dialogButton != ButtonType.OK || eventName == null || eventName.isEmpty()) {
                null
            } else {
                generateFilterDefinition()
            }
        }
    }

    /**
     * Generate a filter definition from the current value of the controls
     *
     * @return The corresponding event series
     */
    private fun generateFilterDefinition(): EventFilterDefinition {
        val eventName = eventNameField.text ?: ""

        return EventFilterDefinition(eventName,
                JfxColorFactory.colorToColorDef(symbolColorPicker.value),
                symbolShapePicker.selectionModel.selectedItem,
                { event -> event.eventName == eventName })
    }

    private class ShapePicker(colorSource: ReadOnlyProperty<Color>) : ComboBox<EventSymbolStyle>() {

        init {
            items.addAll(EventSymbolStyle.values())
            cellFactory = Callback {
                object : ListCell<EventSymbolStyle>() {
                    init {
                        contentDisplay = ContentDisplay.GRAPHIC_ONLY
                    }

                    override fun updateItem(item: EventSymbolStyle?, empty: Boolean) {
                        super.updateItem(item, empty)
                        graphic = if (empty || item == null) {
                            null
                        } else {
                            item.getGraphic(colorSource)
                        }
                    }
                }
            }

            buttonCell = cellFactory.call(null)

            /* Select the first symbol by default */
            selectionModel.select(0)
        }

    }

}

