package nfc;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.*;

public class StaffManagementView extends VBox {

    private final TableView<Admin> table = new TableView<>();
    private final ObservableList<Admin> data = FXCollections.observableArrayList();

    public StaffManagementView() {
        // Header bar
        HBox headerBar = new HBox(18);
        headerBar.setAlignment(Pos.CENTER_LEFT);
        headerBar.setPrefHeight(70);
        headerBar.setMaxWidth(Double.MAX_VALUE);
        headerBar.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #FFD600 90%, #FFC107 100%);" +
            "-fx-border-color: #f4b400; -fx-border-width: 0 0 3 0;" +
            "-fx-background-image: repeating-linear-gradient(to bottom, transparent, transparent 12px, #FECF4D 12px, #FECF4D 15px);"
        );
        ImageView honeyPot = new ImageView(new Image(getClass().getResource("/nfc/hive2.png").toExternalForm()));
        honeyPot.setFitWidth(54);
        honeyPot.setFitHeight(54);
        Label dashboardTitle = new Label("Staff Management");
        dashboardTitle.setFont(javafx.scene.text.Font.font("Impact", javafx.scene.text.FontWeight.EXTRA_BOLD, 44));
        dashboardTitle.setStyle("-fx-text-fill: #181818;");
        headerBar.getChildren().addAll(honeyPot, dashboardTitle);

        // Main body
        VBox mainBody = new VBox(10);
        mainBody.setPadding(new Insets(20));
        mainBody.setAlignment(Pos.TOP_LEFT);

        buildTable();

        Button addBtn = new Button("Add New Staff");
        addBtn.setStyle("-fx-background-color: #FFCB3C;-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #222; -fx-background-radius: 28px;");
        addBtn.setOnAction(e -> CRUDDialogs.showStaffDialog(null, true, this::reload)); // update CRUDDialogs to use Admin

        mainBody.getChildren().addAll(table, addBtn);

        // BorderPane for full-width header
        BorderPane layout = new BorderPane();
        layout.setTop(headerBar);
        layout.setCenter(mainBody);

        getChildren().clear();
        getChildren().add(layout);

        reload();
    }

    private void buildTable() {
        table.setItems(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Admin, Integer> idCol = new TableColumn<>("ID");
        TableColumn<Admin, String> usernameCol = new TableColumn<>("Username");
        TableColumn<Admin, String> passwordCol = new TableColumn<>("Password");
        TableColumn<Admin, String> profilePictureCol = new TableColumn<>("Profile Picture");
        TableColumn<Admin, String> nameCol = new TableColumn<>("Name");
        TableColumn<Admin, Void> actCol = new TableColumn<>("Actions");

        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        passwordCol.setCellValueFactory(new PropertyValueFactory<>("password"));
        profilePictureCol.setCellValueFactory(new PropertyValueFactory<>("profilePicture"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        String fontStyle = "-fx-font-family: 'Poppins', 'Arial', sans-serif; -fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #181818;";

        idCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.valueOf(item));
                setStyle(fontStyle);
            }
        });
        usernameCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle(fontStyle);
            }
        });
        passwordCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle(fontStyle);
            }
        });
        profilePictureCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle(fontStyle);
            }
        });
        nameCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle(fontStyle);
            }
        });
        actCol.setCellFactory(tc -> new TableCell<>() {
            private final Button edit = new Button("Edit");
            private final Button del = new Button("Delete");
            {
                edit.setStyle("-fx-background-color: #FFCB3C;-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #222; -fx-background-radius: 28px;");
                del.setStyle("-fx-background-color: #FFCB3C;-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #222; -fx-background-radius: 28px;");
                edit.setOnAction(e -> showEdit(getCurrent()));
                del.setOnAction(e -> {
                    deleteAdmin(getCurrent());
                    reload();
                });
            }
            private Admin getCurrent() {
                return getTableView().getItems().get(getIndex());
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(5, edit, del));
            }
        });

        table.getColumns().setAll(idCol, usernameCol, passwordCol, profilePictureCol, nameCol, actCol);
    }

    public void reload() {
        data.clear();
        String sql = "SELECT id, username, password, profile_picture, name FROM admin";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                data.add(new Admin(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("profile_picture"),
                    rs.getString("name")
                ));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void showEdit(Admin admin) {
        CRUDDialogs.showStaffDialog(admin, false, this::reload); // update CRUDDialogs to use Admin
    }

    private void deleteAdmin(Admin admin) {
        String sql = "DELETE FROM admin WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, admin.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static class Admin {
        private final int id;
        private final String username;
        private final String password;
        private final String profilePicture;
        private final String name;

        public Admin(int id, String username, String password, String profilePicture, String name) {
            this.id = id;
            this.username = username;
            this.password = password;
            this.profilePicture = profilePicture;
            this.name = name;
        }
        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public String getProfilePicture() { return profilePicture; }
        public String getName() { return name; }
    }
}
