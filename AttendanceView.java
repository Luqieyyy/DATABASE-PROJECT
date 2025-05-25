package nfc;

import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.chart.PieChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AttendanceView {

	private static AttendanceView currentInstance;
    private VBox root;
    private TableView<AttendanceRecord> table;
    private ObservableList<AttendanceRecord> masterRecords = FXCollections.observableArrayList();
    private FilteredList<AttendanceRecord> filteredRecords = new FilteredList<>(masterRecords, p -> true);
    private PieChart chart;  // ✅ make it global
    // ─── NEW: datePicker field ────────────────────────────────────────────────────
    private DatePicker datePicker = new DatePicker(LocalDate.now());
    // ────────────────────────────────────────────────────────────────────────────────

    public AttendanceView() {
        currentInstance = this;
        root = new VBox(10);
        root.setPadding(new Insets(20));

        // ─── 2) Create title + clock ───────────────────────────────────
        Label title = new Label("Attendance");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label clockLabel = new Label();
        clockLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #555;");

        // start the clock
        Timeline clock = new Timeline(
          new KeyFrame(Duration.seconds(1), e -> {
            String now = LocalDate.now().format(
              DateTimeFormatter.ofPattern("dd MMM yyyy"))
              + " | "
              + LocalTime.now().withNano(0);
            clockLabel.setText(now);
          })
        );
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        // spacer to push clockLabel to the right
        Region titleSpacer = new Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);

        HBox titleBar = new HBox(10, title, titleSpacer, clockLabel);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        root.getChildren().add(titleBar);

        // ─── 3) Create date‐picker row ───────────────────────────────────
        DatePicker datePicker = new DatePicker(LocalDate.now());
        Button loadBtn = new Button("Load Date");
        loadBtn.setOnAction(e -> loadStudents());

        // spacer to push date controls to the right
        Region dateSpacer = new Region();
        HBox.setHgrow(dateSpacer, Priority.ALWAYS);

        HBox dateBar = new HBox(10,
            new Label("Select Date:"),
            datePicker,
            loadBtn,
            dateSpacer    // pushes everything left of it to the right edge
        );
        dateBar.setAlignment(Pos.CENTER_LEFT);
        root.getChildren().add(dateBar);

        
        // Header bar
        HBox header = new HBox(10);
        header.setPadding(new Insets(10));
        header.setAlignment(Pos.CENTER_RIGHT);

        CheckBox attendanceCheckbox = new CheckBox("Mark All Present");
        attendanceCheckbox.setOnAction(e -> {
            for (AttendanceRecord record : filteredRecords) {
                boolean nowPresent = attendanceCheckbox.isSelected();
                record.setPresent(nowPresent);
                if (nowPresent) {
                    String nowTime = java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    record.setScanTime(nowTime);
                } else {
                    record.setScanTime("");
                }
            }
            table.refresh();
            updateChart(chart);
            AdminDashboard.updateDashboardData();
        });

       // Button viewReportsButton = new Button("View Reports");
       // viewReportsButton.setOnAction(e -> this.showAllReportsWindow());

        ComboBox<String> reasonDropdown = new ComboBox<>();
        reasonDropdown.getItems().addAll("All", "Permission", "Sick", "Unexcused", "Other...");
        reasonDropdown.setValue("All");
        reasonDropdown.setOnAction(e -> {
            String selected = reasonDropdown.getValue();
            filteredRecords.setPredicate(record -> {
                if ("All".equals(selected)) return true;
                if ("Other...".equals(selected)) return "Other...".equals(record.getReason());
                return selected.equals(record.getReason());
            });
        });

        Button clearFilter = new Button("Clear");
        clearFilter.setOnAction(e -> {
            reasonDropdown.setValue("All");
            filteredRecords.setPredicate(p -> true);
        });

     /*   ComboBox<String> bulkReasonDropdown = new ComboBox<>();
        bulkReasonDropdown.getItems().addAll("Permission", "Sick", "Unexcused", "Other...");
        bulkReasonDropdown.setValue("Permission");

       /* Button applyReasonBtn = new Button("Apply Reason");
        applyReasonBtn.setOnAction(e -> {
            String selectedReason = bulkReasonDropdown.getValue();
            for (AttendanceRecord record : filteredRecords) {
                if (!record.isPresent()) {
                    record.setReason(selectedReason);
                }
            }
            table.refresh();
        });*/

     //   header.getChildren().addAll(bulkReasonDropdown);
        header.getChildren().addAll(attendanceCheckbox, reasonDropdown, clearFilter);

   

        // ─── Table setup ────────────────────────────────────────────────────────────
        table = new TableView<>();
        table.setEditable(true);

        TableColumn<AttendanceRecord, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> data.getValue().nameProperty());

        TableColumn<AttendanceRecord, Boolean> presentCol = new TableColumn<>("Present");
        presentCol.setCellValueFactory(cellData -> cellData.getValue().presentProperty());
        presentCol.setCellFactory(CheckBoxTableCell.forTableColumn(presentCol));
        presentCol.setEditable(true);

        TableColumn<AttendanceRecord, String> reasonCol = new TableColumn<>("Reason");
        reasonCol.setCellValueFactory(data -> data.getValue().reasonProperty());
        // (your existing combo-box factory for reasons)...

        // ─── NEW: Check-In & Check-Out columns ───────────────────────────────────
        TableColumn<AttendanceRecord,String> inCol  = new TableColumn<>("Check-In");
        inCol.setCellValueFactory(c -> c.getValue().checkInTimeProperty());
        inCol.setPrefWidth(140);

        TableColumn<AttendanceRecord,String> outCol = new TableColumn<>("Check-Out");
        outCol.setCellValueFactory(c -> c.getValue().checkOutTimeProperty());
        outCol.setPrefWidth(140);
        // ─────────────────────────────────────────────────────────────────────────

        table.getColumns().addAll(nameCol, presentCol, reasonCol, inCol, outCol);
        table.setItems(filteredRecords);
        // ────────────────────────────────────────────────────────────────────────────

        Label chartTitle = new Label("Today's Attendance");
        chartTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button saveBtn = new Button("Save Attendance");
        saveBtn.setOnAction(e -> saveAttendance());

        chart = new PieChart();
        updateChart(chart);

        Button refreshChart = new Button("Refresh Chart");
        refreshChart.setOnAction(e -> updateChart(chart));

        root.getChildren().addAll(header, table, saveBtn, chartTitle, chart, refreshChart);

        loadStudents();
        updateChart(chart);
    }

    // ─── NEW: Revised loadStudents() with MIN/MAX ───────────────────────────────
    private void loadStudents() {
        masterRecords.clear();

        String sql = """
            SELECT c.id, c.name,
                   MIN(a.scan_time) AS check_in,
                   MAX(a.scan_time) AS check_out,
                   CASE WHEN MAX(a.scan_time) IS NOT NULL THEN 1 ELSE 0 END AS is_present
              FROM children c
              LEFT JOIN attendance a
                ON c.id=a.child_id 
               AND DATE(a.scan_time)=?
             GROUP BY c.id, c.name
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(datePicker.getValue()));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                AttendanceRecord r = new AttendanceRecord(
                    rs.getInt("id"),
                    rs.getString("name")
                );
                r.setPresent     (rs.getBoolean("is_present"));
                r.setCheckInTime (rs.getString("check_in"));
                r.setCheckOutTime(rs.getString("check_out"));
                masterRecords.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        table.refresh();
        updateChart(chart);
        AdminDashboard.updateDashboardData();
    }



    private void saveAttendance() {
    	LocalDate today = LocalDate.now();
    	java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());

        try (Connection conn = DatabaseConnection.getConnection()) {
            for (AttendanceRecord record : filteredRecords) {
            	String sql = "REPLACE INTO attendance_status (child_id, date, is_present, reason, scan_time) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, record.getChildId());
                stmt.setDate(2, java.sql.Date.valueOf(today));
                stmt.setBoolean(3, record.isPresent());
                stmt.setString(4, record.getReason());
                String scanTimeStr = record.getScanTime();
                if (scanTimeStr != null && !scanTimeStr.isEmpty()) {
                    stmt.setTimestamp(5, java.sql.Timestamp.valueOf(scanTimeStr));
                } else {
                    stmt.setTimestamp(5, null);  // No time
                }

                stmt.executeUpdate();
            }
            new Alert(Alert.AlertType.INFORMATION, "Attendance saved.").showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateChart(PieChart chart) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT 
                    SUM(CASE WHEN is_present THEN 1 ELSE 0 END) AS present,
                    SUM(CASE WHEN NOT is_present THEN 1 ELSE 0 END) AS absent
                FROM attendance_status 
                WHERE date = CURDATE()
                """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            int present = 0, absent = 0;
            if (rs.next()) {
                present = rs.getInt("present");
                absent = rs.getInt("absent");
            }

            int total = present + absent;

            PieChart.Data presentData = new PieChart.Data(
                "Present (" + present + (total > 0 ? " | " + (present * 100 / total) + "%" : "") + ")", present
            );
            PieChart.Data absentData = new PieChart.Data(
                "Absent (" + absent + (total > 0 ? " | " + (absent * 100 / total) + "%" : "") + ")", absent
            );

            chart.setData(FXCollections.observableArrayList(presentData, absentData));
            

            Tooltip.install(presentData.getNode(), new Tooltip(present + " students present"));
            Tooltip.install(absentData.getNode(), new Tooltip(absent + " students absent"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    public VBox getRoot() {
        return root;
    }
    private void showStudentReport(AttendanceRecord record) {
    	Stage reportStage = new Stage();
    	reportStage.setTitle("Attendance Report - " + record.getName());
    	
    	VBox layout = new VBox(10);
    	layout.setPadding(new Insets(20));
    	layout.setAlignment(Pos.CENTER_LEFT);
    	
    	Label nameLabel = new Label("Name :" + record.getName());
    	Label presentLabel = new  Label();
    	Label absentLabel = new Label();
    	
    	String sql = """
    			SELECT
    				is_present,
    				reason,
    				COUNT(*) AS total 
    			FROM attendance_status
    			WHERE child_id = ?
    			GROUP BY is_present,reason
    			""";
    	try (Connection conn = DatabaseConnection.getConnection();
    			PreparedStatement stmt = conn.prepareStatement(sql)){
    		
    		stmt.setInt(1, record.getChildId());
    		ResultSet rs = stmt.executeQuery();
    		
    		StringBuilder absentReasons = new StringBuilder();
    		int present = 0;
    		
    		while(rs.next()) {
    			boolean isPresent = rs.getBoolean("is_present");
    			String reason = rs.getString("reason");
    			int total= rs.getInt("total");
    			
    			if(isPresent) {
    				present +=total;
    				}else {
    					absentReasons.append("- ").append(reason).append(": ").append(total).append("\n");
    				}
    		}
    		
    		presentLabel.setText("✔ Present: " + present);
            absentLabel.setText("❌ Absent:\n" + absentReasons);
        
    	} catch (Exception e) {
            presentLabel.setText("Error loading data.");
            e.printStackTrace();
        }
    	
    	layout.getChildren().addAll(nameLabel, presentLabel, absentLabel);
        Scene scene = new Scene(layout, 350, 250);
        reportStage.setScene(scene);
        reportStage.show();
    }

    public static void markPresentByChildId(int childId) {
        Platform.runLater(() -> {
            if (currentInstance != null) {

                boolean found = false;
                for (AttendanceRecord record : currentInstance.masterRecords) {
                    if (record.getChildId() == childId) {
                        record.setPresent(true);
                        found = true;
                        break;
                    }
                }

                // Reload attendance reasons and chart from database
                currentInstance.loadStudents();   // ✅ refresh all data (so absent/present list updates)
                currentInstance.updateChart(currentInstance.chart);
                currentInstance.table.refresh();
            }
        });
    }

 // ✅ NEW STATIC FUNCTION to update chart when NFC scanned
    public static void updateChartFromStatic() {
        Platform.runLater(() -> {
            if (currentInstance != null) {
                if (currentInstance.chart != null) {
                    currentInstance.updateChart(currentInstance.chart);
                    currentInstance.table.refresh();
                } else {
                    System.out.println("⚠ Pie chart not available (Attendance tab might be closed). Skipping chart update.");
                }
            } else {
                System.out.println("⚠ AttendanceView not open. Skipping chart update.");
            }
        });
    }
    public static void refreshUI() {
        Platform.runLater(() -> {
            if (currentInstance != null) {
                currentInstance.loadStudents(); // reload the attendance list
                if (currentInstance.chart != null) {
                    currentInstance.updateChart(currentInstance.chart);
                }
                currentInstance.table.refresh();
            }
        });
    }




    
    
    
    
}