
package nfc;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public class ChildrenView extends VBox {

    private final TableView<Child> table = new TableView<>();
    private final ObservableList<Child> data = FXCollections.observableArrayList();

    public ChildrenView() {
        // --- HEADER (Yellow Bar) ---
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

        Label headerTitle = new Label("Children & Parents");
        headerTitle.setStyle("-fx-font-family: Impact; -fx-font-size: 44px; -fx-font-weight: bold; -fx-text-fill: #181818;");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        headerBar.getChildren().addAll(honeyPot, headerTitle, headerSpacer);

        // --- MAIN CONTENT AREA ---
        VBox mainContent = new VBox(10);
        mainContent.setPadding(new Insets(20));
        mainContent.setAlignment(Pos.TOP_LEFT);

        buildTable();

        Button addBtn = new Button("Add New Child");
        addBtn.getStyleClass().add("button-birulawa");
        addBtn.setOnAction(e -> CRUDDialogs.showChildDialog(null, true, this::reload));

        mainContent.getChildren().addAll(table, addBtn);

        // --- COMBINE WITH BORDERPANE ---
        BorderPane layout = new BorderPane();
        layout.setTop(headerBar);
        layout.setCenter(mainContent);

        // --- ChildrenView as VBox root ---
        this.getChildren().add(layout);

        reload();
    }

    private void buildTable() {
        table.setItems(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Child, Integer>      idCol            = new TableColumn<>("ID");
        TableColumn<Child, String>       nameCol          = new TableColumn<>("Name");
        TableColumn<Child, LocalDate>    dobCol           = new TableColumn<>("Birth Date");
        TableColumn<Child, String>       parentNameCol    = new TableColumn<>("Parent Name");
        TableColumn<Child, String>       parentContactCol = new TableColumn<>("Parent Contact");
        TableColumn<Child, String>       uidCol           = new TableColumn<>("NFC UID");
        TableColumn<Child, Void>         actionsCol       = new TableColumn<>("Actions");

        idCol           .setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol         .setCellValueFactory(new PropertyValueFactory<>("name"));
        dobCol          .setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        parentNameCol   .setCellValueFactory(new PropertyValueFactory<>("parentName"));
        parentContactCol.setCellValueFactory(new PropertyValueFactory<>("parentContact"));
        uidCol          .setCellValueFactory(new PropertyValueFactory<>("uid"));

        actionsCol.setCellFactory(tc -> new TableCell<>() {
            private final Button edit = new Button("Edit");
            private final Button del  = new Button("Delete");
            {
                edit.getStyleClass().add("button-birulawa1");
                del .getStyleClass().add("buttonlogout1");
                edit.setOnAction(e -> showEdit(getCurrent()));
                del .setOnAction(e -> {
                    Child current = getCurrent();
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete child and all their attendance records?", ButtonType.YES, ButtonType.NO);
                    alert.setHeaderText("Confirm Delete");
                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES) {
                            deleteChild(current);
                            reload();
                        }
                    });
                });

            }
            private Child getCurrent() {
                return getTableView().getItems().get(getIndex());
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new HBox(5, edit, del));
                }
            }
        });

        table.getColumns().setAll(
            idCol,
            nameCol,
            dobCol,
            parentNameCol,
            parentContactCol,
            uidCol,
            actionsCol
        );
    }

    public void reload() {
        data.clear();
        String sql = """
            SELECT child_id,
                   name,
                   birth_date,
                   parent_name,
                   parent_contact,
                   nfc_uid
              FROM children
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement  stmt = conn.createStatement();
             ResultSet  rs   = stmt.executeQuery(sql)) {

            while (rs.next()) {
                data.add(new Child(
                    rs.getInt("child_id"),
                    rs.getString("name"),
                    rs.getDate("birth_date") != null
                        ? rs.getDate("birth_date").toLocalDate()
                        : null,
                    rs.getString("parent_name"),
                    rs.getString("parent_contact"),
                    rs.getString("nfc_uid")
                ));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void showEdit(Child c) {
        CRUDDialogs.showChildDialog(c, false, this::reload);
    }

    private void deleteChild(Child c) {
        try (var conn = DatabaseConnection.getConnection()) {
            // 1. Find the child's ID (since your delete uses nfc_uid, get id first)
            int childId = c.getId();

            // 2. Delete from attendance_status first
            String delAttendance = "DELETE FROM attendance_status WHERE child_id=?";
            try (var psAttend = conn.prepareStatement(delAttendance)) {
                psAttend.setInt(1, childId);
                psAttend.executeUpdate();
            }

            // 3. Now delete from children
            String delChild = "DELETE FROM children WHERE nfc_uid=?";
            try (var psChild = conn.prepareStatement(delChild)) {
                psChild.setString(1, c.getUid());
                psChild.executeUpdate();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static class Child {
        private final int       child_id;
        private final String    name;
        private final LocalDate birthDate;
        private final String    parentName;
        private final String    parentContact;
        private final String    uid;

        public Child(int child_id,
                     String name,
                     LocalDate birthDate,
                     String parentName,
                     String parentContact,
                     String uid) {
            this.child_id      =child_id;
            this.name          = name;
            this.birthDate     = birthDate;
            this.parentName    = parentName;
            this.parentContact = parentContact;
            this.uid           = uid;
        }

        public int        getId()            { return child_id; }
        public String     getName()          { return name; }
        public LocalDate  getBirthDate()     { return birthDate; }
        public String     getParentName()    { return parentName; }
        public String     getParentContact() { return parentContact; }
        public String     getUid()           { return uid; }
    }
}
