/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeElement;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeRender;
import org.lttng.scope.tmf2.views.ui.jfx.JfxUtils;

import com.efficios.jabberwocky.project.ITraceProject;
import com.sun.javafx.scene.control.skin.TreeViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;

import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;

/**
 * Extension of {@link TreeView} which exposes some of its components, like
 * Scrollbars.
 *
 * It also supports specifying a static visibility modifier for the scrollbars,
 * similar to a {@link ScrollPane}'s bar policy.
 *
 * This class makes us of some internal, non-API JavaFX objects and methods and
 * thus, might break once the codebase moves to a newer version.
 *
 * @author Alexandre Montplaisir
 */
@SuppressWarnings("restriction")
public class TimeGraphWidgetTreeArea extends BorderPane {

    private final double fEntryHeight;

    private final TreeAreaTreeView fTreeView;

    /**
     * Constructor
     */
    public TimeGraphWidgetTreeArea(double entryHeight, ObjectProperty<@Nullable ITraceProject<?, ?>> targetTraceProjectProperty) {
        TreeItem<String> treeRoot = new TreeItem<>();
        treeRoot.setExpanded(true);

        TreeAreaTreeView treeView = new TreeAreaTreeView(treeRoot);
        treeView.setFixedCellSize(entryHeight);

        /*
         * Instead of using the TreeView's scrollbar, whose visibility and size are
         * managed internally by the super-class, we will instantiate our own and will
         * bind its important properties to the "real" one.
         */
        ScrollBar dummyScrollBar = new ScrollBar();
        dummyScrollBar.setOrientation(Orientation.HORIZONTAL);
        ScrollBar realHScrollBar = treeView.getHBar();
        dummyScrollBar.valueProperty().bindBidirectional(realHScrollBar.valueProperty());
        dummyScrollBar.visibleAmountProperty().bind(treeView.widthProperty().divide(2));

        /* Add the "children" to this BorderPane */
        setCenter(treeView);
        setBottom(dummyScrollBar);

        treeView.setCellFactory(view -> new TreeAreaTreeViewCell());

        fEntryHeight = entryHeight;
        fTreeView = treeView;
    }

    public void clear() {
        fTreeView.setRoot(null);
    }

    public ScrollBar getVerticalScrollBar() {
        return fTreeView.getVBar();
    }

    public DoubleBinding currentHeightProperty() {
        return fTreeView.expandedItemCountProperty().multiply((fEntryHeight));
    }

    public void updateTreeContents(TimeGraphTreeRender treeRender) {
        TreeItem<String> newRoot = getTreeItemForElement(treeRender.getRootElement());
        newRoot.setExpanded(true);
        Platform.runLater(() -> {
            fTreeView.setRoot(newRoot);
        });
    }

    private static TreeItem<String> getTreeItemForElement(TimeGraphTreeElement element) {
        TreeItem<String> treeItem = new TreeItem<>(element.getName());
        List<TreeItem<String>> childItems = element.getChildElements().stream()
                .map(treeElem -> getTreeItemForElement(treeElem))
                .collect(Collectors.toList());
        if (!childItems.isEmpty()) {
            treeItem.getChildren().addAll(childItems);
        }

        // TODO Correctly manage sub-trees being expanded. Disallow it for now.
        treeItem.setExpanded(true);
        treeItem.addEventHandler(TreeItem.branchCollapsedEvent(),
                event -> event.getTreeItem().setExpanded(true));

        return treeItem;
    }

    // ------------------------------------------------------------------------
    // Methods related to the Tree area
    // ------------------------------------------------------------------------

    private static List<TreeItem<String>> getTreeItemNames(TimeGraphTreeRender treeRender) {
        return treeRender.getAllTreeElements().stream()
                .map(elem -> new TreeItem<>(elem.getName()))
                .collect(Collectors.toList());
    }

