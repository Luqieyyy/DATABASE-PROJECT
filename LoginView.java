package nfc;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginView extends Application {

    private ImageView profileImageView;
    

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Taska Attendance System");

        // ── 1) Use an AnchorPane as the root for absolute positioning ─────────
        AnchorPane root = new AnchorPane();
        root.setPadding(new Insets(20));
        root.setStyle(
          "-fx-background-color: linear-gradient(to bottom right, #FFEB3B 0%, #FFC107 100%);"
        );

        // ── 2) Header titles ───────────────────────────────────────────────
        Label mainTitle = new Label("BEE CALIPH \n ATTENDANCE SYSTEM ");
        mainTitle.setFont(Font.font("Impact", FontWeight.BLACK, 40));
        mainTitle.setTextFill(Color.web("#333"));
        mainTitle.setTextAlignment(TextAlignment.CENTER);

        Label subTitle = new Label("-Buzzing into bright future-");
        subTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        subTitle.setTextFill(Color.web("#555"));

        VBox headerBox = new VBox(5, mainTitle, subTitle);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(20, 0, 30, 0));

        // anchor headerBox: 20px from top, center horizontally
        AnchorPane.setTopAnchor(headerBox, 10.0);
        AnchorPane.setLeftAnchor(headerBox, 0.0);
        AnchorPane.setRightAnchor(headerBox, 0.0);


        // ── 3) Profile image ───────────────────────────────────────────────
        profileImageView = new ImageView();
        setDefaultProfileImage();
        profileImageView.setFitWidth(200);
        profileImageView.setPreserveRatio(true);
        profileImageView.setSmooth(true);

        // anchor profileImageView: 100px from top, center horizontally
        AnchorPane.setTopAnchor(profileImageView, 150.0);
        AnchorPane.setLeftAnchor(profileImageView, 380.0);
        AnchorPane.setRightAnchor(profileImageView, 200.0);


        // ── 4) White “card” container ───────────────────────────────────────
        VBox card = new VBox(20);
        card.setPadding(new Insets(40));
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(260);
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 20px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 5);"
        );

        // Input fields inside card
        Label userLabel = new Label("Username");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        usernameField.setFont(Font.font("Classic", FontWeight.BOLD,20));
        usernameField.setPrefWidth(50);
        usernameField.setMaxWidth(250);
        usernameField.setPrefHeight(40);
        usernameField.setMaxHeight(250);
        usernameField.getStyleClass().add("custom-input");

        

        Label passLabel = new Label("Password");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setPrefWidth(250);
        passwordField.setMaxWidth(250);
        passwordField.setPrefHeight(40);
        passwordField.setMaxHeight(250);
        passwordField.getStyleClass().add("custom-input");
        VBox inputs = new VBox(10, userLabel, usernameField, passLabel, passwordField);
        inputs.setAlignment(Pos.CENTER);

        // Message label
        Label messageLabel = new Label();
        messageLabel.setTextFill(Color.RED);

        // Login button
        Button loginBtn = new Button("Login");
        loginBtn.setFont(Font.font("System", FontWeight.BOLD, 16));
        loginBtn.setPrefWidth(200);
        loginBtn.setStyle(
            "-fx-background-color: #FFC107; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 20px;"
        );
        loginBtn.setDefaultButton(true);
        loginBtn.setOnAction(e -> {
            String user = usernameField.getText().trim();
            String pass = passwordField.getText().trim();
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "SELECT * FROM admin WHERE username=? AND password=?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, user);
                stmt.setString(2, pass);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    AdminModel.setName(rs.getString("name"));
                    AdminModel.setProfilePicture(rs.getString("profile_picture"));
                    new AdminDashboard().start(new Stage());
                    primaryStage.close();
                } else {
                    messageLabel.setText("Invalid Credentials.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                messageLabel.setText("Database error.");
            }
        });

        // Forgot password link
        Label forgot = new Label("Forgot password?");
        forgot.setTextFill(Color.web("#777"));
        forgot.setUnderline(true);
        forgot.setOnMouseClicked(e -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION,
                "Please contact your system administrator."
            );
            a.setHeaderText(null);
            a.setTitle("Forgot Password");
            a.showAndWait();
        });

        card.getChildren().addAll(inputs, loginBtn, messageLabel, forgot);

        // anchor card: 250px from top, center horizontally
        AnchorPane.setTopAnchor(card, 300.0);
        AnchorPane.setLeftAnchor(card, 200.0);
        AnchorPane.setRightAnchor(card, 200.0);


        // ── 5) Add headerBox, profileImageView, and card to root ─────────
        root.getChildren().addAll(headerBox, profileImageView, card);


        // ── Sarang Kanan ───────────────
        ImageView sarangkanan = new ImageView(
            new Image(getClass().getResource("/nfc/sarangkanan.png").toExternalForm())
        );
        sarangkanan.setFitWidth(250);
        sarangkanan.setPreserveRatio(true);

        // absolute positioning:
        sarangkanan.setLayoutX(755);  // change this to move left/right
        sarangkanan.setLayoutY(0);   // change this to move up/down
        
        // ── Sarang Kanan ───────────────
        ImageView sarangkiri = new ImageView(
            new Image(getClass().getResource("/nfc/sarangkiri.png").toExternalForm())
        );
        sarangkiri.setFitWidth(250);
        sarangkiri.setPreserveRatio(true);

        // absolute positioning:
        sarangkiri.setLayoutX(0);  // change this to move left/right
        sarangkiri.setLayoutY(0);   // change this to move up/down
        
        // ── Gambar Bee pegang pencil ───────────────
        ImageView image1 = new ImageView(
            new Image(getClass().getResource("/nfc/image1.png").toExternalForm())
        );
        image1.setFitWidth(150);
        image1.setPreserveRatio(true);

        // absolute positioning:
        image1.setLayoutX(140);  // change this to move left/right
        image1.setLayoutY(190);   // change this to move up/down
      
        
        // ── mangga1 ───────────────
        ImageView mangga1 = new ImageView(
            new Image(getClass().getResource("/nfc/mangga1.png").toExternalForm())
        );
        mangga1.setFitWidth(40);
        mangga1.setPreserveRatio(true);

        // absolute positioning:
        mangga1.setLayoutX(585);  // change this to move left/right
        mangga1.setLayoutY(386);   // change this to move up/down

        // ── mangga1 ───────────────
        ImageView mangga2 = new ImageView(
            new Image(getClass().getResource("/nfc/mangga2.png").toExternalForm())
        );
        mangga2.setFitWidth(40);
        mangga2.setPreserveRatio(true);

        // absolute positioning:
        mangga2.setLayoutX(585);  // change this to move left/right
        mangga2.setLayoutY(466);   // change this to move up/down
        
        root.getChildren().addAll(sarangkanan,sarangkiri,image1,mangga1,mangga2);
        
        // ── 7) Show the scene ──────────────────────────────────────────────
        Scene scene = new Scene(root, 1000, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setDefaultProfileImage() {
        var url = getClass().getResource("/nfc/beecaliph.png");
        if (url != null) {
            profileImageView.setImage(new Image(url.toExternalForm()));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}