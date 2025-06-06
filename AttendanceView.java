
package nfc;

import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.chart.PieChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.sql.*;

public class AttendanceView {

	private static AttendanceView currentInstance;
    private VBox root;
    private TableView<AttendanceRecord> table;
    private ObservableList<AttendanceRecord> masterRecords = FXCollections.observableArrayList();
    private FilteredList<AttendanceRecord> filteredRecords = new FilteredList<>(masterRecords, p -> true);
    private PieChart chart; // ✅ make it global
    // ─── NEW: datePicker field ────────────────────────────────────────────────────
    private DatePicker datePicker;
    // ────────────────────────────────────────────────────────────────────────────────
    private static final DateTimeFormatter DB_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DISPLAY_TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");

    public AttendanceView() {
        currentInstance = this;
        
        datePicker = new DatePicker(LocalDate.now());

        HBox dashboardHeader = new HBox(18);
        dashboardHeader.setAlignment(Pos.CENTER_LEFT);
        dashboardHeader.setPrefHeight(70);
        dashboardHeader.setMaxWidth(Double.MAX_VALUE); // Full width
        dashboardHeader.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #FFD600 90%, #FFC107 100%);"
            + "-fx-border-color: #f4b400; -fx-border-width: 0 0 3 0;"
            + "-fx-background-image: repeating-linear-gradient(to bottom, transparent, transparent 12px, #FECF4D 12px, #FECF4D 15px);"
        );
        ImageView honeyPot = new ImageView(new Image(getClass().getResource("/nfc/hive2.png").toExternalForm()));
        honeyPot.setFitWidth(54);
        honeyPot.setFitHeight(54);

