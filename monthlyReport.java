package nfc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.util.List;
import java.util.ArrayList;
import javafx.scene.control.TextField; // ✅ Use this
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;



public class monthlyReport {
	private StudentInfo getStudentInfoFromDB(int childId) {
	    String sql = "SELECT child_id, name, parent_name, parent_contact FROM children WHERE child_id = ?";
	    try (Connection conn = DatabaseConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setInt(1, childId);
	        ResultSet rs = ps.executeQuery();
	        if (rs.next()) {
	            return new StudentInfo(
	                String.valueOf(rs.getInt("child_id")),
	                rs.getString("name"),
	                rs.getString("parent_name"),
	                rs.getString("parent_contact")
	            );
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    // fallback if not found
	    return new StudentInfo(String.valueOf(childId), "Unknown", "Unknown", "Unknown");
	}


	private List<AttendanceRow> getAttendanceRowsForStudentMonth(int childId, int month, int year) {
	    List<AttendanceRow> list = new ArrayList<>();
	    String sql = """
	        SELECT a.date, c.child_id, c.name, a.is_present, a.reason, a.check_in_time, a.check_out_time
	        FROM children c
	        LEFT JOIN attendance_status a ON c.child_id = a.child_id
	        WHERE c.child_id = ? AND MONTH(a.date) = ? AND YEAR(a.date) = ?
	        ORDER BY a.date
	    """;
	    try (Connection conn = DatabaseConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setInt(1, childId);
	        ps.setInt(2, month);
	        ps.setInt(3, year);

	        ResultSet rs = ps.executeQuery();
	        while (rs.next()) {
	            int id = rs.getInt("child_id");
	            String name = rs.getString("name");
	            boolean present = rs.getObject("is_present") != null && rs.getInt("is_present") == 1;
	            String reason = rs.getString("reason") != null ? rs.getString("reason") : "";
	            String status = present ? "attend" : "absence";
	            String checkIn = rs.getString("check_in_time") != null ? rs.getString("check_in_time") : "";
	            String checkOut = rs.getString("check_out_time") != null ? rs.getString("check_out_time") : "";
	            LocalDate date = rs.getDate("date") != null ? rs.getDate("date").toLocalDate() : null;

	            // Only add rows with a date (skip null/empty)
	            if (date != null)
	                list.add(new AttendanceRow(id, name, status, reason, checkIn, checkOut, date));
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return list;
	}
    private VBox root;
    private TableView<StudentMonthlyAttendance> table;
    private ComboBox<String> monthDropdown;
    private ComboBox<Integer> yearDropdown;

    public monthlyReport() {
        root = new VBox(12);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Monthly Attendance Report");
        title.setFont(Font.font("Poppins", FontWeight.BOLD, 24));

        // Month Dropdown
        monthDropdown = new ComboBox<>();
        monthDropdown.getItems().addAll(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        );
        int currentMonthIndex = java.time.LocalDate.now().getMonthValue() - 1; // 0-based for ComboBox
        monthDropdown.setValue(monthDropdown.getItems().get(currentMonthIndex));


        // Year Dropdown (e.g., from 2020 to current year)
        yearDropdown = new ComboBox<>();
        int currentYear = java.time.Year.now().getValue();
        for (int y = 2020; y <= currentYear; y++) {
            yearDropdown.getItems().add(y);
        }
        yearDropdown.setValue(currentYear);

        // Add listeners to auto refresh on selection change
        monthDropdown.setOnAction(e -> loadMonthlyReport());
        yearDropdown.setOnAction(e -> loadMonthlyReport());

        HBox controls = new HBox(15, new Label("Select Month:"), monthDropdown,
                                     new Label("Year:"), yearDropdown);
        controls.setAlignment(Pos.CENTER);

        // Table Setup
        table = new TableView<>();
        setupTable();
        
         root.getChildren().addAll(title, controls, table);

        // Initial load
        loadMonthlyReport();
    }

    private void setupTable() {
        TableColumn<StudentMonthlyAttendance, Integer> idCol = new TableColumn<>("Child ID");
        idCol.setCellValueFactory(data -> data.getValue().childIdProperty().asObject());
        idCol.setPrefWidth(120);

        TableColumn<StudentMonthlyAttendance, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> data.getValue().nameProperty());
        nameCol.setPrefWidth(150);

        TableColumn<StudentMonthlyAttendance, String> percentCol = new TableColumn<>("Attendance ");
        percentCol.setCellValueFactory(data -> data.getValue().attendancePercentProperty());
        percentCol.setPrefWidth(150);

        
        TableColumn<StudentMonthlyAttendance, String> performanceCol = new TableColumn<>("Performance");
        performanceCol.setCellValueFactory(data -> data.getValue().performanceProperty());
        performanceCol.setPrefWidth(150);

        TableColumn<StudentMonthlyAttendance, Void> downloadCol = new TableColumn<>("Download");
        downloadCol.setPrefWidth(140);
        downloadCol.setCellFactory(col -> new TableCell<StudentMonthlyAttendance, Void>() {
            private final Button btn = new Button("⬇");
            {
               btn.setOnAction(e -> {
                    StudentMonthlyAttendance row = getTableView().getItems().get(getIndex());

                    StudentInfo info = getStudentInfoFromDB(row.getChildId());

                    int selectedMonth = monthDropdown.getSelectionModel().getSelectedIndex() + 1;
                    int selectedYear = yearDropdown.getValue();
                    String selectedMonthName = monthDropdown.getValue();

                    List<AttendanceRow> days = getAttendanceRowsForStudentMonth(row.getChildId(), selectedMonth, selectedYear);

                    
                    
                    // Pass performance and percent
                    showMonthlyReportPreview(
                        info, 
                        days, 
                        selectedMonthName, 
                        String.valueOf(selectedYear), 
                        row.getAttendancePercent(), 
                        row.getPerformance() // Pass performance value
                    );
                });

            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        downloadCol.setPrefWidth(100);


        table.getColumns().addAll(idCol, nameCol,percentCol, performanceCol, downloadCol);
        
        table.setRowFactory(tv -> new TableRow<StudentMonthlyAttendance>() {
            @Override
            protected void updateItem(StudentMonthlyAttendance item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if ("Good".equalsIgnoreCase(item.getPerformance())) {
                    setStyle("-fx-background-color: #e7ffe9;"); // green
                } else {
                    setStyle("-fx-background-color: #ffe7e7;"); // red
                }
            }
        });
        percentCol.setCellFactory(col -> new TableCell<StudentMonthlyAttendance, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    StudentMonthlyAttendance att = getTableView().getItems().get(getIndex());
                    setStyle("Good".equalsIgnoreCase(att.getPerformance()) ?
                        "-fx-text-fill: #087400; -fx-font-weight: bold;" :
                        "-fx-text-fill: #c62828; -fx-font-weight: bold;");
                }
            }
        });
        performanceCol.setCellFactory(col -> new TableCell<StudentMonthlyAttendance, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("Good".equalsIgnoreCase(item) ?
                        "-fx-text-fill: #087400; -fx-font-weight: bold;" :
                        "-fx-text-fill: #c62828; -fx-font-weight: bold;");
                }
            }
        });
    }

