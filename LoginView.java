package nfc;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginView extends Application {

    private ImageView profileImageView;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Taska Attendance System");

        // Profile Picture section
        profileImageView = new ImageView();
        profileImageView.setFitWidth(100);
        profileImageView.setFitHeight(100);
        profileImageView.setPreserveRatio(true);
        profileImageView.setStyle("-fx-border-color: #ccc; -fx-border-width: 2px;");
        setDefaultProfileImage();

        // 1) Build header (gradient bar + titles)
        Label mainTitle = new Label("WELCOME TO TASKA ATTENDANCE SYSTEM!");
        mainTitle.getStyleClass().add("header-title");

        Label subTitle = new Label("Children are our most valuable resource.");
        subTitle.getStyleClass().add("header-subtitle");

        VBox titleBox = new VBox(5, mainTitle, subTitle);
        titleBox.setAlignment(Pos.CENTER);

        HBox headerBar = new HBox(titleBox);
        headerBar.getStyleClass().add("header-bar");
        headerBar.setMaxWidth(Double.MAX_VALUE);

        // 2) Username Input
        Label usernameLabel = new Label("Username");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        usernameField.setPrefWidth(20);

        // Password Input
        Label passwordLabel = new Label("Password");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setPrefWidth(20);

        VBox usernameBox = new VBox(5, usernameLabel, usernameField);
        VBox passwordBox = new VBox(5, passwordLabel, passwordField);

        VBox inputBox = new VBox(20, usernameBox, passwordBox);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setMaxWidth(250);

        // Message label
        Label messageLabel = new Label();

        // Login Button
        Button loginButton = new Button("Login");
        loginButton.setDefaultButton(true);
        loginButton.getStyleClass().add("button-birulawa");
        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "SELECT * FROM admin WHERE username=? AND password=?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, username);
                stmt.setString(2, password);

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    AdminModel.setName(rs.getString("name"));
                    AdminModel.setProfilePicture(rs.getString("profile_picture"));
                    messageLabel.setText("Login Successful!");

                    AdminDashboard dashboard = new AdminDashboard();
                    Stage dashboardStage = new Stage();
                    dashboard.start(dashboardStage);

                    ((Stage) loginButton.getScene().getWindow()).close();
                } else {
                    messageLabel.setText("Invalid Credentials.");
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                messageLabel.setText("Database error.");
            }
        });

        // Forgot Password Text
        Text forgotPassword = new Text("Forgot password?");
        forgotPassword.setStyle("-fx-underline: true; -fx-cursor: hand;");
        forgotPassword.setOnMouseClicked(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Forgot Password");
            alert.setHeaderText(null);
            alert.setContentText("Please contact your system administrator.");
            alert.showAndWait();
        });

        // Assemble root layout
        VBox root = new VBox(20,
            headerBar,
            profileImageView,
            inputBox,
            loginButton,
            messageLabel,
            forgotPassword
        );
     // remove the old “layout-setStyle” entirely…
        root.getStyleClass().removeAll("layout-setStyle");
        // …and add our new gradient class
        root.getStyleClass().add("layout-setStyle");

        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 1000, 800);
        scene.getStylesheets().add(getClass().getResource("/nfc/style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setDefaultProfileImage() {
        var imageUrl = getClass().getResource("/nfc/children.jpg");
        if (imageUrl == null) {
            System.out.println("❌ Image not found!");
            return;
        }
        Image defaultImage = new Image(imageUrl.toExternalForm());
        profileImageView.setImage(defaultImage);

        // 👉 Tweak these two values until it’s the size you like:
        profileImageView.setFitWidth(200);
        profileImageView.setFitHeight(200);

        // Keep the picture from stretching if its aspect ratio differs
        profileImageView.setPreserveRatio(true);
        profileImageView.setSmooth(true);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
