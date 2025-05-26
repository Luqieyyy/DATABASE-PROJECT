package nfc;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import nfc.ChildrenView;
import nfc.StaffManagementView;
import nfc.LoginView;
public class AdminDashboard extends Application {

    private static AdminDashboard instance;
    private Label mainContent;
    private static Label scannedToday;
    private static Label notScanned;
    private static ListView<String> liveIns, liveOuts;
    private static boolean dashboardReady = false;
    private StackPane contentPane;
    private static NFCReader reader;
    private static Thread nfcReaderThread;
    private double xOffset = 0;
    private double yOffset = 0;

    public static void main(String[] args) {
        launch(args);
    }

    public AdminDashboard() {
        instance = this;
    }

    public static boolean isDashboardReady() {
        return dashboardReady;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Admin Dashboard - Taska Attendance");

        VBox mainLayout = new VBox();
        mainLayout.setSpacing(0);
        mainLayout.setStyle(
        	    "-fx-background-color: linear-gradient(to bottom right, #fffde4 0%, #ffe29a 100%);" // cream to light yellow
        	);

        HBox topBar = createTopBar(primaryStage);
        topBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        topBar.setOnMouseDragged(event -> {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        HBox bodyLayout = createBodyLayout(primaryStage);

        mainLayout.getChildren().addAll(topBar, bodyLayout);
        VBox.setVgrow(bodyLayout, Priority.ALWAYS);

        Scene scene = new Scene(mainLayout, 1000, 800);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm()); 

        primaryStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> {
            System.out.println("👋 Closing application, releasing Serial Port...");
            if (reader != null) {
                reader.stopReading();  // ✅ This must stop the Thread!
            }
            System.exit(0);
        });




        primaryStage.show();

        startAutoRefresh();
        startNFCReader();
    }
    private void startAutoRefresh() {
        Timeline autoRefreshTimeline = new Timeline(
            new KeyFrame(Duration.seconds(5), e -> {
                if (scannedToday != null && notScanned != null && liveIns != null && liveOuts != null) {
                    updateStatistics();
                    updateLiveScans();
                }
            })
        );
        autoRefreshTimeline.setCycleCount(Animation.INDEFINITE);
        autoRefreshTimeline.play();
    }


