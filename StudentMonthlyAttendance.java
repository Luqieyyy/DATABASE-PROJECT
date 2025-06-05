package nfc;

import javafx.beans.property.*;

public class StudentMonthlyAttendance {
    private final IntegerProperty childId;
    private final StringProperty name;
    private final StringProperty performance;

    public StudentMonthlyAttendance(int childId, String name, String performance) {
        this.childId = new SimpleIntegerProperty(childId);
        this.name = new SimpleStringProperty(name);
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

    public String getName() {
        return name.get();
    }

    public String getPerformance() {
        return performance.get();
    }
}
	