package nfc;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdminDashboard extends Application {

    private static AdminDashboard instance;
    private Label mainContent;
    private static Label scannedToday;
    private static Label notScanned;
    private static ListView<String> liveScans;
    private static boolean dashboardReady = false;
    private StackPane contentPane;
    private static NFCReader reader;
    private static Thread nfcReaderThread;

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
        mainLayout.setStyle("-fx-background-color: linear-gradient(to bottom right, #00c6ff, #0072ff);");

        HBox topBar = createTopBar(primaryStage);
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
                if (scannedToday != null && notScanned != null && liveScans != null) {
                    updateStatistics();
                    updateLiveScans();
                }
            })
        );
        autoRefreshTimeline.setCycleCount(Animation.INDEFINITE);
        autoRefreshTimeline.play();
    }


    private HBox createTopBar(Stage primaryStage) {
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(5, 10, 10, 10));
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setStyle("-fx-background-color: transparent;");

        Button minimizeButton = new Button("-");
        Button maximizeButton = new Button("⬜");
        Button closeButton = new Button("X");

        minimizeButton.getStyleClass().add("window-button");
        maximizeButton.getStyleClass().add("window-button");
        closeButton.getStyleClass().addAll("window-button", "close-button");

        minimizeButton.setOnAction(e -> primaryStage.setIconified(true));
        maximizeButton.setOnAction(e -> primaryStage.setMaximized(!primaryStage.isMaximized()));
        closeButton.setOnAction(event -> {
            System.out.println("👋 Closing application, releasing Serial Port...");
            if (reader != null) {
                reader.stopReading();  // ✅ This must stop the Thread!
            }
            System.exit(0);
        });

        topBar.getChildren().addAll(minimizeButton, maximizeButton, closeButton);
        return topBar;
    }

    private HBox createBodyLayout(Stage primaryStage) {
        HBox bodyLayout = new HBox();
        bodyLayout.getStyleClass().add("button-birulawa1");

        VBox sidebar = createSidebar(primaryStage);
        contentPane = createContentPane();

        bodyLayout.getChildren().addAll(sidebar, contentPane);
        HBox.setHgrow(contentPane, Priority.ALWAYS);
        return bodyLayout;
    }

    private VBox createSidebar(Stage primaryStage) {
        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(10));
        sidebar.setPrefWidth(190);
        sidebar.setAlignment(Pos.TOP_CENTER);
        sidebar.getStyleClass().add("navigatesidebar");

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

        Label nameLabel = new Label("Welcome, " + AdminModel.getName());
        VBox profileBox = new VBox(10, profilePic, nameLabel);
        profileBox.setAlignment(Pos.CENTER);

        Button btnDashboard = createNavButton("Dashboard");
        Button btnAttendance = createNavButton("Attendance");
        Button btnChildren = createNavButton("Children & Parents");
        Button btnStaff = createNavButton("Staff Management");
        Button btnLogout = createNavButton("Logout");
        btnLogout.getStyleClass().add(".buttonlogout1");

        btnDashboard.setOnAction(e -> loadDashboardContent());
        btnAttendance.setOnAction(e -> setMainContent(new Label("📅 Attendance Management")));
        btnChildren.setOnAction(e -> setMainContent(new Label("🧒 Children & Parents Section")));
        btnStaff.setOnAction(e -> setMainContent(new Label("👩‍🏫 Staff Management Section")));
        btnLogout.setOnAction(event -> {
            System.out.println("👋 Closing application, releasing Serial Port...");
            if (reader != null) {
                reader.stopReading();  // ✅ This must stop the Thread!
            }
            System.exit(0);
        });




        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(profileBox, btnDashboard, btnAttendance, btnChildren, btnStaff, spacer, btnLogout);
        return sidebar;
    }

    private StackPane createContentPane() {
        mainContent = new Label("Welcome, " + AdminModel.getName());
        mainContent.setStyle("-fx-font-size: 20px; -fx-text-fill: #333;");

        VBox box = new VBox(mainContent);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));

        StackPane contentPane = new StackPane(box);
        contentPane.setStyle("-fx-background-color: #f5f5f5;");
        return contentPane;
    }

    private Button createNavButton(String title) {
        Button button = new Button(title);
        button.getStyleClass().add("button-birulawa");
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
        Label title = new Label("Taska Attendance System");
        title.setStyle("-fx-font-size: 30px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 20px; -fx-background-radius: 50px;");
        HBox titleBox = new HBox(title);
        titleBox.setAlignment(Pos.CENTER);

        VBox dashboardContent = new VBox(20);
        dashboardContent.setPadding(new Insets(20));
        dashboardContent.setAlignment(Pos.TOP_LEFT);
        dashboardContent.getStyleClass().add("button-birulawa1");

        Label clockLabel = new Label();
        clockLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            clockLabel.setText("Time: " + java.time.LocalTime.now().withNano(0).toString());
        }));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        scannedToday = new Label();
        scannedToday.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 20px; -fx-background-radius: 50px;");

        notScanned = new Label();
        notScanned.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-padding: 20px; -fx-background-radius: 50px;");

        updateStatistics();

        HBox topRow = new HBox(50, scannedToday, notScanned);
        topRow.setAlignment(Pos.CENTER);

        Label scanLabel = new Label("Live Scans:");
        HBox statsBox = new HBox(0, scanLabel, new Region(), clockLabel);
        HBox.setHgrow(statsBox.getChildren().get(1), Priority.ALWAYS);
        statsBox.setAlignment(Pos.TOP_CENTER);

        liveScans = new ListView<>();
        liveScans.setPrefHeight(100);
        updateLiveScans();

        Label announcementTitle = new Label("Announcements");
        announcementTitle.setAlignment(Pos.CENTER);
        announcementTitle.setMaxWidth(Double.MAX_VALUE);

        TextArea announcementBox = new TextArea();
        announcementBox.setPromptText("HARINI KITA MAKAN NASI AYAM");
        announcementBox.setWrapText(true);
        announcementBox.setPrefHeight(200);

        VBox announcementArea = new VBox(5, announcementTitle, announcementBox);

        dashboardContent.getChildren().addAll(
            titleBox,
            topRow,
            statsBox,
            new Separator(),
            liveScans,
            new Separator(),
            announcementArea
        );

        setMainContent(dashboardContent);
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
                String query = "SELECT TIME(scan_time), name FROM attendance JOIN children ON attendance.child_id = children.id WHERE DATE(scan_time) = CURDATE() ORDER BY scan_time DESC";
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery();

                ObservableList<String> newList = javafx.collections.FXCollections.observableArrayList();
                while (rs.next()) newList.add(rs.getString(1) + " - " + rs.getString(2));

                if (!liveScans.getItems().equals(newList)) {
                    liveScans.setItems(newList);
                }
            } catch (Exception e) {
                liveScans.getItems().add("Unable to load scan data.");
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



    // ✅ Add this
    public static void promptRegisterCard(String tagId) {
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
    }

}