    public void showMonthlyReportPreview(StudentInfo info, List<AttendanceRow> days, String month, String year,String attendancePercent, String performance) {
        Stage previewStage = new Stage();
        previewStage.setTitle("Preview & Edit Report for " + info.childName);

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        // Logo and Title
        Image logoImg;
        try {
            logoImg = new Image(getClass().getResourceAsStream("/nfc/beecaliph.png"));
        } catch (Exception e) {
            logoImg = null; // fallback or placeholder
        }
        ImageView logo = new ImageView(logoImg);
        logo.setFitHeight(50); logo.setFitWidth(50);

        Label title = new Label("BEE CALIPH STUDENT REPORT");
        title.setStyle("-fx-font-size: 22px; -fx-text-fill: orange; -fx-font-weight: bold;");

      

        GridPane infoPane = new GridPane();
        infoPane.setVgap(4);
        infoPane.setHgap(6);
        infoPane.addRow(0, new Label("CHILD ID:"), new Label(info.childId));
        infoPane.addRow(1, new Label("NAME:"), new Label(info.childName));
        infoPane.addRow(2, new Label("PARENT NAME:"), new Label(info.parentName));
        infoPane.addRow(3, new Label("CONTACT NUMBER:"), new Label(info.parentContact)); 
        infoPane.addRow(4, new Label("MONTH:"), new Label(month.toUpperCase()));
        infoPane.addRow(5, new Label("YEAR:"), new Label(year));
        
        Label performanceLabel = new Label(
                "Attendance: " + attendancePercent + "   |   Performance: " + performance.toUpperCase()
            );
            performanceLabel.setFont(Font.font("Poppins", FontWeight.BOLD, 15));
            if ("Good".equalsIgnoreCase(performance)) {
                performanceLabel.setStyle(
                    "-fx-background-color: #e7ffe9; -fx-text-fill: #087400; -fx-padding: 10 0 10 10; -fx-background-radius: 8;"
                );
            } else {
                performanceLabel.setStyle(
                    "-fx-background-color: #ffe7e7; -fx-text-fill: #c62828; -fx-padding: 10 0 10 10; -fx-background-radius: 8;"
                );
            }
            performanceLabel.setMaxWidth(Double.MAX_VALUE);

        // TableView for Attendance (read-only or editable as you wish)
        TableColumn<AttendanceRow, String> dateCol = new TableColumn<>("DATE");
        dateCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getDate() != null
                ? data.getValue().getDate().format(DateTimeFormatter.ofPattern("d MMM yyyy"))
                : "")
        );
        dateCol.setPrefWidth(100);

        TableColumn<AttendanceRow, String> statusCol = new TableColumn<>("STATUS");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setPrefWidth(80);

        TableColumn<AttendanceRow, String> checkInCol = new TableColumn<>("CHECK-IN");
        checkInCol.setCellValueFactory(data -> new SimpleStringProperty(
            formatTime(data.getValue().getCheckInTime()))
        );
        checkInCol.setPrefWidth(80);

        TableColumn<AttendanceRow, String> checkOutCol = new TableColumn<>("CHECK-OUT");
        checkOutCol.setCellValueFactory(data -> new SimpleStringProperty(
            formatTime(data.getValue().getCheckOutTime()))
        );
        checkOutCol.setPrefWidth(80);

        TableColumn<AttendanceRow, String> reasonCol = new TableColumn<>("REASON");
        reasonCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getReason()));
        reasonCol.setPrefWidth(100);

        TableView<AttendanceRow> attendanceTable = new TableView<>(FXCollections.observableArrayList(days));
        
		// Add columns to table
        attendanceTable.getColumns().addAll(dateCol, statusCol, checkInCol, checkOutCol, reasonCol);

        // Optional: autosize to content
        attendanceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        attendanceTable.setPrefHeight(200);

        // Button to Save as PDF
        Button saveBtn = new Button("Save as PDF");
        saveBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            fileChooser.setInitialFileName(info.childName + "_report.pdf");
            File file = fileChooser.showSaveDialog(previewStage);

            if (file != null) {
                try {
                    PDFReport.generateStudentMonthlyReport(
                        file.getAbsolutePath(), "src/nfc/beecaliph.png", info, days, month, year
                    );
                    new Alert(Alert.AlertType.INFORMATION, "PDF saved!").showAndWait();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Failed to generate PDF: " + ex.getMessage()).showAndWait();
                }
            }
        });


        layout.getChildren().addAll(logo, title, infoPane, performanceLabel, attendanceTable, saveBtn);
        previewStage.setScene(new Scene(layout, 650, 600));
        previewStage.show();
    }
    private String formatTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return "";
        try {
            DateTimeFormatter input = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            DateTimeFormatter output = DateTimeFormatter.ofPattern("h:mm a");
            LocalDateTime dt = LocalDateTime.parse(timeStr, input);
            return dt.format(output);
        } catch (Exception e) {
            // If already formatted or invalid, return as is
            return timeStr;
        }
    }



	private void loadMonthlyReport() {
        int month = monthDropdown.getSelectionModel().getSelectedIndex() + 1; // 1-12
        int year = yearDropdown.getValue();

        ObservableList<StudentMonthlyAttendance> data = FXCollections.observableArrayList();

        String sql = """
        	    SELECT
        	        c.child_id,
        	        c.name,
        	        COUNT(a.id) AS total_records,
        	        SUM(a.is_present = 1) AS days_present,
        	        (SELECT DAY(LAST_DAY(CONCAT(?, '-', LPAD(?,2,'0'), '-01')))) AS days_in_month,
        	        ROUND(
        	            (SUM(a.is_present = 1) /
        	            (SELECT DAY(LAST_DAY(CONCAT(?, '-', LPAD(?,2,'0'), '-01')))) ) * 100, 2
        	        ) AS attendance_percent,
        	        CASE
        	            WHEN ROUND(
        	                (SUM(a.is_present = 1) /
        	                (SELECT DAY(LAST_DAY(CONCAT(?, '-', LPAD(?,2,'0'), '-01')))) ) * 100, 2
        	            ) >= 80 THEN 'Good'
        	            ELSE 'Poor'
        	        END AS performance
        	    FROM children c
        	    LEFT JOIN attendance_status a ON c.child_id = a.child_id
        	        AND MONTH(a.date) = ?
        	        AND YEAR(a.date) = ?
        	    GROUP BY c.child_id, c.name
        	    ORDER BY c.child_id
        	    """;


        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            ps.setInt(3, year);   // for percent calc
            ps.setInt(4, month);
            ps.setInt(5, year);   // for percent calc
            ps.setInt(6, month);
            ps.setInt(7, month);  // for attendance_status join
            ps.setInt(8, year);


            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("child_id");
                String name = rs.getString("name");
                double percent= rs.getDouble("attendance_percent");
                String percentString = String.format("%.2f%%", percent);
                String performance = rs.getString("performance");

                data.add(new StudentMonthlyAttendance(id, name,percentString, performance));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        table.setItems(data);
    }

    public VBox getRoot() {
        return root;
    }

    
    // You will need to create this StudentMonthlyAttendance model class with properties
}