    // TODO Background could be re-added by salvaging this
//    private static Node prepareTreeContents(TimeGraphTreeRender treeRender, ReadOnlyDoubleProperty widthProperty) {
//        /* Prepare the tree element objects */
//        List<Label> treeElements = treeRender.getAllTreeElements().stream()
//                // TODO Put as a real tree. TreeView ?
//                .map(elem -> new Label(elem.getName()))
//                .peek(label -> {
//                    label.setPrefHeight(ENTRY_HEIGHT);
//                    label.setPadding(new Insets(0, LABEL_SIDE_MARGIN, 0, LABEL_SIDE_MARGIN));
//                    /*
//                     * Re-set the solid background for the labels, so we do not
//                     * see the background lines through.
//                     */
//                    label.setStyle(BACKGROUND_STYLE);
//                })
//                .collect(Collectors.toList());
//
//        VBox treeElemsBox = new VBox(); // Change to TreeView eventually ?
//        treeElemsBox.getChildren().addAll(treeElements);
//
//        /* Prepare the background layer with the horizontal alignment lines */
//        List<Line> lines = DoubleStream.iterate((ENTRY_HEIGHT / 2), y -> y + ENTRY_HEIGHT)
//                .limit(treeElements.size())
//                .mapToObj(y -> {
//                    Line line = new Line();
//                    line.startXProperty().bind(JfxUtils.ZERO_PROPERTY);
//                    line.endXProperty().bind(widthProperty);
//                    line.setStartY(y);
//                    line.setEndY(y);
//
//                    line.setStroke(BACKGROUD_LINES_COLOR);
//                    line.setStrokeWidth(1.0);
//                    return line;
//                })
//                .collect(Collectors.toList());
//        Pane background = new Pane();
//        background.getChildren().addAll(lines);
//
//        /* Put the background layer and the Tree View into their containers */
//        StackPane stackPane = new StackPane(background, treeElemsBox);
//        stackPane.setStyle(BACKGROUND_STYLE);
//        return stackPane;
//    }


    // ------------------------------------------------------------------------
    // Helper inner classes
    // ------------------------------------------------------------------------

    private static class TreeAreaTreeViewCell extends TreeCell<String> {

        public TreeAreaTreeViewCell() {
            // TODO Font size etc. could be managed here. Might be needed on ENTRY_HEIGHT
            // becomes variable
            setPadding(Insets.EMPTY);
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(null);
            if (empty) {
                setText(null);
            } else {
                setText(item);
            }
        }
    }

    private static class TreeAreaTreeView extends TreeView<String> {

        private TreeAreaTreeViewSkin fSkin = new TreeAreaTreeViewSkin(this);

        public TreeAreaTreeView(TreeItem<String> root) {
            super(root);

            /*
             * Completely hide both scrollbars. This works much better than trying to set
             * their 'visible' property, which are constantly being changed by the
             * superclass.
             */
            getHBar().minHeightProperty().bind(JfxUtils.ZERO_PROPERTY);
            getHBar().prefHeightProperty().bind(JfxUtils.ZERO_PROPERTY);
            getHBar().maxHeightProperty().bind(JfxUtils.ZERO_PROPERTY);

            getVBar().minWidthProperty().bind(JfxUtils.ZERO_PROPERTY);
            getVBar().prefWidthProperty().bind(JfxUtils.ZERO_PROPERTY);
            getVBar().maxWidthProperty().bind(JfxUtils.ZERO_PROPERTY);

            lookupAll(".scroll-bar").forEach(node -> node.setStyle("-fx-base: transparent;")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        @Override
        protected TreeAreaTreeViewSkin createDefaultSkin() {
            return fSkin;
        }

        private TreeAreaTreeViewSkin retrieveSkin() {
            return fSkin;
        }

        /**
         * Retrieve the horizontal scrollbar
         *
         * @return The horizontal scrollbar
         */
        public ScrollBar getHBar() {
            return retrieveSkin().getFlow().retrieveHbar();
        }

        /**
         * Retrieve the vertical scrollbar
         *
         * @return The vertical scrollbar
         */
        public ScrollBar getVBar() {
            return retrieveSkin().getFlow().retrieveVbar();
        }
    }

    private static class TreeAreaTreeViewSkin extends TreeViewSkin<String> {

        public TreeAreaTreeViewSkin(TreeAreaTreeView treeView) {
            super(treeView);
        }

        @Override
        protected TreeAreaVirtualFlow createVirtualFlow() {
            return new TreeAreaVirtualFlow();
        }

        public TreeAreaVirtualFlow getFlow() {
            return (TreeAreaVirtualFlow) requireNonNull(flow);
        }

    }

    @SuppressWarnings("rawtypes")
    private static class TreeAreaVirtualFlow extends VirtualFlow {

        public ScrollBar retrieveHbar() {
            return requireNonNull(getHbar());
        }

        public ScrollBar retrieveVbar() {
            return requireNonNull(getVbar());
        }

    }
}