    private HBox createTopBar(Stage primaryStage) {
        // 1) Application title on the left
        Label title = new Label("Taska Attendance System");
        title.setStyle("-fx-text-fill: black; -fx-font-size: 16px; -fx-font-weight: bold;");

        // 2) Spacer pushes window‐control buttons to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 3) Window buttons
        Button minimizeButton = new Button("-");
        Button maximizeButton = new Button("⬜");
        Button closeButton    = new Button("X");

        minimizeButton.getStyleClass().add("window-button");
        maximizeButton.getStyleClass().add("window-button");
        closeButton.getStyleClass().addAll("window-button", "close-button");

        minimizeButton.setOnAction(e -> primaryStage.setIconified(true));
        maximizeButton.setOnAction(e -> primaryStage.setMaximized(!primaryStage.isMaximized()));
        closeButton.setOnAction(e -> {
            System.out.println("👋 Closing application, releasing Serial Port...");
            if (reader != null) reader.stopReading();
            System.exit(0);
        });

        // 4) Assemble the top bar
        HBox topBar = new HBox(10, title, spacer, minimizeButton, maximizeButton, closeButton);
        topBar.setPadding(new Insets(5, 10, 5, 10));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: linear-gradient(to right, #ffe259, #ffa751);");

        return topBar;
    }


    private HBox createBodyLayout(Stage primaryStage) {
        HBox bodyLayout = new HBox();
        bodyLayout.setStyle("-fx-background-color: transparent;");

        VBox sidebar = createSidebar(primaryStage);
        contentPane = new StackPane();
        contentPane.setStyle("-fx-background-color: transparent;");

        loadDashboardContent(); //

        bodyLayout.getChildren().addAll(sidebar, contentPane);
        HBox.setHgrow(contentPane, Priority.ALWAYS);
        return bodyLayout;
    }

    private VBox createSidebar(Stage primaryStage) {
        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(10));
        sidebar.setPrefWidth(220);         // Set your desired fixed width
        sidebar.setMinWidth(220);          // Prevent shrinking
        sidebar.setMaxWidth(220); 
        sidebar.setAlignment(Pos.TOP_CENTER);
        sidebar.setStyle("-fx-background-color: #d3e3bd;"); // Soft green


        Image profileImage;
        try {
            profileImage = new Image(getClass().getResourceAsStream(AdminModel.getProfilePicture()));
            if (profileImage.isError()) throw new Exception("Image error");
        } catch (Exception e) {
            profileImage = new Image("https://via.placeholder.com/60");
        }

        ImageView profilePic = new ImageView(profileImage);
        profilePic.setFitWidth(100);
        profilePic.setFitHeight(130);
        

        Label nameLabel = new Label("" + AdminModel.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        ImageView beeIcon = new ImageView(new Image(getClass().getResource("/nfc/bee-icon.png").toExternalForm()));
        beeIcon.setFitWidth(24);
        beeIcon.setFitHeight(24);
        HBox welcomeBox = new HBox(6, nameLabel, beeIcon);
        welcomeBox.setAlignment(Pos.CENTER);
        
        VBox profileBox = new VBox(10, profilePic, welcomeBox);
        profileBox.setAlignment(Pos.CENTER);

        
        //Nav Buttons
        Button btnDashboard = createNavButton("Dashboard");
        btnDashboard.setStyle("-fx-background-color: #FFCB3C;-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #222; -fx-background-radius: 28px;");
        btnDashboard.setPrefHeight(50);
        Button btnAttendance = createNavButton("Attendance");
        btnAttendance.setStyle("-fx-background-color: #FFCB3C;-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #222; -fx-background-radius: 28px;");
        btnAttendance.setPrefHeight(50);
        Button btnChildren = createNavButton("Children & Parents");
        btnChildren.setStyle("-fx-background-color: #FFCB3C;-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #222; -fx-background-radius: 28px;");
        btnChildren.setPrefHeight(50);
        Button btnStaff = createNavButton("Staff Management");
        btnStaff.setStyle("-fx-background-color: #FFCB3C;-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #222; -fx-background-radius: 28px;");
        btnStaff.setPrefHeight(50);
        Button btnLogout = createNavButton("Logout");
        btnLogout.setStyle("-fx-background-color: #FFCB3C;-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #222; -fx-background-radius: 28px;");
        btnLogout.setPrefHeight(50);

        btnDashboard.setOnAction(e -> loadDashboardContent());
        btnAttendance.setOnAction(e -> {
            AttendanceView view = new AttendanceView();
            setMainContent(view.getRoot());
        });


        btnChildren.setOnAction(e -> {
            ChildrenView childrenPane = new ChildrenView();
            setMainContent(childrenPane);
        });

        btnStaff.setOnAction(e -> {
            StaffManagementView staffPane = new StaffManagementView();
            setMainContent(staffPane);
        });
        btnLogout.setOnAction(event -> {
            System.out.println("👋 Closing application, releasing Serial Port...");
            if (reader != null) {
                reader.stopReading();  // ✅ This must stop the Thread!
            }
            LoginView  loginView = new LoginView();
            Stage loginStage = new Stage();
            try {
            	loginView.start(loginStage);
            }catch(Exception ex) {
            ex.printStackTrace();
            }
        });




        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(profileBox, btnDashboard, btnAttendance, btnChildren, btnStaff, spacer, btnLogout);
        return sidebar;
    }

   /* private StackPane createContentPane() {
        mainContent = new Label("Welcome, " + AdminModel.getName());
        mainContent.setStyle("-fx-font-size: 20px; -fx-text-fill: #333;");

        VBox box = new VBox(mainContent);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));

        StackPane contentPane = new StackPane(box);
        contentPane.setStyle("-fx-background-color: #f5f5f5;");
        return contentPane;
    }*/

    private Button createNavButton(String title) {
        Button button = new Button(title);
        //button.getStyleClass().add("s");
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    private void setMainContent(Node node) {
        contentPane.getChildren().setAll(node);
    }

    private void startNFCReader() {
        if (reader != null) {
            reader.stopReading();
            if (nfcReaderThread != null && nfcReaderThread.isAlive()) {
                nfcReaderThread.interrupt();
                try {
                    nfcReaderThread.join();
                    Thread.sleep(1000); // 🧠 Add this small 1 second delay to fully release COM4
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        reader = new NFCReader("COM4");
        nfcReaderThread = new Thread(reader);
        nfcReaderThread.start();
    }



    private void loadDashboardContent() {
        // Dashboard Header Bar (full-width)
        HBox dashboardHeader = new HBox(18);
        dashboardHeader.setAlignment(Pos.CENTER_LEFT);
        dashboardHeader.setPrefHeight(70);
        dashboardHeader.setMaxWidth(Double.MAX_VALUE); // Stretch to parent
        dashboardHeader.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #FFD600 90%, #FFC107 100%);" +
            "-fx-border-color: #f4b400; -fx-border-width: 0 0 3 0;" +
            "-fx-background-image: repeating-linear-gradient(to bottom, transparent, transparent 12px, #FECF4D 12px, #FECF4D 15px);"
        );
        ImageView honeyPot = new ImageView(new Image(getClass().getResource("/nfc/hive2.png").toExternalForm()));
        honeyPot.setFitWidth(54);
        honeyPot.setFitHeight(54);
        Label dashboardTitle = new Label("DASHBOARD");
        dashboardTitle.setFont(Font.font("Impact", FontWeight.EXTRA_BOLD, 44));
        dashboardTitle.setTextFill(Color.web("#181818"));
        dashboardHeader.getChildren().addAll(honeyPot, dashboardTitle);

        // Dashboard main body content (center area)
        VBox dashboardBody = new VBox(20);
        dashboardBody.setPadding(new Insets(20));
        dashboardBody.setAlignment(Pos.TOP_LEFT);
        dashboardBody.setStyle("-fx-background-color: linear-gradient(to bottom right, #fffde4 0%, #ffe29a 100%);");

        // Date and time labels
        Label dateLabel = new Label();
        dateLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Label clockLabel = new Label();
        clockLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            clockLabel.setText("Time: " + java.time.LocalTime.now().withNano(0).toString());
            dateLabel.setText("Date: " + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy")));
        }));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        // Statistics (scanned today, not yet scanned)
        scannedToday = new Label();
        scannedToday.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 20px; -fx-background-radius: 50px;");
        notScanned = new Label();
        notScanned.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-padding: 20px; -fx-background-radius: 50px;");
        updateStatistics();

        HBox topRow = new HBox(50, scannedToday, notScanned);
        topRow.setAlignment(Pos.CENTER);

        // Live scan stats
        Label scanLabel = new Label("Live Scans:");
        scanLabel.setFont(Font.font("Impact", FontWeight.EXTRA_BOLD, 20));
        HBox statsBox = new HBox(30, scanLabel, new Region(), dateLabel, clockLabel);
        HBox.setHgrow(statsBox.getChildren().get(1), Priority.ALWAYS);
        statsBox.setAlignment(Pos.TOP_CENTER);

        // Live check-ins/outs
        liveIns  = new ListView<>();
        liveIns.setPrefHeight(300);
        liveIns.setPrefWidth(300);
        liveOuts = new ListView<>();
        liveOuts.setPrefHeight(300);
        liveOuts.setPrefWidth(300);
        updateLiveScans();

        VBox inBox  = new VBox(new Label("Check-Ins"), liveIns);
        inBox.setStyle("-fx-background-color: #FFC72C; -fx-background-radius: 18; -fx-padding: 18; -fx-effect: dropshadow(three-pass-box,rgba(0,0,0,0.06),4,0,0,2);");
        inBox.setMaxWidth(600);
        inBox.setMinHeight(200);
        inBox.setPrefHeight(200);
        inBox.setMaxHeight(200);

        VBox outBox = new VBox(new Label("Check-Outs"), liveOuts);
        outBox.setStyle("-fx-background-color: #FFC72C; -fx-background-radius: 18; -fx-padding: 18; -fx-effect: dropshadow(three-pass-box,rgba(0,0,0,0.06),4,0,0,2);");
        outBox.setMaxWidth(600);
        outBox.setMinHeight(200);
        outBox.setPrefHeight(200);
        outBox.setMaxHeight(200);

        HBox livePane = new HBox(50, inBox, outBox);
        livePane.setAlignment(Pos.CENTER);
        livePane.setMaxWidth(Double.MAX_VALUE);

      
        Label announcementTitle = new Label("Announcements");
        announcementTitle.setFont(Font.font("Impact", FontWeight.EXTRA_BOLD, 20));
        announcementTitle.setAlignment(Pos.CENTER_LEFT);
        announcementTitle.setMaxWidth(Double.MAX_VALUE);

        // Bee icon (use your real path)
        ImageView beeIcon = new ImageView(new Image(getClass().getResource("/nfc/bee-speaker.png").toExternalForm()));
        beeIcon.setFitWidth(64);
        beeIcon.setFitHeight(64);

        // Announcement text label
        Label announcementText = new Label(
                "Dear Bee Caliph Team,\n" +
                "Here’s what’s coming up this week:\n" +
                "• Monday: Staff meeting at 8:00 AM in the Teachers’ Lounge (Room 2)\n" +
                "Please check the Staff Docs section for updated duty rosters and activity guides."
        );
        announcementText.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        announcementText.setStyle("-fx-text-fill: #181818;");
        announcementText.setWrapText(true);

        // Green rounded background HBox
        HBox announcementBox = new HBox(18, beeIcon, announcementText);
        announcementBox.setPadding(new Insets(24, 24, 24, 24));
        announcementBox.setBackground(new Background(new BackgroundFill(
                Color.web("#A2DB8E"), new CornerRadii(18), Insets.EMPTY
        )));
        announcementBox.setMaxWidth(Double.MAX_VALUE);
        announcementBox.setAlignment(Pos.CENTER_LEFT);

        VBox announcementArea = new VBox(10, announcementTitle, announcementBox);
        announcementArea.setPadding(new Insets(10, 0, 0, 0));


        // Flexible spacer for visual balance
        Region flexibleSpacer = new Region();
        VBox.setVgrow(flexibleSpacer, Priority.ALWAYS);

        // Add all dashboard body content (not the header!)
        dashboardBody.getChildren().addAll(
            topRow,
            statsBox,
            new Separator(),
            livePane,
            new Separator(),
            flexibleSpacer,
            announcementArea
        );

        // Final layout: header at top, dashboardBody in center
        BorderPane dashboardLayout = new BorderPane();
        dashboardLayout.setTop(dashboardHeader);
        dashboardLayout.setCenter(dashboardBody);

        setMainContent(dashboardLayout);
        dashboardReady = true;
    }


    public static void updateStatistics() {
        Platform.runLater(() -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String scannedQuery = "SELECT COUNT(*) FROM attendance WHERE DATE(scan_time) = CURDATE()";
                PreparedStatement stmt1 = conn.prepareStatement(scannedQuery);
                ResultSet rs1 = stmt1.executeQuery();
                if (rs1.next()) scannedToday.setText("Scanned Today: " + rs1.getInt(1));

                String notScannedQuery = "SELECT COUNT(*) FROM children WHERE id NOT IN (SELECT child_id FROM attendance WHERE DATE(scan_time) = CURDATE())";
                PreparedStatement stmt2 = conn.prepareStatement(notScannedQuery);
                ResultSet rs2 = stmt2.executeQuery();
                if (rs2.next()) notScanned.setText("Not Yet Scanned: " + rs2.getInt(1));

            } catch (Exception e) {
                scannedToday.setText("Scanned Today: N/A");
                notScanned.setText("Not Yet Scanned: N/A");
            }
        });
    }

