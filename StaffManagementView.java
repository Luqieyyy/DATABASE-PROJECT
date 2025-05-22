package nfc;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.*;

public class StaffManagementView extends VBox {

    private final TableView<Staff> table = new TableView<>();
    private final ObservableList<Staff> data = FXCollections.observableArrayList();

    public StaffManagementView() {
     	Label title = new Label("Staff Management");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        title.setPadding(new Insets(0, 0, 10, 0));
        setSpacing(10);
        setPadding(new Insets(20));
        setAlignment(Pos.TOP_CENTER);
    	
        setSpacing(10);
        setPadding(new Insets(20));
        setAlignment(Pos.TOP_CENTER);

        buildTable();

        Button addBtn = new Button("Add New Staff");
        addBtn.getStyleClass().add("button-birulawa");
        addBtn.setOnAction(e -> CRUDDialogs.showStaffDialog(null, true, this::reload));

        getChildren().addAll(title,table, addBtn);
        reload();
    }

    private void buildTable() {
        table.setItems(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Staff, Integer> idCol     = new TableColumn<>("ID");
        TableColumn<Staff, String>  nameCol   = new TableColumn<>("Name");
        TableColumn<Staff, String>  contactCol= new TableColumn<>("Contact Number");
        TableColumn<Staff, String>  roleCol   = new TableColumn<>("Role");
        TableColumn<Staff, Void>    actCol    = new TableColumn<>("Actions");

        idCol     .setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol   .setCellValueFactory(new PropertyValueFactory<>("name"));
        contactCol.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));
        roleCol   .setCellValueFactory(new PropertyValueFactory<>("role"));

        actCol.setCellFactory(tc -> new TableCell<>() {
            private final Button edit = new Button("Edit");
            private final Button del  = new Button("Delete");
            {
                edit.getStyleClass().add("button-birulawa1");
                del .getStyleClass().add("buttonlogout1");
                edit.setOnAction(e -> showEdit(getCurrent()));
                del .setOnAction(e -> {
                    deleteStaff(getCurrent());
                    reload();
                });
            }
            private Staff getCurrent() {
                return getTableView().getItems().get(getIndex());
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(5, edit, del));
            }
        });

        table.getColumns().setAll(idCol, nameCol, contactCol, roleCol, actCol);
    }

    public void reload() {
        data.clear();
        String sql = "SELECT id, name, contact_number, role FROM staff";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement  stmt = conn.createStatement();
             ResultSet  rs   = stmt.executeQuery(sql)) {

            while (rs.next()) {
                data.add(new Staff(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("contact_number"),
                    rs.getString("role")
                ));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void showEdit(Staff s) {
        CRUDDialogs.showStaffDialog(s, false, this::reload);
    }

    private void deleteStaff(Staff s) {
        String sql = "DELETE FROM staff WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, s.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static class Staff {
        private final int    id;
        private final String name;
        private final String contactNumber;
        private final String role;

        public Staff(int id, String name, String contactNumber, String role) {
            this.id            = id;
            this.name          = name;
            this.contactNumber = contactNumber;
            this.role          = role;
        }
        public int    getId()            { return id; }
        public String getName()          { return name; }
        public String getContactNumber(){ return contactNumber; }
        public String getRole()          { return role; }
    }
}
