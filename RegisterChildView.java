package nfc;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;
import java.sql.Connection;
import java.sql.PreparedStatement;


public class RegisterChildView {

    private String tagId;

    public RegisterChildView(String tagId) {
        this.tagId = tagId;
        showForm();
    }

    private void showForm() {
        Stage stage = new Stage();
        stage.setTitle("Register New Child");

        Label label = new Label("Register New Child for Card: " + tagId);
        label.setStyle("-fx-font-weight: bold;");

        TextField nameField = new TextField();
        nameField.setPromptText("Enter Child's Name");

        Button saveButton = new Button("Save");
        Label messageLabel = new Label();

        saveButton.setOnAction(e -> {
            String childName = nameField.getText().trim();

            if (childName.isEmpty()) {
                messageLabel.setText("❌ Please enter child's name.");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO children (name, nfc_uid) VALUES (?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, childName);
                stmt.setString(2, tagId);
                stmt.executeUpdate();

                messageLabel.setText("✅ Child registered successfully!");

                // ✅ Close registration window
                ((Stage) saveButton.getScene().getWindow()).close();

            } catch (Exception ex) {
                ex.printStackTrace();
                messageLabel.setText("❌ Database error.");
            }
        });


        VBox layout = new VBox(10, label, nameField, saveButton, messageLabel);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 350, 250);
        stage.setScene(scene);
        stage.show();
    }
}