    public static void updateLiveScans() {
    	  Platform.runLater(() -> {
    	    try (Connection conn = DatabaseConnection.getConnection()) {
    	      // 1) Check-Ins
    	      String inSql = """
    	        SELECT TIME(a.scan_time) AS tm, c.name
    	          FROM attendance a
    	          JOIN children  c ON a.child_id = c.id
    	         WHERE DATE(a.scan_time)=CURDATE() AND a.scan_type='IN'
    	         ORDER BY a.scan_time DESC
    	      """;
    	      ObservableList<String> ins = FXCollections.observableArrayList();
    	      try (ResultSet rs = conn.createStatement().executeQuery(inSql)) {
    	        while (rs.next()) {
    	          ins.add(rs.getString("tm") + " – " + rs.getString("name"));
    	        }
    	      }
    	      liveIns.setItems(ins);

    	      // 2) Check-Outs
    	      String outSql = """
    	        SELECT TIME(a.scan_time) AS tm, c.name
    	          FROM attendance a
    	          JOIN children  c ON a.child_id = c.id
    	         WHERE DATE(a.scan_time)=CURDATE() AND a.scan_type='OUT'
    	         ORDER BY a.scan_time DESC
    	      """;
    	      ObservableList<String> outs = FXCollections.observableArrayList();
    	      try (ResultSet rs = conn.createStatement().executeQuery(outSql)) {
    	        while (rs.next()) {
    	          outs.add(rs.getString("tm") + " – " + rs.getString("name"));
    	        }
    	      }
    	      liveOuts.setItems(outs);

    	    } catch (Exception ex) {
    	      liveIns .getItems().setAll("Error loading");
    	      liveOuts.getItems().setAll("Error loading");
    	      ex.printStackTrace();
    	    }
    	  });
    	}


