package nfc;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.beans.property.*;

public class AttendanceRecord {
    private final IntegerProperty childId;
    private final StringProperty name;
    private final BooleanProperty present = new SimpleBooleanProperty(false);
    private final StringProperty reason = new SimpleStringProperty("Default");

    private final StringProperty customReason = new SimpleStringProperty("");
    private final StringProperty checkInFullTimestamp = new SimpleStringProperty("");
    private final StringProperty checkInTime = new SimpleStringProperty("");
    private final StringProperty checkOutFullTimestamp = new SimpleStringProperty("");
    private final StringProperty checkOutTime = new SimpleStringProperty("");
    
    private final BooleanProperty manualCheckOut = new SimpleBooleanProperty(false);
    private final ObjectProperty<File> reasonLetterFile = new SimpleObjectProperty<>();
    
    public AttendanceRecord(int childId, String name) {
        this.childId = new SimpleIntegerProperty(childId);
        this.name = new SimpleStringProperty(name);
        
        this.present.addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                setCheckInFullTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } else {
                setCheckInFullTimestamp("");
            }
        });
        this.manualCheckOut.addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                setCheckOutFullTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } else {
                setCheckOutFullTimestamp("");
            }
        });
    }

    // childId
    public int getChildId() { return childId.get(); }
    public IntegerProperty childIdProperty() { return childId; }

    // name
    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }

    // present
    public boolean isPresent() { return present.get(); }
    public void setPresent(boolean value) { present.set(value); }
    public BooleanProperty presentProperty() { return present; }

    // reason
    public String getReason() { return reason.get(); }
    public void setReason(String value) { reason.set(value); }
    public StringProperty reasonProperty() { return reason; }

    // customReason
    public String getCustomReason() { return customReason.get(); }
    public void setCustomReason(String value) { customReason.set(value); }
    public StringProperty customReasonProperty() { return customReason; }

    // Check-In full timestamp (for DB)
    public String getCheckInFullTimestamp() { return checkInFullTimestamp.get(); }
    public void setCheckInFullTimestamp(String value) {
        checkInFullTimestamp.set(value);
        if(value != null && !value.isEmpty()) {
            LocalDateTime dt = LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            checkInTime.set(dt.format(DateTimeFormatter.ofPattern("hh:mm a")));
        } else {
            checkInTime.set("");
        }
    }
    public StringProperty checkInFullTimestampProperty() { return checkInFullTimestamp; }

    // Check-In display time
    public String getCheckInTime() { return checkInTime.get(); }
    public StringProperty checkInTimeProperty() { return checkInTime; }

    // Check-Out full timestamp (for DB)
    public String getCheckOutFullTimestamp() { return checkOutFullTimestamp.get(); }
    public void setCheckOutFullTimestamp(String value) {
        checkOutFullTimestamp.set(value);
        if(value != null && !value.isEmpty()) {
            LocalDateTime dt = LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            checkOutTime.set(dt.format(DateTimeFormatter.ofPattern("hh:mm a")));
        } else {
            checkOutTime.set("");
        }
    }
    public StringProperty checkOutFullTimestampProperty() { return checkOutFullTimestamp; }

    // Check-Out display time
    public String getCheckOutTime() { return checkOutTime.get(); }
    public StringProperty checkOutTimeProperty() { return checkOutTime; }

    // Manual check-out
    public boolean isManualCheckOut() { return manualCheckOut.get(); }
    public void setManualCheckOut(boolean value) { manualCheckOut.set(value); }
    public BooleanProperty manualCheckOutProperty() { return manualCheckOut; }

    // Reason letter file
    public File getReasonLetterFile() { return reasonLetterFile.get(); }
    public void setReasonLetterFile(File file) { reasonLetterFile.set(file); }
    public ObjectProperty<File> reasonLetterFileProperty() { return reasonLetterFile; }
}
