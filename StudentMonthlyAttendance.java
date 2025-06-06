package nfc;

import javafx.beans.property.*;

public class StudentMonthlyAttendance {
    private final IntegerProperty childId;
    private final StringProperty name;
    private final StringProperty attendancePercent;

    private final StringProperty performance;

    public StudentMonthlyAttendance(int childId, String name,String attendancePercent, String performance) {
        this.childId = new SimpleIntegerProperty(childId);
        this.name = new SimpleStringProperty(name);
        this.attendancePercent = new SimpleStringProperty(attendancePercent);
        this.performance = new SimpleStringProperty(performance);
    }

    public IntegerProperty childIdProperty() {
        return childId;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty performanceProperty() {
        return performance;
    }

    public int getChildId() {
        return childId.get();
    }
    public StringProperty attendancePercentProperty() { return attendancePercent; }

    public String getName() {
        return name.get();
    }

    public String getAttendancePercent() {
        return attendancePercent.get();
    }
    public String getPerformance() {
        return performance.get();
    }
}
	