 // ✅ Add this
    public static void updateDashboardData() {
        Platform.runLater(() -> {
            if (instance != null) {
                updateStatistics();
                updateLiveScans();
            }
        });
    }
    
    public static void showRegisterForm(String tagId) {
        Stage stage = new Stage();
        stage.setTitle("Register New Child");

        Label nameLabel = new Label("Child's Name:");
        TextField nameField = new TextField();

        Label parentContactLabel = new Label("Parent Contact:");
        TextField parentContactField = new TextField();

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String parentContact = parentContactField.getText().trim();

            if (name.isEmpty() || parentContact.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please fill all fields.");
                alert.showAndWait();
            } else {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String sql = "INSERT INTO children (name, parent_contact, nfc_uid) VALUES (?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, name);
                    stmt.setString(2, parentContact);
                    stmt.setString(3, tagId);
                    stmt.executeUpdate();

                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Child registered successfully!");
                    alert.showAndWait();
                    stage.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        VBox layout = new VBox(10, nameLabel, nameField, parentContactLabel, parentContactField, saveButton);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 300, 250);
        stage.setScene(scene);
        stage.show();
    }

    //UNTUK CHECK IN CHECK OUT
    public static void handleNfcAttendance(String nfcUid) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        try (Connection conn = DatabaseConnection.getConnection()) {
            // 1. Find child by NFC
            String childSql = "SELECT id, name FROM children WHERE nfc_uid = ?";
            PreparedStatement childStmt = conn.prepareStatement(childSql);
            childStmt.setString(1, nfcUid);
            ResultSet childRs = childStmt.executeQuery();

            if (!childRs.next()) {
                // Card not registered
                Platform.runLater(() -> showAlert("This card is not registered!", Alert.AlertType.WARNING));
                return;
            }

            int childId = childRs.getInt("id");
            String childName = childRs.getString("name");

            // 2. Get today's attendance for this child
            String attSql = "SELECT scan_time, scan_type FROM attendance WHERE child_id = ? AND DATE(scan_time) = ?";
            PreparedStatement attStmt = conn.prepareStatement(attSql);
            attStmt.setInt(1, childId);
            attStmt.setDate(2, java.sql.Date.valueOf(today));
            ResultSet attRs = attStmt.executeQuery();

            LocalDateTime checkInTime = null;
            boolean checkedOut = false;

            while (attRs.next()) {
                String scanType = attRs.getString("scan_type");
                if ("IN".equals(scanType)) {
                    checkInTime = attRs.getTimestamp("scan_time").toLocalDateTime();
                }
                if ("OUT".equals(scanType)) {
                    checkedOut = true;
                }
            }

            if (checkInTime == null) {
                // No check-in today: do check-in
                recordAttendance(childId, "IN");
                Platform.runLater(() -> showAlert("Check-in successful for " + childName, Alert.AlertType.INFORMATION));
            } else if (!checkedOut) {
                // Already checked in, check if enough time passed for check-out
                long hoursBetween = java.time.Duration.between(checkInTime, LocalDateTime.now()).toHours();
                if (hoursBetween < 8) {
                    Platform.runLater(() -> showAlert("Cannot check out yet. Minimum 8 hours between check-in and check-out.", Alert.AlertType.WARNING));
                } else {
                    recordAttendance(childId, "OUT");
                    Platform.runLater(() -> showAlert("Check-out successful for " + childName, Alert.AlertType.INFORMATION));
                }
            } else {
                // Already checked out today
                Platform.runLater(() -> showAlert("Already checked out today for " + childName, Alert.AlertType.WARNING));
            }

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> showAlert("Database error: " + e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    // Helper method to record attendance
    private static void recordAttendance(int childId, String type) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO attendance (child_id, scan_time, scan_type) VALUES (?, NOW(), ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, childId);
            stmt.setString(2, type);
            stmt.executeUpdate();
        }
    }

    // Helper to show alerts on UI
    private static void showAlert(String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Error" : "Info");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }


    // ✅ Add this
    /*public static void promptRegisterCard(String tagId) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Unknown Card Detected");
            alert.setHeaderText(null);
            alert.setContentText("This card is not registered.\nWould you like to register it now?");

            ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
            ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);

            alert.getButtonTypes().setAll(yesButton, noButton);

            alert.showAndWait().ifPresent(response -> {
                if (response == yesButton) {
                	showRegisterForm(tagId);  // No need to write AdminDashboard again
   // ✅ correct
                }
            });

        });
    }*/    

}
