package MiniSecDash;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.beans.property.SimpleStringProperty;
import java.util.stream.Collectors;

public class Main extends Application {
    private NetworkGraph graph = new NetworkGraph();
    private ScannerService scanner = new ScannerService(graph);

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        // TableView to show discovered devices
        TableView<Device> table = new TableView<>();

        TableColumn<Device, String> ipColumn = new TableColumn<>("IP Address");
        ipColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getIpAddress()));

        TableColumn<Device, String> hostColumn = new TableColumn<>("Host Name");
        hostColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getHostName()));

        TableColumn<Device, String> portsColumn = new TableColumn<>("Open Ports");
        portsColumn.setCellValueFactory(c -> {
            var ports = c.getValue().getOpenPorts().stream()
                    .map(p -> p.getPortNumber() + "(" + p.getStatus() + ")")
                    .collect(Collectors.toList());
            String text;
            if (ports.size() <= 2) {
                text = String.join(", ", ports);
            } else if (ports.size() == 0) {
                text = "";
            } else {
                text = ports.get(0) + ", " + ports.get(1) + " ...";
            }
            return new SimpleStringProperty(text);
        });

        TableColumn<Device, String> newColumn = new TableColumn<>("New");
        newColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isNew() ? "Yes" : "No"));

        table.getColumns().addAll(ipColumn, hostColumn, portsColumn, newColumn);
        table.setItems(graph.getDevices());
        root.setCenter(table);

        // Right-side details pane (updates when a row is selected)
        Label detailIp = new Label("IP: ");
        Label detailHost = new Label("Host: ");
        TextArea detailPorts = new TextArea();
        detailPorts.setEditable(false);
        detailPorts.setWrapText(true);
        detailPorts.setPrefWidth(250);
        detailPorts.setPrefHeight(200);

        VBox detailsPane = new VBox(8, detailIp, detailHost, new Label("Open Ports:"), detailPorts);
        detailsPane.setPadding(new Insets(10));
        detailsPane.setPrefWidth(300);
        root.setRight(detailsPane);

        // Double-click a row to show detailed device info (all ports + statuses)
        table.setRowFactory(tv -> {
            TableRow<Device> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    Device d = row.getItem();
                    StringBuilder sb = new StringBuilder();
                    sb.append("IP: ").append(d.getIpAddress()).append("\n");
                    sb.append("Host: ").append(d.getHostName()).append("\n\n");
                    sb.append("Open ports:\n");
                    if (d.getOpenPorts().isEmpty()) {
                        sb.append("(none)\n");
                    } else {
                        d.getOpenPorts().forEach(p -> sb.append(p.getPortNumber()).append(" (").append(p.getStatus()).append(")\n"));
                    }

                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.setTitle("Device Details");
                    info.setHeaderText(d.getIpAddress());
                    TextArea ta = new TextArea(sb.toString());
                    ta.setEditable(false);
                    ta.setWrapText(true);
                    ta.setPrefWidth(400);
                    ta.setPrefHeight(300);
                    info.getDialogPane().setContent(ta);
                    info.showAndWait();
                }
            });
            return row;
        });

        // Update details pane when selection changes
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel == null) {
                detailIp.setText("IP: ");
                detailHost.setText("Host: ");
                detailPorts.setText("");
            } else {
                detailIp.setText("IP: " + newSel.getIpAddress());
                detailHost.setText("Host: " + newSel.getHostName());
                StringBuilder sb = new StringBuilder();
                if (newSel.getOpenPorts().isEmpty()) {
                    sb.append("(none)\n");
                } else {
                    newSel.getOpenPorts().forEach(p -> sb.append(p.getPortNumber()).append(" (").append(p.getStatus()).append(")\n"));
                }
                detailPorts.setText(sb.toString());
            }
        });

        // Progress label at bottom
        Label progressLabel = new Label("Scanned 0 / 0");
        HBox bottomBox = new HBox(progressLabel);
        bottomBox.setPadding(new Insets(10));
        bottomBox.setAlignment(Pos.CENTER_LEFT);
        root.setBottom(bottomBox);

        // Create the input controls
        Label promptLabel = new Label("Target Network (e.g., 192.168.1.0):");
        TextField ipField = new TextField("192.168.1.0"); // Default text
        Button scanButton = new Button("Start Scan");
        Button cancelButton = new Button("Cancel");
        cancelButton.setDisable(true);

        // Arrange controls in a horizontal box
        HBox topControls = new HBox(10); // 10px spacing
        topControls.setPadding(new Insets(10)); // 10px padding
        topControls.setAlignment(Pos.CENTER);
        topControls.getChildren().addAll(promptLabel, ipField, scanButton, cancelButton);

        // Add the controls to the top of the BorderPane
        root.setTop(topControls);

        // Refresh the table when the underlying ObservableList changes
        graph.getDevices().addListener((ListChangeListener<Device>) c -> Platform.runLater(() -> table.refresh()));

        // Connect the button to the ScannerService
        scanButton.setOnAction(event -> {
            String baseIp = ipField.getText();

            // Simple validation (you can make this more robust)
            if (baseIp != null && !baseIp.isEmpty() && baseIp.contains(".")) {
                // Remove the last number to get the "base"
                String baseNetwork = baseIp.substring(0, baseIp.lastIndexOf('.'));

                // initialize progress label
                progressLabel.setText("Scanned 0 / 254    Devices: 0");

                // disable start while scanning and enable cancel
                scanButton.setDisable(true);
                cancelButton.setDisable(false);

                // register progress callback to update UI
                scanner.setProgressCallback(done -> Platform.runLater(() -> {
                    progressLabel.setText("Scanned " + done + " / 254    Devices: " + graph.getDevices().size());
                    // when finished re-enable start and disable cancel
                    if (done >= 254) {
                        scanButton.setDisable(false);
                        cancelButton.setDisable(true);
                    }
                }));

                scanner.startScan(baseNetwork);
            }
        });

        // Cancel button stops the current scan and allows restart
        cancelButton.setOnAction(evt -> {
            scanner.cancelScan();
            progressLabel.setText("Scan cancelled");
            scanButton.setDisable(false);
            cancelButton.setDisable(true);
        });

        // Shutdown executor when window closes
        primaryStage.setOnCloseRequest(event -> scanner.shutdown());

        primaryStage.setTitle("Security Dashboard");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
