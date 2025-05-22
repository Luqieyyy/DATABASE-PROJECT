package nfc;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.util.Callback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Consumer;

public class CRUDDialogs {

    // ------------------ Child Dialog ------------------

    public static void showChildDialog(ChildrenView.Child existing,
                                       boolean isNew,
                                       Runnable onSave) {
        Dialog<ChildrenView.Child> dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(isNew ? "Add New Child" : "Edit Child");

        TextField nameTf          = new TextField();
        DatePicker dobPicker      = new DatePicker();
        TextField parentNameTf    = new TextField();
        TextField parentContactTf = new TextField();
        TextField uidTf           = new TextField();

        if (!isNew && existing != null) {
            nameTf.setText(existing.getName());
            if (existing.getBirthDate() != null) {
                dobPicker.setValue(existing.getBirthDate());
            }
            parentNameTf.setText(existing.getParentName());
            parentContactTf.setText(existing.getParentContact());
            uidTf.setText(existing.getUid());
            uidTf.setDisable(true);
        }

        VBox content = new VBox(10,
            new Label("Child Name:"),     nameTf,
            new Label("Birth Date:"),     dobPicker,
            new Label("Parent Name:"),    parentNameTf,
            new Label("Parent Contact:"), parentContactTf,
            new Label("NFC UID:"),        uidTf
        );
        content.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(content);

        ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == saveType) {
                return new ChildrenView.Child(
                    isNew ? 0 : existing.getId(),
                    nameTf.getText().trim(),
                    dobPicker.getValue(),
                    parentNameTf.getText().trim(),
                    parentContactTf.getText().trim(),
                    uidTf.getText().trim()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(child -> {
            if (isNew) {
                String insert = """
                    INSERT INTO children
                      (name, birth_date, parent_name, parent_contact, nfc_uid)
                    VALUES (?, ?, ?, ?, ?)
                """;
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement(insert)) {
                    ps.setString(1, child.getName());
                    ps.setObject(2, child.getBirthDate());
                    ps.setString(3, child.getParentName());
                    ps.setString(4, child.getParentContact());
                    ps.setString(5, child.getUid());
                    ps.executeUpdate();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                String update = """
                    UPDATE children
                       SET name=?, birth_date=?, parent_name=?, parent_contact=?
                     WHERE nfc_uid=?
                """;
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement(update)) {
                    ps.setString(1, child.getName());
                    ps.setObject(2, child.getBirthDate());
                    ps.setString(3, child.getParentName());
                    ps.setString(4, child.getParentContact());
                    ps.setString(5, child.getUid());
                    ps.executeUpdate();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            onSave.run();
        });
    }

    // ------------------ Action Cell Factory ------------------

    public static <T> Callback<TableColumn<T, Void>, TableCell<T, Void>> createActionCell(
            Consumer<T> onEdit,
            Consumer<T> onDelete
    ) {
        return col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button delBtn  = new Button("Delete");

            {
                editBtn.getStyleClass().add("button-birulawa1");
                delBtn.getStyleClass().add("buttonlogout1");
                editBtn.setOnAction(e -> onEdit.accept(getCurrentItem()));
                delBtn.setOnAction(e -> onDelete.accept(getCurrentItem()));
                setGraphic(new HBox(5, editBtn, delBtn));
            }

            private T getCurrentItem() {
                return getTableView().getItems().get(getIndex());
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : getGraphic());
            }
        };
    }

    // ------------------ Staff Dialog ------------------

    public static void showStaffDialog(StaffManagementView.Staff existing,
                                       boolean isNew,
                                       Runnable onSave) {
        Dialog<StaffManagementView.Staff> dlg = new Dialog<>();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle(isNew ? "Add New Staff" : "Edit Staff");

        TextField nameTf    = new TextField();
        TextField contactTf = new TextField();
        TextField roleTf    = new TextField();

        if (!isNew && existing != null) {
            nameTf.setText(existing.getName());
            contactTf.setText(existing.getContactNumber());
            roleTf.setText(existing.getRole());
        }

        VBox vb = new VBox(10,
            new Label("Name:"),           nameTf,
            new Label("Contact Number:"), contactTf,
            new Label("Role:"),           roleTf
        );
        vb.setPadding(new Insets(20));
        dlg.getDialogPane().setContent(vb);

        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        dlg.setResultConverter(bt -> {
            if (bt == saveBtn) {
                return new StaffManagementView.Staff(
                    isNew ? 0 : existing.getId(),
                    nameTf.getText().trim(),
                    contactTf.getText().trim(),
                    roleTf.getText().trim()
                );
            }
            return null;
        });

        dlg.showAndWait().ifPresent(staff -> {
            if (isNew) {
                String insert = "INSERT INTO staff(name, contact_number, role) VALUES (?, ?, ?)";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement(insert)) {
                    ps.setString(1, staff.getName());
                    ps.setString(2, staff.getContactNumber());
                    ps.setString(3, staff.getRole());
                    ps.executeUpdate();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                String update = "UPDATE staff SET name=?, contact_number=?, role=? WHERE id=?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement(update)) {
                    ps.setString(1, staff.getName());
                    ps.setString(2, staff.getContactNumber());
                    ps.setString(3, staff.getRole());
                    ps.setInt   (4, existing.getId());
                    ps.executeUpdate();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            onSave.run();
        });
    }
}
