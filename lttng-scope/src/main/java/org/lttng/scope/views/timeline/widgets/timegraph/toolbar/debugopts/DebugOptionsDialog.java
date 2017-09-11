/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline.widgets.timegraph.toolbar.debugopts;

import static java.util.Objects.requireNonNull;

import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import org.lttng.scope.views.timeline.DebugOptions;

import com.efficios.jabberwocky.common.ConfigOption;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Dialog to configure the debug options at runtime.
 *
 * @author Alexandre Montplaisir
 */
class DebugOptionsDialog extends Dialog<Void> {

    private static final Insets PADDING = new Insets(20.0);
    private static final double SPACING = 10.0;

    private final DebugOptions fOpts;
    private final TabPane fTabPane;

    public DebugOptionsDialog(DebugOptionsButton button) {
        fOpts = button.getDebugOptions();

        setTitle(Messages.debugOptionsDialogTitle);
        setHeaderText(Messages.debugOptionsDialogName);

        ButtonType resetToDefaultButtonType = new ButtonType(Messages.resetDefaultsButtonLabel, ButtonData.LEFT);
        getDialogPane().getButtonTypes().addAll(resetToDefaultButtonType, ButtonType.CANCEL, ButtonType.OK);

        fTabPane = new TabPane(getGeneralTab(),
                getLoadingOverlayTab(),
                getZoomTab(),
                getStateIntervalsTab(),
                getTooltipTab());
        getDialogPane().setContent(fTabPane);

        /*
         * Restore the last-selected tab (that state is saved in the button),
         * and re-bind on the new dialog so that the property continues getting
         * updated.
         */
        fTabPane.getSelectionModel().select(button.lastSelectedDialogProperty().get());
        button.lastSelectedDialogProperty().bind(fTabPane.getSelectionModel().selectedIndexProperty());

        /* What to do when the dialog is closed */
        setResultConverter(dialogButton -> {
            fTabPane.getSelectionModel().getSelectedIndex();

            if (dialogButton != ButtonType.OK) {
                return null;
            }
            /*
             * Set the debug options according to the current contents of the
             * dialog.
             */
            getAllPropertySetters().forEach(PropertySetter::save);

            return null;
        });

        /* Define how to "Reset Defaults" button works */
        getDialogPane().lookupButton(resetToDefaultButtonType).addEventFilter(ActionEvent.ACTION, e -> {
            /*
             * This button should not close the dialog. Consuming the event here
             * will prevent the dialog from closing.
             */
            e.consume();

            getAllPropertySetters().forEach(ps -> {
                ConfigOption<?> option = ps.getOption();
                option.resetToDefault();
                ps.load();
            });

        });

    }

    private Stream<PropertySetter> getAllPropertySetters() {
        return fTabPane.getTabs().stream()
                .flatMap(tab -> ((VBox) tab.getContent()).getChildren().stream())
                .map(e -> requireNonNull((PropertySetter) e));
    }

    // ------------------------------------------------------------------------
    // Tab classes
    // ------------------------------------------------------------------------

    private static class DebugOptionsDialogTab extends Tab {

        public DebugOptionsDialogTab(@Nullable String name, Node... contents) {
            VBox page = new VBox(contents);
            page.setPadding(PADDING);
            page.setSpacing(SPACING);

            setClosable(false);
            setText(name);
            setContent(page);
        }

    }

    private Tab getGeneralTab() {
        return new DebugOptionsDialogTab(Messages.tabNameGeneral,
                new CheckBoxControl(Messages.controlPaintingEnabled, fOpts.isPaintingEnabled),
                new IntegerTextField(Messages.controlEntryPadding, fOpts.entryPadding),
                new DoubleTextField(Messages.controlRenderRangePadding, fOpts.renderRangePadding),
                new IntegerTextField(Messages.controlUIUpdateDelay, fOpts.uiUpdateDelay),
                new CheckBoxControl(Messages.controlHScrollEnabled, fOpts.isScrollingListenersEnabled));
    }

    private Tab getLoadingOverlayTab() {
        return new DebugOptionsDialogTab(Messages.tabNameLoadingOverlay,
                new CheckBoxControl(Messages.controlLoadingOverlayEnabled, fOpts.isLoadingOverlayEnabled),
                new ColorControl(Messages.controlLoadingOverlayColor, fOpts.loadingOverlayColor),
                new DoubleTextField(Messages.controlLoadingOverlayFullOpacity, fOpts.loadingOverlayFullOpacity),
//                new DoubleTextField(Messages.controlLoadingOverlayTransparentOpacity, fOpts.loadingOverlayTransparentOpacity),
                new DoubleTextField(Messages.controlLoadingOverlayFadeIn, fOpts.loadingOverlayFadeInDuration),
                new DoubleTextField(Messages.controlLoadingOverlayFadeOut, fOpts.loadingOverlayFadeOutDuration));
    }

