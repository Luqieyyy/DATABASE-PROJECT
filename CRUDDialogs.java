
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
                editBtn.setStyle("-fx-background-color: #FFCB3C;-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #222; -fx-background-radius: 28px;");
                delBtn.setStyle("-fx-background-color: #FFCB3C;-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #222; -fx-background-radius: 28px;");
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

    public static void showStaffDialog(StaffManagementView.Admin existing,
            boolean isNew,
            Runnable onSave) {
Dialog<StaffManagementView.Admin> dlg = new Dialog<>();
dlg.initModality(Modality.APPLICATION_MODAL);
dlg.setTitle(isNew ? "Add New Staff" : "Edit Staff");

TextField usernameTf = new TextField();
TextField passwordTf = new TextField();
TextField profilePictureTf = new TextField();
TextField nameTf = new TextField();

if (!isNew && existing != null) {
usernameTf.setText(existing.getUsername());
// Password field left blank for security
profilePictureTf.setText(existing.getProfilePicture());
nameTf.setText(existing.getName());
}

VBox vb = new VBox(10,
new Label("Username:"),       usernameTf,
new Label("Password:"),       passwordTf,
new Label("Profile Picture:"), profilePictureTf,
new Label("Name:"),           nameTf
);
vb.setPadding(new Insets(20));
dlg.getDialogPane().setContent(vb);

ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
dlg.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

dlg.setResultConverter(bt -> {
if (bt == saveBtn) {
return new StaffManagementView.Admin(
isNew ? 0 : existing.getId(),
usernameTf.getText().trim(),
passwordTf.getText().trim(),
profilePictureTf.getText().trim(),
nameTf.getText().trim()
);
}
return null;
});
dlg.showAndWait().ifPresent(admin -> {
String hashedPassword = admin.getPassword().isEmpty()
? (!isNew && existing != null ? existing.getPassword() : "")
: PasswordUtil.hashPassword(admin.getPassword());

if (isNew) {
String insert = "INSERT INTO admin (username, password, profile_picture, name) VALUES (?, ?, ?, ?)";
try (Connection conn = DatabaseConnection.getConnection();
PreparedStatement ps = conn.prepareStatement(insert)) {
ps.setString(1, admin.getUsername());
ps.setString(2, hashedPassword);
ps.setString(3, admin.getProfilePicture());
ps.setString(4, admin.getName());
ps.executeUpdate();
} catch (SQLException ex) {
ex.printStackTrace();
}
} else {
String update;
if (admin.getPassword().isEmpty()) {
// Don't update password if left blank
update = "UPDATE admin SET username=?, profile_picture=?, name=? WHERE id=?";
} else {
update = "UPDATE admin SET username=?, password=?, profile_picture=?, name=? WHERE id=?";
}
try (Connection conn = DatabaseConnection.getConnection();
PreparedStatement ps = conn.prepareStatement(update)) {
ps.setString(1, admin.getUsername());
if (admin.getPassword().isEmpty()) {
ps.setString(2, admin.getProfilePicture());
ps.setString(3, admin.getName());
ps.setInt(4, admin.getId());
} else {
ps.setString(2, hashedPassword);
ps.setString(3, admin.getProfilePicture());
ps.setString(4, admin.getName());
ps.setInt(5, admin.getId());
}
ps.executeUpdate();
} catch (SQLException ex) {
ex.printStackTrace();
}
}
onSave.run();
});
}

}
