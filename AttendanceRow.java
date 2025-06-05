package nfc;

import java.time.LocalDate;
import javafx.beans.property.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.ObjectProperty;

public class AttendanceRow {
    private final IntegerProperty childId;
    private final StringProperty name;
    private final StringProperty status;
    private final StringProperty reason;
    private final StringProperty checkInTime;
    private final StringProperty checkOutTime;
    private final ObjectProperty<LocalDate> date; // <-- add this

    

    public AttendanceRow(int childId, String name, String status, String reason, String checkInTime, String checkOutTime, LocalDate date) {
        this.childId = new SimpleIntegerProperty(childId);
        this.name = new SimpleStringProperty(name);
        this.status = new SimpleStringProperty(status);
        this.reason = new SimpleStringProperty(reason);
        this.checkInTime = new SimpleStringProperty(checkInTime);
        this.checkOutTime = new SimpleStringProperty(checkOutTime);
        this.date = new SimpleObjectProperty<>(date); // This line now works
        
        
    }


    public IntegerProperty childIdProperty() { return childId; }
    public StringProperty nameProperty() { return name; }
    public StringProperty statusProperty() { return status; }
    public StringProperty reasonProperty() { return reason; }
    public StringProperty checkInTimeProperty() { return checkInTime; }
    public StringProperty checkOutTimeProperty() { return checkOutTime; }
    public ObjectProperty<LocalDate> dateProperty() { return date; }
    public LocalDate getDate() { return date.get(); }
    public String getStatus() { return status.get(); }
    public String getCheckInTime() { return checkInTime.get(); }
    public String getCheckOutTime() { return checkOutTime.get(); }
    public String getReason() { return reason.get(); }


}
