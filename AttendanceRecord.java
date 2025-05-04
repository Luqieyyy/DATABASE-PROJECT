package nfc;

import javafx.beans.property.*;

public class AttendanceRecord {
    private final IntegerProperty childId;
    private final StringProperty name;
    private final BooleanProperty present = new SimpleBooleanProperty(false);
    private final StringProperty reason = new SimpleStringProperty("");
    private final StringProperty customReason = new SimpleStringProperty("");
    private final SimpleStringProperty scanTime = new SimpleStringProperty("");

    
    public AttendanceRecord(int childId, String name) {
        this.childId = new SimpleIntegerProperty(childId);
        this.name = new SimpleStringProperty(name);
    }
    public StringProperty scanTimeProperty() {
        return scanTime;
    }

    public String getScanTime() {
        return scanTime.get();
    }

    public void setScanTime(String scanTime) {
        this.scanTime.set(scanTime);
    }
    public int getChildId() { 
    	return childId.get();
    	}
    public IntegerProperty childIdProperty() {
    	return childId;
    	}

    public String getName() { 
    	return name.get(); 
    	}
    public StringProperty nameProperty() {
    	return name;
    	}

    public boolean isPresent() {
    	return present.get();
    	}
    public BooleanProperty presentProperty() {
    	return present;
    	}

    public String getReason() {
    	return reason.get(); 
    	}
    
    public StringProperty reasonProperty() {
    	return reason;
    	}

    public void setReason(String reason) {
    	this.reason.set(reason); 
    	}
    public void setPresent(boolean value) { 
    	this.present.set(value); 
    	}
    public StringProperty customReasonProperty() {
    	return customReason; 
    	}
    public String getCustomReason() {
    	return customReason.get();
    	}
    public void setCustomReason(String value) { 
    	customReason.set(value); 
    	}

   
    
    }

