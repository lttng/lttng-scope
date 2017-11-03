package org.lttng.scope.views.jfx.examples;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.efficios.jabberwocky.ctf.trace.event.CtfTraceEvent;
import com.efficios.jabberwocky.ctf.trace.generic.GenericCtfTrace;
import com.efficios.jabberwocky.trace.TraceIterator;
import com.google.common.collect.Iterators;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class TableViewTesting  extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        if (primaryStage == null) {
            return;
        }

        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Select CTF trace directory");
        File dir = dirChooser.showDialog(primaryStage);
        if (dir == null) {
            return;
        }

        Path tracePath = dir.toPath();
        GenericCtfTrace trace = new GenericCtfTrace(tracePath);

        // FIXME Reading all events into memory
        List<CtfTraceEvent> events = new ArrayList<>();
        try (TraceIterator<CtfTraceEvent> iter = trace.iterator()) {
            Iterators.addAll(events, iter);
        }

        ObservableList<CtfTraceEvent> eventList = FXCollections.observableList(events);

        /* Setup the filter text field */
        TextField filterField = new TextField();
        FilteredList<CtfTraceEvent> filteredData = new FilteredList<>(eventList, p -> true);
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(event -> {
                // If filter text is empty, display everything
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                if (event.toString().toLowerCase().contains(newValue.toLowerCase())) {
                    return true;
                }
                return false;
            });
        });

        /* Setup the table */
        TableColumn<CtfTraceEvent, String> col1 = new TableColumn<>("Timestamp");
        col1.setCellValueFactory(p -> new ReadOnlyObjectWrapper(p.getValue().getTimestamp()));
        col1.setSortable(false);

        TableColumn<CtfTraceEvent, String> col2 = new TableColumn<>("CPU");
        col2.setCellValueFactory(p -> new ReadOnlyObjectWrapper(p.getValue().getCpu()));
        col2.setSortable(false);

        TableColumn<CtfTraceEvent, String> col3 = new TableColumn<>("All");
        col3.setCellValueFactory(p -> new ReadOnlyObjectWrapper(p.getValue().toString()));
        col3.setSortable(false);

        TableView<CtfTraceEvent> tableView = new TableView<>();
        tableView.setFixedCellSize(24);
        tableView.setCache(true);
        tableView.setCacheHint(CacheHint.SPEED);
        tableView.getColumns().addAll(col1, col2, col3);
        tableView.setItems(filteredData);

        /* Setup the full scene */
        Label filterLabel = new Label("Filter:");
        filterLabel.setPadding(new Insets(5));
        HBox filterBox = new HBox(filterLabel, filterField);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        filterBox.setPadding(new Insets(5));

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(filterBox);
        borderPane.setCenter(tableView);

        primaryStage.setScene(new Scene(borderPane, 800, 350));
        primaryStage.setTitle("tableview");
        primaryStage.show();
    }

}