        // Clock (put it to the far right)
        Label clockLabel = new Label();
        clockLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 0 16px;");
        Timeline clock = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> {
                String now = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                    + " | " + LocalTime.now().withNano(0);
                clockLabel.setText(now);
            })
        );
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        Label dashboardTitle = new Label("Attendance");
        dashboardTitle.setFont(Font.font("Impact", FontWeight.EXTRA_BOLD, 44));
        dashboardTitle.setTextFill(Color.web("#181818"));

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        dashboardHeader.getChildren().addAll(honeyPot, dashboardTitle, headerSpacer, clockLabel);
        
        
        root = new VBox(10);
        root.setPadding(new Insets(20));

       
        // ─── 3) Create date‐picker row ───────────────────────────────────
        datePicker = new DatePicker(LocalDate.now());
        Button loadBtn = new Button("Load Date");
        loadBtn.setOnAction(e -> {
            LocalDate selectedDate = datePicker.getValue();
            loadStudents(selectedDate);
            updateChart(chart, selectedDate);
        });
        
        loadBtn.setStyle("-fx-background-color: #FFCB3C;-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #222; -fx-background-radius: 28px;");
        loadBtn.setPrefHeight(50);
        
     
       

        // spacer to push date controls to the right
        Region dateSpacer = new Region();
        HBox.setHgrow(dateSpacer, Priority.ALWAYS);

        Label selectDateLabel = new Label("Select Date:");
        selectDateLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");        
        HBox dateBar = new HBox(10,
            selectDateLabel,
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
        attendanceCheckbox.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #222;");
        attendanceCheckbox.setSelected(false); // force unticked on load

        attendanceCheckbox.setOnAction(e -> {
            for (AttendanceRecord record : filteredRecords) {
                boolean nowPresent = attendanceCheckbox.isSelected();
                if (nowPresent) {
                    String nowTime = java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    record.setCheckInFullTimestamp(nowTime);  // Use setCheckInFullTimestamp here!
                } else {
                    record.setCheckInFullTimestamp("");  // Clear full timestamp when unchecked
                }
            }
            table.refresh();
            updateChart(chart, datePicker.getValue());
            AdminDashboard.updateDashboardData();

            loadStudents(datePicker.getValue());
            updateChart(chart, datePicker.getValue());
        });


       // Button viewReportsButton = new Button("View Reports");
       // viewReportsButton.setOnAction(e -> this.showAllReportsWindow());

        ComboBox<String> reasonDropdown = new ComboBox<>();
        reasonDropdown.getItems().addAll("All", "Permission", "Sick", "Unexcused", "Other...");
        reasonDropdown.setValue("All");
        reasonDropdown.setStyle("-fx-background-color: #FFCB3C;-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #222; -fx-background-radius: 28px;");
        reasonDropdown.setPrefHeight(50);
        reasonDropdown.setOnAction(e -> {
            String selected = reasonDropdown.getValue();
            filteredRecords.setPredicate(record -> {
                if ("All".equals(selected)) return true;
                if ("Other...".equals(selected)) return "Other...".equals(record.getReason());
                return selected.equals(record.getReason());
            });
        });

        // Add these two listeners to auto-show dropdown
        reasonDropdown.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                reasonDropdown.show();
            }
        });

        reasonDropdown.setOnMouseEntered(e -> reasonDropdown.show());


        Button clearFilter = new Button("Clear");
        clearFilter.setStyle("-fx-background-color: #FFCB3C;-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #222; -fx-background-radius: 28px;");
        clearFilter.setPrefHeight(50);
        clearFilter.setOnAction(e -> {
            reasonDropdown.setValue("All");
            filteredRecords.setPredicate(p -> true);
        });

     ComboBox<String> bulkReasonDropdown = new ComboBox<>();
        bulkReasonDropdown.getItems().addAll("Permission", "Sick", "Unexcused", "Other...");
        bulkReasonDropdown.setValue("Permission");
        bulkReasonDropdown.setStyle("-fx-background-color: #FFCB3C;-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #222; -fx-background-radius: 28px;");

       Button applyReasonBtn = new Button("Apply Reason");
        applyReasonBtn.setOnAction(e -> {
            String selectedReason = bulkReasonDropdown.getValue();
            for (AttendanceRecord record : filteredRecords) {
                if (!record.isPresent()) {
                    record.setReason(selectedReason);
                }
            }
            table.refresh();
        });

        header.getChildren().addAll(bulkReasonDropdown);
        header.getChildren().addAll(attendanceCheckbox, reasonDropdown, clearFilter);

   

        // ─── Table setup ────────────────────────────────────────────────────────────
        table = new TableView<>();
        table.setEditable(true);

        TableColumn<AttendanceRecord, String> nameCol = new TableColumn<>("Name");
        nameCol.setPrefWidth(80);
        nameCol.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #222;");
        nameCol.setCellValueFactory(data -> data.getValue().nameProperty());

        TableColumn<AttendanceRecord, Boolean> presentCol = new TableColumn<>("Manual In");
        presentCol.setPrefWidth(90);
        presentCol.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #222;");
        presentCol.setCellValueFactory(cellData -> cellData.getValue().presentProperty());
        presentCol.setCellFactory(col -> {
            CheckBoxTableCell<AttendanceRecord, Boolean> cell = new CheckBoxTableCell<>();

            cell.setSelectedStateCallback(index -> {
                AttendanceRecord record = table.getItems().get(index);
                return record.presentProperty();
            });

            cell.setOnMouseClicked(event -> {
                int index = cell.getIndex();
                if (index >= 0 && index < table.getItems().size()) {
                    AttendanceRecord record = table.getItems().get(index);
                    boolean isSelected = !record.isPresent();
                    record.setPresent(isSelected);

                    if (isSelected) {
                        LocalDateTime now = LocalDateTime.now();
                        record.setCheckInFullTimestamp(now.format(DB_TIMESTAMP_FORMAT));
                    } else {
                        record.setCheckInFullTimestamp("");
                    }


                    table.refresh();
                }
            });

            return cell;
        });
        presentCol.setEditable(true);

        TableColumn<AttendanceRecord, String> reasonCol = new TableColumn<>("Reason");
        reasonCol.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #222;");
        reasonCol.setMinWidth(100);
        reasonCol.setCellFactory(ComboBoxTableCell.forTableColumn("", "Permission", "Sick", "Unexcused", "Other..."));
      //reasonCol.setStyle("-fx-background-color: #FFCB3C;-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #222; -fx-background-radius: 28px;");

        reasonCol.setCellValueFactory(data -> data.getValue().reasonProperty());


        TableColumn<AttendanceRecord, String> inCol  = new TableColumn<>("Check-In");
        inCol.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #222;");
        inCol.setPrefWidth(90);
        inCol.setCellValueFactory(data -> data.getValue().checkInTimeProperty());
        inCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isEmpty()) {
                    setText("");
                } else {
                    try {
                        LocalDateTime dt = LocalDateTime.parse(item, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        setText(dt.format(DateTimeFormatter.ofPattern("hh:mm a")));
                    } catch (Exception e) {
                        setText(item);
                    }
                }
            }
        });

     

        
        TableColumn<AttendanceRecord, Boolean> manualCheckOutCol = new TableColumn<>("Manual Out");
        manualCheckOutCol.setMinWidth(90);
        manualCheckOutCol.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #222;");
        manualCheckOutCol.setCellValueFactory(cellData -> cellData.getValue().manualCheckOutProperty());
        manualCheckOutCol.setCellFactory(col -> {
            CheckBoxTableCell<AttendanceRecord, Boolean> cell = new CheckBoxTableCell<>();

            cell.setSelectedStateCallback(index -> {
                AttendanceRecord record = table.getItems().get(index);
                return record.manualCheckOutProperty();
            });

            cell.setOnMouseClicked(event -> {
                int index = cell.getIndex();
                if (index >= 0 && index < table.getItems().size()) {
                    AttendanceRecord record = table.getItems().get(index);
                    boolean isSelected = !record.isManualCheckOut();
                    record.setManualCheckOut(isSelected);

                    if (isSelected) {
                        LocalDateTime now = LocalDateTime.now();
                        record.setCheckOutFullTimestamp(now.format(DB_TIMESTAMP_FORMAT));
                    } else {
                        record.setCheckOutFullTimestamp("");
                    }


                    table.refresh();
                }
            });

            return cell;
        });
        manualCheckOutCol.setEditable(true);

        TableColumn<AttendanceRecord, String> outCol = new TableColumn<>("Check-Out");
        outCol.setPrefWidth(95);
        outCol.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #222;");
        outCol.setCellValueFactory(c -> c.getValue().checkOutTimeProperty());
        outCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isEmpty()) {
                    setText("");
                } else {
                    try {
                        LocalDateTime dateTime = LocalDateTime.parse(item, DB_TIMESTAMP_FORMAT);
                        setText(dateTime.format(DISPLAY_TIME_FORMAT));
                    } catch (Exception e) {
                        setText(item);  // fallback to raw string if parse fails
                    }
                }
            }
        });





        TableColumn<AttendanceRecord, Void> uploadCol = new TableColumn<>("Upload Letter");
        uploadCol.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #222;");
        uploadCol.setCellFactory(new Callback<>() {
            @Override
            public TableCell<AttendanceRecord, Void> call(final TableColumn<AttendanceRecord, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Upload");
                    {
                        btn.setOnAction(event -> {
                            AttendanceRecord record = getTableView().getItems().get(getIndex());
                            FileChooser fileChooser = new FileChooser();
                            File selectedFile = fileChooser.showOpenDialog(null);
                            if (selectedFile != null) {
                                record.setReasonLetterFile(selectedFile);
                            }
                        });
                    }
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : btn);
                    }
                };
            }
        });
        
        TableColumn<AttendanceRecord, Void> viewCol = new TableColumn<>("View Letter");
        viewCol.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #222;");
        viewCol.setCellFactory(new Callback<>() {
            @Override
            public TableCell<AttendanceRecord, Void> call(final TableColumn<AttendanceRecord, Void> param) {
                return new TableCell<>() {
                    private final Button viewBtn = new Button("View");

                    {
                        viewBtn.setOnAction(event -> {
                            AttendanceRecord record = getTableView().getItems().get(getIndex());
                            File reasonLetterFile = record.getReasonLetterFile();

                            if (reasonLetterFile != null && reasonLetterFile.exists()) {
                                try {
                                    Desktop.getDesktop().open(reasonLetterFile);
                                } catch (IOException ex) {
                                    new Alert(Alert.AlertType.ERROR, "Unable to open file: " + ex.getMessage()).showAndWait();
                                }
                            } else {
                                new Alert(Alert.AlertType.WARNING, "No reason letter available to view.").showAndWait();
                            }
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : viewBtn);
                    }
                };
            }
        });



        table.getColumns().addAll(
        	    nameCol, presentCol,inCol, outCol, manualCheckOutCol,reasonCol, uploadCol, viewCol
        	);
        table.setItems(filteredRecords);
        table.setEditable(true);
        table.setRowFactory(tv -> new TableRow<AttendanceRecord>() {
            @Override
            protected void updateItem(AttendanceRecord item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (item.isPresent()) {
                    setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32;"); // Soft green row, green text
                } else {
                    setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828;"); // Soft red row, red text
                }
            }
        });


        // ────────────────────────────────────────────────────────────────────────────

        Label chartTitle = new Label("Today's Attendance");
        chartTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button saveBtn = new Button("Save Attendance");
        saveBtn.setOnAction(e -> saveAttendance());
        saveBtn.setStyle("-fx-background-color: #FFCB3C;-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #222; -fx-background-radius: 28px;");
        saveBtn.setPrefHeight(50);

        chart = new PieChart();
        updateChart(chart, datePicker.getValue());

        Button refreshChart = new Button("Refresh Chart");
        refreshChart.setOnAction(e -> updateChart(chart, datePicker.getValue()));
        refreshChart.setStyle("-fx-background-color: #FFCB3C;-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #222; -fx-background-radius: 28px;");
        refreshChart.setPrefHeight(50);

        root.getChildren().addAll(header, table, saveBtn, chartTitle, chart, refreshChart);

        loadStudents(datePicker.getValue());
        updateChart(chart, datePicker.getValue());
        
        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(dashboardHeader);
        mainLayout.setCenter(root);
        
        this.root = new VBox();
        this.root.getChildren().add(mainLayout);
    }

    // ─── NEW: Revised loadStudents() with MIN/MAX ───────────────────────────────
 // Change method to accept date parameter
    private void loadStudents(LocalDate date) {
        masterRecords.clear();
        String sql = """
        	    SELECT c.child_id, c.name, a.check_in_time, a.check_out_time, a.is_present, a.reason_letter, a.manual_checkout
        	    FROM children c
        	    LEFT JOIN attendance_status a ON c.child_id = a.child_id AND a.date = ?
        	""";


        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(date));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                AttendanceRecord r = new AttendanceRecord(
                    rs.getInt("child_id"),
                    rs.getString("name")
                );
                r.setPresent(rs.getBoolean("is_present"));
                
                Timestamp checkInTs = rs.getTimestamp("check_in_time");
                r.setCheckInFullTimestamp(checkInTs != null ? checkInTs.toLocalDateTime().format(DB_TIMESTAMP_FORMAT) : "");

                Timestamp checkOutTs = rs.getTimestamp("check_out_time");
                r.setCheckOutFullTimestamp(checkOutTs != null ? checkOutTs.toLocalDateTime().format(DB_TIMESTAMP_FORMAT) : "");

                r.setManualCheckOut(rs.getBoolean("manual_checkout"));
                
                String reason = rs.getString("reason_letter");
                if ("Default".equals(reason)) reason = "";
                r.setReason(reason);

                masterRecords.add(r);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        table.refresh();
        updateChart(chart, date);
        AdminDashboard.updateDashboardData();
    }





    
    private void saveAttendance() {
        LocalDate selectedDate = datePicker.getValue();
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        DateTimeFormatter sqlFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (Connection conn = DatabaseConnection.getConnection()) {
            for (AttendanceRecord record : filteredRecords) {
            	String sql = "REPLACE INTO attendance_status (child_id, date, is_present, reason, check_in_time, check_out_time, reason_letter, manual_checkout) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);

                stmt.setInt(1, record.getChildId());
                stmt.setDate(2, java.sql.Date.valueOf(selectedDate));
                stmt.setBoolean(3, record.isPresent());
                stmt.setString(4, record.getReason());

                String checkInStr = record.getCheckInTime();
                System.out.println("Saving child: " + record.getChildId() + " checkIn: " + checkInStr);
                if (checkInStr != null && !checkInStr.isEmpty()) {
                    LocalTime time = LocalTime.parse(checkInStr, displayFormatter);
                    LocalDateTime dateTime = LocalDateTime.of(selectedDate, time);
                    String fullTimestamp = dateTime.format(sqlFormatter);
                    stmt.setTimestamp(5, java.sql.Timestamp.valueOf(fullTimestamp));
                } else {
                    stmt.setTimestamp(5, null);
                }

                String checkOutStr = record.getCheckOutTime();
                System.out.println("Saving child: " + record.getChildId() + " checkOut: " + checkOutStr);
                if (checkOutStr != null && !checkOutStr.isEmpty()) {
                    LocalTime time = LocalTime.parse(checkOutStr, displayFormatter);
                    LocalDateTime dateTime = LocalDateTime.of(selectedDate, time);
                    String fullTimestamp = dateTime.format(sqlFormatter);
                    stmt.setTimestamp(6, java.sql.Timestamp.valueOf(fullTimestamp));
                } else {
                    stmt.setTimestamp(6, null);
                }

                File reasonFile = record.getReasonLetterFile();
                if (reasonFile != null) {
                    stmt.setString(7, reasonFile.getAbsolutePath());
                } else {
                    stmt.setString(7, null);
                }
                stmt.setBoolean(8, record.isManualCheckOut());

                stmt.executeUpdate();
            }
            new Alert(Alert.AlertType.INFORMATION, "Attendance saved.").showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



   


    private void updateChart(PieChart chart, LocalDate date) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT 
                    SUM(CASE WHEN is_present THEN 1 ELSE 0 END) AS present,
                    SUM(CASE WHEN NOT is_present THEN 1 ELSE 0 END) AS absent
                FROM attendance_status 
                WHERE date = ?
                """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDate(1, java.sql.Date.valueOf(date));

            ResultSet rs = stmt.executeQuery();

            final int[] counts = new int[2]; // counts[0] = present, counts[1] = absent
            if (rs.next()) {
                counts[0] = rs.getObject("present") != null ? rs.getInt("present") : 0;
                counts[1] = rs.getObject("absent")  != null ? rs.getInt("absent")  : 0;
            }


            int total = counts[0] + counts[1];

            PieChart.Data presentData = new PieChart.Data(
                "Present (" + counts[0] + (total > 0 ? " | " + (counts[0] * 100 / total) + "%" : "") + ")", counts[0]
            );
            PieChart.Data absentData = new PieChart.Data(
                "Absent (" + counts[1] + (total > 0 ? " | " + (counts[1] * 100 / total) + "%" : "") + ")", counts[1]
            );

            chart.setData(FXCollections.observableArrayList(presentData, absentData));

            Platform.runLater(() -> {
                if (presentData.getNode() != null)
                    Tooltip.install(presentData.getNode(), new Tooltip(counts[0] + " students present"));
                if (absentData.getNode() != null)
                    Tooltip.install(absentData.getNode(), new Tooltip(counts[1] + " students absent"));
            });


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
                        record.setCheckInFullTimestamp(LocalDateTime.now().format(DB_TIMESTAMP_FORMAT));
                        found = true;
                        break;
                    }
                }

                // Reload attendance reasons and chart from database
                currentInstance.loadStudents(currentInstance.datePicker.getValue());   // ✅ refresh all data (so absent/present list updates)
                currentInstance.updateChart(currentInstance.chart, currentInstance.datePicker.getValue());
                currentInstance.table.refresh();
            }
        });
    }

 // ✅ NEW STATIC FUNCTION to update chart when NFC scanned
    public static void updateChartFromStatic() {
        Platform.runLater(() -> {
            if (currentInstance != null) {
                if (currentInstance.chart != null) {
                	currentInstance.updateChart(currentInstance.chart, currentInstance.datePicker.getValue());
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
            	currentInstance.loadStudents(currentInstance.datePicker.getValue());
                if (currentInstance.chart != null) {
                	currentInstance.updateChart(currentInstance.chart, currentInstance.datePicker.getValue());
                }
                currentInstance.table.refresh();
            }
        });
    }
 
    




    
    
    
    
}