    private Tab getZoomTab() {
        return new DebugOptionsDialogTab(Messages.tabNameZoom,
                new IntegerTextField(Messages.controlZoomAnimationDuration + " (unused)", fOpts.zoomAnimationDuration), //$NON-NLS-1$
                new DoubleTextField(Messages.controlZoomStep, fOpts.zoomStep),
                new CheckBoxControl(Messages.controlZoomPivotOnSelection, fOpts.zoomPivotOnSelection),
                new CheckBoxControl(Messages.controlZoomPivotOnMousePosition, fOpts.zoomPivotOnMousePosition));
    }

    private Tab getStateIntervalsTab() {
        return new DebugOptionsDialogTab(Messages.tabNameIntervals,
                new DoubleTextField(Messages.controlIntervalOpacity, fOpts.stateIntervalOpacity)
                // multi-state Paint ?
                // state label Font ?
                );
    }

    private Tab getTooltipTab() {
        return new DebugOptionsDialogTab(Messages.tabNameTooltips,
                // Tooltip Font picker
                new ColorControl(Messages.controlTooltipFontColor, fOpts.toolTipFontFill));
    }

    // ------------------------------------------------------------------------
    // Property-setting controls
    // ------------------------------------------------------------------------

    private static interface PropertySetter {
        ConfigOption<?> getOption();
        void load();
        void save();
    }

    private static class CheckBoxControl extends CheckBox implements PropertySetter {

        private final ConfigOption<Boolean> fOption;

        public CheckBoxControl(@Nullable String labelText, ConfigOption<Boolean> option) {
            fOption = option;
            setText(labelText);
            load();
        }

        @Override
        public ConfigOption<?> getOption() {
            return fOption;
        }

        @Override
        public void load() {
            setSelected(fOption.get());
        }

        @Override
        public void save() {
            fOption.set(isSelected());
        }
    }

    private static class ColorControl extends HBox implements PropertySetter {

        private final ConfigOption<Color> fOption;
        private final ColorPicker fColorPicker;

        public ColorControl(@Nullable String labelText, ConfigOption<Color> option) {
            fOption = option;

            Label label = new Label(labelText + ":"); //$NON-NLS-1$
            fColorPicker = new ColorPicker(option.get());

            getChildren().addAll(label, fColorPicker);
            setAlignment(Pos.CENTER_LEFT);
            setSpacing(SPACING);
        }

        @Override
        public ConfigOption<?> getOption() {
            return fOption;
        }

        @Override
        public void load() {
            fColorPicker.setValue(fOption.get());
        }

        @Override
        public void save() {
            Color color = fColorPicker.getValue();
            if (color != null) {
                fOption.set(color);
            }
        }
    }

    private static abstract class TextFieldControl<T extends Number> extends HBox implements PropertySetter {

        private final ConfigOption<T> fOption;
        private final TextField fTextField = new TextField();

        protected TextFieldControl(@Nullable String labelText, ConfigOption<T> option) {
            fOption = option;

            Label label = new Label(labelText + ":"); //$NON-NLS-1$
            load();

            getChildren().addAll(label, fTextField);
            setAlignment(Pos.CENTER_LEFT);
            setSpacing(SPACING);
        }

        @Override
        public ConfigOption<T> getOption() {
            return fOption;
        }

        @Override
        public void load() {
            fTextField.setText(fOption.get().toString());
        }

        @Override
        public abstract void save();

        protected TextField getTextField() {
            return fTextField;
        }
    }

    private static class IntegerTextField extends TextFieldControl<Integer> {

        public IntegerTextField(@Nullable String labelText, ConfigOption<Integer> option) {
            super(labelText, option);
        }

        @Override
        public void save() {
            String text = getTextField().getText();
            Integer value = Ints.tryParse(text);
            if (value == null) {
                getOption().resetToDefault();
                load();
            } else {
                getOption().set(value);
            }
        }
    }

    private static class DoubleTextField extends TextFieldControl<Double> {

        public DoubleTextField(@Nullable String labelText, ConfigOption<Double> option) {
            super(labelText, option);
        }

        @Override
        public void save() {
            String text = getTextField().getText();
            Double value = Doubles.tryParse(text);
            if (value == null) {
                getOption().resetToDefault();
                load();
            } else {
                getOption().set(value);
            }
        }
    }

}
