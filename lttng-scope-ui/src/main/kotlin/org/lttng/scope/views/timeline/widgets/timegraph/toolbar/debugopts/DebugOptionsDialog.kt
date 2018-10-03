/*
 * Copyright (C) 2017-2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline.widgets.timegraph.toolbar.debugopts

import com.efficios.jabberwocky.common.ConfigOption
import javafx.event.ActionEvent
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color

/**
 * Dialog to configure the debug options at runtime.
 */
class DebugOptionsDialog(button: DebugOptionsButton) : Dialog<Unit>() {

    companion object {
        private val PADDING = Insets(20.0)
        private const val SPACING = 10.0
    }

    private val opts = button.debugOptions
    private val tabPane = TabPane(createGeneralTab(),
            createLoadingOverlayTab(),
            createZoomTab(),
            createStateIntervalsTab(),
            createTooltipTab())

    init {
        title = Messages.debugOptionsDialogTitle
        headerText = Messages.debugOptionsDialogName

        val resetToDefaultButtonType = ButtonType(Messages.resetDefaultsButtonLabel, ButtonData.LEFT)
        dialogPane.buttonTypes.addAll(resetToDefaultButtonType, ButtonType.CANCEL, ButtonType.OK)

        dialogPane.content = tabPane

        /*
         * Restore the last-selected tab (that state is saved in the button),
         * and re-bind on the new dialog so that the property continues getting
         * updated.
         */
        tabPane.selectionModel.select(button.lastSelectedTabProperty.get())
        button.lastSelectedTabProperty.bind(tabPane.selectionModel.selectedIndexProperty())

        /* What to do when the dialog is closed */
        setResultConverter { dialogButton ->
            if (dialogButton != ButtonType.OK) return@setResultConverter
            /*
             * Set the debug options according to the current contents of the
             * dialog.
             */
            allPropertySetters.forEach(PropertySetter::save)
        }

        /* Define how to "Reset Defaults" button works */
        dialogPane.lookupButton(resetToDefaultButtonType).addEventFilter(ActionEvent.ACTION) { e ->
            /*
             * This button should not close the dialog. Consuming the event here
             * will prevent the dialog from closing.
             */
            e.consume()

            allPropertySetters.forEach {
                it.option.resetToDefault()
                it.load()
            }

        }

    }

    private val allPropertySetters: List<PropertySetter>
        get() = tabPane.tabs
                .flatMap { tab -> (tab.content as VBox).children }
                .map { it as PropertySetter }

    // ------------------------------------------------------------------------
    // Tab classes
    // ------------------------------------------------------------------------

    private class DebugOptionsDialogTab(name: String?, vararg contents: Node) : Tab() {

        init {
            isClosable = false
            text = name

            VBox(*contents)
                    .apply {
                        padding = PADDING
                        spacing = SPACING
                    }
                    .let { content = it }
        }

    }

    private fun createGeneralTab(): Tab =
            DebugOptionsDialogTab(Messages.tabNameGeneral,
                    CheckBoxControl(Messages.controlPaintingEnabled, opts.isPaintingEnabled),
                    IntegerTextField(Messages.controlEntryPadding, opts.entryPadding),
                    DoubleTextField(Messages.controlRenderRangePadding, opts.renderRangePadding),
                    IntegerTextField(Messages.controlUIUpdateDelay, opts.uiUpdateDelay),
                    CheckBoxControl(Messages.controlHScrollEnabled, opts.isScrollingListenersEnabled))

    private fun createLoadingOverlayTab(): Tab =
            DebugOptionsDialogTab(Messages.tabNameLoadingOverlay,
                    CheckBoxControl(Messages.controlLoadingOverlayEnabled, opts.isLoadingOverlayEnabled),
                    ColorControl(Messages.controlLoadingOverlayColor, opts.loadingOverlayColor),
                    DoubleTextField(Messages.controlLoadingOverlayFullOpacity, opts.loadingOverlayFullOpacity),
//                  DoubleTextField(Messages.controlLoadingOverlayTransparentOpacity, opts.loadingOverlayTransparentOpacity),
                    DoubleTextField(Messages.controlLoadingOverlayFadeIn, opts.loadingOverlayFadeInDuration),
                    DoubleTextField(Messages.controlLoadingOverlayFadeOut, opts.loadingOverlayFadeOutDuration))

    private fun createZoomTab(): Tab =
            DebugOptionsDialogTab(Messages.tabNameZoom,
                    IntegerTextField(Messages.controlZoomAnimationDuration + " (unused)", opts.zoomAnimationDuration),
                    DoubleTextField(Messages.controlZoomStep, opts.zoomStep),
                    CheckBoxControl(Messages.controlZoomPivotOnSelection, opts.zoomPivotOnSelection),
                    CheckBoxControl(Messages.controlZoomPivotOnMousePosition, opts.zoomPivotOnMousePosition))

    private fun createStateIntervalsTab(): Tab =
            DebugOptionsDialogTab(Messages.tabNameIntervals,
                    DoubleTextField(Messages.controlIntervalOpacity, opts.stateIntervalOpacity)
                    // multi-state Paint ?
                    // state label Font ?
            )

    private fun createTooltipTab(): Tab =
            DebugOptionsDialogTab(Messages.tabNameTooltips,
                    // Tooltip Font picker
                    ColorControl(Messages.controlTooltipFontColor, opts.toolTipFontFill))

    // ------------------------------------------------------------------------
    // Property-setting controls
    // ------------------------------------------------------------------------

    private interface PropertySetter {
        val option: ConfigOption<*>
        fun load()
        fun save()
    }

    private class CheckBoxControl(labelText: String?, override val option: ConfigOption<Boolean>) : CheckBox(), PropertySetter {

        init {
            text = labelText
            load()
        }

        override fun load() {
            isSelected = option.get()
        }

        override fun save() {
            option.set(isSelected)
        }
    }

    private class ColorControl(labelText: String?, override val option: ConfigOption<Color>) : HBox(), PropertySetter {

        private val colorPicker = ColorPicker(option.get())

        init {
            children.addAll(Label("$labelText:"), colorPicker)
            alignment = Pos.CENTER_LEFT
            spacing = SPACING
        }

        override fun load() {
            colorPicker.value = option.get()
        }

        override fun save() {
            colorPicker.value?.let { option.set(it) }
        }
    }

    private abstract class TextFieldControl<T : Number> protected constructor(labelText: String?,
                                                                              final override val option: ConfigOption<T>) : HBox(), PropertySetter {
        protected val textField = TextField()

        init {
            val label = Label("$labelText:")
            load()

            children.addAll(label, textField)
            alignment = Pos.CENTER_LEFT
            spacing = SPACING
        }

        final override fun load() {
            textField.text = option.get().toString()
        }

        abstract override fun save()
    }

    private class IntegerTextField(labelText: String?, option: ConfigOption<Int>) : TextFieldControl<Int>(labelText, option) {

        override fun save() {
            textField.text.toIntOrNull()
                    ?.let { option.set(it) }
                    ?: run {
                        option.resetToDefault()
                        load()
                    }
        }
    }

    private class DoubleTextField(labelText: String?, option: ConfigOption<Double>) : TextFieldControl<Double>(labelText, option) {

        override fun save() {
            textField.text.toDoubleOrNull()
                    ?.let { option.set(it) }
                    ?: run {
                        option.resetToDefault()
                        load()
                    }
        }
    }

}
