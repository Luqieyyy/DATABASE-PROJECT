package nfc;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Callback;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class dailyReport{
    private final VBox root;
    private final TableView<AttendanceRow> table;
    private final DatePicker datePicker;

    public dailyReport() {
        root = new VBox(24);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(36, 20, 20, 20));
        root.setStyle("-fx-background-color:transparent;");

        Label title = new Label("Daily Attendance Report");
        title.setFont(Font.font("Poppins", FontWeight.BOLD, 28));
        title.setStyle("-fx-text-fill: #222;");

        // Date picker
        HBox dateRow = new HBox(8);
        dateRow.setAlignment(Pos.CENTER_LEFT);
        Label dateLabel = new Label("Select Date:");
        dateLabel.setFont(Font.font("Poppins", 16));
        datePicker = new DatePicker(LocalDate.now());
        datePicker.setPrefWidth(160);
        datePicker.valueProperty().addListener((obs, old, selected) -> loadAttendance(selected));
        dateRow.getChildren().addAll(dateLabel, datePicker);

        // Table
        table = new TableView<>();
        setupTable();

        // Load for today on startup
        loadAttendance(LocalDate.now());

        root.getChildren().addAll(title, dateRow, table);

        // Generate ALL button (UI only)
        Button generateAllBtn = new Button("Generate ALL");
        generateAllBtn.setFont(Font.font("Poppins", 16));
        generateAllBtn.setStyle("-fx-background-color:#FFCB3C;-fx-background-radius:18;-fx-font-weight:bold;");
        VBox.setMargin(generateAllBtn, new Insets(24, 0, 0, 0));
        root.getChildren().add(generateAllBtn);
    }

    public Node getRoot() {
        return root;
    }

    // Table setup
    private void setupTable() {
    	
    	DateTimeFormatter dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    	DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("hh:mm a");
    	
    	
        TableColumn<AttendanceRow, Integer> colId = new TableColumn<>("Child ID");
        colId.setCellValueFactory(data -> data.getValue().childIdProperty().asObject());
        colId.setPrefWidth(80);

        TableColumn<AttendanceRow, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(data -> data.getValue().nameProperty());
        colName.setPrefWidth(120);

        TableColumn<AttendanceRow, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());
        colStatus.setPrefWidth(90);

        TableColumn<AttendanceRow, String> colReason = new TableColumn<>("Reason");
        colReason.setCellValueFactory(data -> data.getValue().reasonProperty());
        colReason.setPrefWidth(120);

        TableColumn<AttendanceRow, String> checkInCol = new TableColumn<>("Check-In");
        checkInCol.setCellValueFactory(cellData -> cellData.getValue().checkInTimeProperty());
        checkInCol.setCellFactory(col -> new TableCell<AttendanceRow, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isEmpty()) {
                    setText("");
                } else {
                    try {
                        LocalDateTime dt = LocalDateTime.parse(item, dbFormatter);
                        setText(dt.format(displayFormatter));
                    } catch (Exception e) {
                        setText(item); // fallback if parsing fails
                    }
                }
            }
        });

        TableColumn<AttendanceRow, String> colCheckOut = new TableColumn<>("Check-out");
        colCheckOut.setCellValueFactory(data -> data.getValue().checkOutTimeProperty());
        colCheckOut.setPrefWidth(80);
        colCheckOut.setCellFactory(col -> new TableCell<AttendanceRow, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isEmpty()) {
                    setText("");
                } else {
                    try {
                        LocalDateTime dt = LocalDateTime.parse(item, dbFormatter);
                        setText(dt.format(displayFormatter));
                    } catch (Exception e) {
                        setText(item); // fallback
                    }
                }
            }
        });

        table.getColumns().setAll(colId, colName, colStatus, colReason, checkInCol, colCheckOut);
        table.setPrefHeight(320);
    }

    // Load attendance from DB for given date
    private void loadAttendance(LocalDate date) {
        table.getItems().clear();

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get all children with LEFT JOIN to attendance_status for the selected date
            String sql = """
                SELECT c.child_id AS child_id, c.name,
                       s.is_present, s.reason,
                       s.check_in_time, s.check_out_time
                  FROM children c
             LEFT JOIN attendance_status s ON c.child_id = s.child_id AND s.date = ?
              ORDER BY c.child_id
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            Date sqlDate = Date.valueOf(date);
            stmt.setDate(1, sqlDate);
            LocalDate selectedDate = datePicker.getValue();

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int childId = rs.getInt("child_id");
                String name = rs.getString("name");
                boolean present = rs.getObject("is_present") != null && rs.getInt("is_present") == 1;
                String reason = rs.getString("reason") != null ? rs.getString("reason") : "";
                String status = present ? "attend" : "absence";
                String checkIn = rs.getString("check_in_time") != null ? rs.getString("check_in_time") : "";
                String checkOut = rs.getString("check_out_time") != null ? rs.getString("check_out_time") : "";
                table.getItems().add(new AttendanceRow(childId, name, status, reason, checkIn, checkOut,selectedDate));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            table.setPlaceholder(new Label("Error loading data"));
        }
    }
}
