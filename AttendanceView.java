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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class AttendanceView {

    private VBox root;
    private TableView<AttendanceRecord> table;
    private ObservableList<AttendanceRecord> masterRecords = FXCollections.observableArrayList();
    private FilteredList<AttendanceRecord> filteredRecords = new FilteredList<>(masterRecords, p -> true);

    public AttendanceView() {
        root = new VBox(15);
        root.setPadding(new Insets(20));

        // Header bar
        HBox header = new HBox(10);
        header.setPadding(new Insets(10));
        header.setAlignment(Pos.CENTER_RIGHT);

        CheckBox attendanceCheckbox = new CheckBox("Mark All Present");
        attendanceCheckbox.setOnAction(e -> {
            for (AttendanceRecord record : filteredRecords) {
                record.setPresent(attendanceCheckbox.isSelected());
            }
            table.refresh();
        });

        Button viewReportsButton = new Button("View Reports");
        viewReportsButton.setOnAction(e -> showAllReportsWindow());


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
        
        ComboBox<String> bulkReasonDropdown = new ComboBox<>();
        bulkReasonDropdown.getItems().addAll("Permission", "Sick", "Unexcused", "Other...");
        bulkReasonDropdown.setValue("Permission");

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

        header.getChildren().addAll(bulkReasonDropdown, applyReasonBtn);


        header.getChildren().addAll(attendanceCheckbox, viewReportsButton, reasonDropdown, clearFilter);

        Label title = new Label("Attendance");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        title.setAlignment(Pos.CENTER_RIGHT);

        table = new TableView<>();

        TableColumn<AttendanceRecord, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> data.getValue().nameProperty());

        
        table = new TableView<>();
        table.setEditable(true);  
        TableColumn<AttendanceRecord, Boolean> presentCol = new TableColumn<>("Present");
        presentCol.setCellValueFactory(cellData -> cellData.getValue().presentProperty());
        presentCol.setCellFactory(CheckBoxTableCell.forTableColumn(presentCol));
        presentCol.setEditable(true); 
        
        TableColumn<AttendanceRecord, String> reasonCol = new TableColumn<>("Reason");
        reasonCol.setCellValueFactory(data -> data.getValue().reasonProperty());
        reasonCol.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<String> comboBox = new ComboBox<>();
            private final TextField textField = new TextField();

            {
                comboBox.getItems().addAll("Sick", "Permission", "Unexcused", "Other...");
                comboBox.setMaxWidth(Double.MAX_VALUE);
                textField.setPromptText("Enter reason...");
                textField.setMaxWidth(Double.MAX_VALUE);

                comboBox.setOnAction(e -> {
                    AttendanceRecord record = getTableView().getItems().get(getIndex());
                    String selected = comboBox.getValue();

                    if ("Other...".equals(selected)) {
                        record.setReason("Other...");
                        record.setCustomReason(textField.getText());
                        setGraphic(new VBox(comboBox, textField));
                    } else {
                        record.setReason(selected);
                        record.setCustomReason(""); // clear old input
                        setGraphic(comboBox);
                    }
                });

                textField.textProperty().addListener((obs, oldVal, newVal) -> {
                    AttendanceRecord record = getTableView().getItems().get(getIndex());
                    if ("Other...".equals(comboBox.getValue())) {
                        record.setCustomReason(newVal);
                    }
                });
            }
            

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    AttendanceRecord record = getTableView().getItems().get(getIndex());

                    // 🔒 Lock reason editing if marked present
                    if (record.isPresent()) {
                        comboBox.setDisable(true);
                        textField.setDisable(true);
                    } else {
                        comboBox.setDisable(false);
                        textField.setDisable(false);
                    }

                    comboBox.setValue(item);
                    if ("Other...".equals(item)) {
                        setGraphic(new VBox(comboBox, textField));
                    } else {
                        setGraphic(comboBox);
                    }
                }
            }

        });

        table.getColumns().addAll(nameCol, presentCol, reasonCol);
        table.setItems(filteredRecords);

        Label chartTitle = new Label("Today's Attendance");
        chartTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button saveBtn = new Button("Save Attendance");
        saveBtn.setOnAction(e -> saveAttendance());

        PieChart chart = new PieChart();
        chart.setTitle("Today's Attendance");
        updateChart(chart); // right after initializing the chart


        Button refreshChart = new Button("Refresh Chart");
        refreshChart.setOnAction(e -> updateChart(chart));

        root.getChildren().addAll(title, header, table, saveBtn, chartTitle, chart, refreshChart);

        loadStudents();
        updateChart(chart);
    }

    private void showAllReportsWindow() {
        Stage stage = new Stage();
        stage.setTitle("Today's Attendance Records");

        TableView<AttendanceRecord> table = new TableView<>();

        TableColumn<AttendanceRecord, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> data.getValue().nameProperty());

        TableColumn<AttendanceRecord, Boolean> presentCol = new TableColumn<>("Present");
        presentCol.setCellValueFactory(data -> data.getValue().presentProperty());

        TableColumn<AttendanceRecord, String> reasonCol = new TableColumn<>("Reason");
        reasonCol.setCellValueFactory(data -> data.getValue().reasonProperty());
        
        TableColumn<AttendanceRecord, Void> reportCol = new TableColumn<>("");
        reportCol.setPrefWidth(40);
        reportCol.setCellFactory(col -> new TableCell<>() {
        	private final Button reportBtn = new Button ("📋");
        	
        	{
        		reportBtn.setOnAction(e -> {
        			AttendanceRecord record = getTableView().getItems().get(getIndex());
        			showStudentReport(record);
        		});
        		 reportBtn.setStyle("-fx-background-color: transparent;");
        	}
        	protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : reportBtn);
        	}
        });

        table.getColumns().addAll(reportCol,nameCol, presentCol, reasonCol);

        ObservableList<AttendanceRecord> reportData = FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnection.getConnection()) {
        	String sql = """
        		    SELECT c.name, 
        		           MAX(a.is_present) AS is_present, 
        		           GROUP_CONCAT(DISTINCT a.reason SEPARATOR ', ') AS reason
        		    FROM attendance_status a
        		    JOIN children c ON a.child_id = c.id
        		    WHERE a.date = CURDATE()
        		    GROUP BY c.name
        		""";

            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("name");
                boolean present = rs.getBoolean("is_present");
                String reason = rs.getString("reason");

                AttendanceRecord record = new AttendanceRecord(0, name);
                record.setPresent(present);
                record.setReason(reason);

                reportData.add(record);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        table.setItems(reportData);

        VBox layout = new VBox(10, new Label("Today's Attendance Report"), table);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 400, 400);
        stage.setScene(scene);
        stage.show();
    }

    private void loadStudents() {
        masterRecords.clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, name FROM children";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                masterRecords.add(new AttendanceRecord(rs.getInt("id"), rs.getString("name")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveAttendance() {
        LocalDate today = LocalDate.now();
        try (Connection conn = DatabaseConnection.getConnection()) {
            for (AttendanceRecord record : filteredRecords) {
                String sql = "REPLACE INTO attendance_status (child_id, date, is_present, reason) VALUES (?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, record.getChildId());
                stmt.setDate(2, java.sql.Date.valueOf(today));
                stmt.setBoolean(3, record.isPresent());
                stmt.setString(4, record.getReason());
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
        		    SELECT final_status, COUNT(*) FROM (
        		        SELECT c.id, MAX(a.is_present) AS final_status
        		        FROM attendance_status a
        		        JOIN children c ON a.child_id = c.id
        		        WHERE a.date = CURDATE()
        		        GROUP BY c.id
        		    ) AS subquery
        		    GROUP BY final_status
        		""";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            int present = 0, absent = 0;
            while (rs.next()) {
            	boolean isPresent = rs.getBoolean("final_status");  // ✅ NEW!
                int count = rs.getInt(2);
                if (isPresent) present = count;
                else absent = count;
            }
            
            int total = present + absent;

            // Prepare data with percentage in label
            PieChart.Data presentData = new PieChart.Data(
                "Present (" + present + (total > 0 ? " | " + (present * 100 / total) + "%" : "") + ")", present
            );
            PieChart.Data absentData = new PieChart.Data(
                "Absent (" + absent + (total > 0 ? " | " + (absent * 100 / total) + "%" : "") + ")", absent
            );
            chart.setData(FXCollections.observableArrayList(presentData, absentData));

            // Add tooltips for each slice
            Tooltip presentTip = new Tooltip(present + " students present");
            Tooltip absentTip = new Tooltip(absent + " students absent");

            Tooltip.install(presentData.getNode(), presentTip);
            Tooltip.install(absentData.getNode(), absentTip);

            // Optional: Smooth resizing
            VBox.setVgrow(chart, Priority.ALWAYS);
            chart.setMinHeight(200);
      

        } catch (Exception e) {
            e.printStackTrace();
        }
        VBox.setVgrow(chart, Priority.ALWAYS);
        chart.setMinHeight(200); // Optional: force height

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
    
    
    
    
}
