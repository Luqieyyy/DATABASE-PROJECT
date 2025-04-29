package nfc;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginView extends Application {

	private ImageView profileImageView;
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Taska Attendance System");

        //Profile Picture section
        profileImageView = new ImageView();
        profileImageView.setFitWidth(100);
        profileImageView.setFitHeight(100);
        profileImageView.setPreserveRatio(true);
        profileImageView.setStyle("-fx-border-color: #ccc; -fx-border-width: 2px;");
        setDefaultProfileImage();
       
        
        // Title
        Label title = new Label("Taska Attendance System\n   \t\tAdmin");
        title.setStyle("-fx-font-size: 20px; -fx-text-fill: black;");
        
  
        // Username Input
        Label usernameLabel = new Label("Username");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        usernameField.setPrefWidth(20);

        // Password Input
        Label passwordLabel = new Label("Password");
        PasswordField passwordField = new PasswordField(); // fixed here
        passwordField.setPromptText("Enter password");
        passwordField.setPrefWidth(20);

        //HBox
        VBox inputBox = new VBox(20);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setMaxWidth(250); 
        
        //Create VBox 
        VBox usernameBox = new VBox (5,usernameLabel, usernameField);
        VBox passwordBox = new VBox (5,passwordLabel, passwordField);
        inputBox.getChildren().addAll(usernameBox, passwordBox);
        
        // Message label
        Label messageLabel = new Label();

        // Login Button
        Button loginButton = new Button("Login");
        loginButton.setDefaultButton(true);
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
                
                    System.out.println("🧠 AdminModel.getName(): " + AdminModel.getName());
                    System.out.println("🧠 AdminModel.getProfilePicture(): " + AdminModel.getProfilePicture());
                    
                    // Load dashboard
                    AdminDashboard dashboard = new AdminDashboard();
                    Stage dashboardStage = new Stage();
                    dashboard.start(dashboardStage);

                    // Close login window
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

        // Layout
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.getStyleClass().add("layout-setStyle");
        layout.getChildren().addAll(
        		profileImageView,
                title,
                inputBox,
                loginButton,
                messageLabel,
                forgotPassword
        );

        Scene scene = new Scene(layout, 400, 450);
        scene.getStylesheets().add(getClass().getResource("/nfc/style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    private void setDefaultProfileImage() {
        var imageUrl = getClass().getResource("/nfc/user.jpg");
        if (imageUrl == null) {
            System.out.println("❌ Image not found!");
            return;
        } else {
            System.out.println("✅ Image found at: " + imageUrl);
        }
        Image defaultImage = new Image(imageUrl.toExternalForm());
        profileImageView.setImage(defaultImage);
    }


    public static void main(String[] args) {
        launch(args);

    }
}
