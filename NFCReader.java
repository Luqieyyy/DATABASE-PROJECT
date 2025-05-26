package nfc;

import com.fazecast.jSerialComm.SerialPort;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import nfc.AdminDashboard;
public class NFCReader implements Runnable {

    private SerialPort serialPort;
    private final String portName;
    private volatile boolean running = true;

    public NFCReader(String portName) {
        this.portName = portName;
    }

    @Override
    public void run() {
        while (running && !Thread.currentThread().isInterrupted()) {

            serialPort = SerialPort.getCommPort(portName);
            serialPort.setBaudRate(115200);
            serialPort.setNumDataBits(8);
            serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
            serialPort.setParity(0);

            if (serialPort.openPort()) {
                System.out.println("✅ Serial port " + portName + " opened successfully!");
            } else {
                System.out.println("❌ Failed to open serial port " + portName + "!");
                return;
            }

            System.out.println("📡 Listening for NFC tags...");

            try (InputStream in = serialPort.getInputStream()) {
                byte[] buffer = new byte[64];

                while (running) {
                    if (in.available() > 0) {
                        int len = in.read(buffer);
                        if (len > 0) {
                            String tagId = bytesToHex(buffer, len).trim();
                            if (tagId.length() >= 8 && tagId.length() <= 40) {
                                System.out.println("🏷️ Tag detected: " + tagId);
                                processTag(tagId);
                            } else {
                                System.out.println("⚠️ Ignored invalid tag: " + tagId);
                            }
                        }
                    }
                    Thread.sleep(300);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (serialPort != null && serialPort.isOpen()) {
                    serialPort.closePort();
                    System.out.println("🔒 Serial port closed.");
                }
            }
        }
    }

    public void stopReading() {
        running = false;
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            System.out.println("✅ Serial port closed successfully.");
        }
    }

    private String bytesToHex(byte[] bytes, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%02X", bytes[i]));
        }
        return sb.toString();
    }

    private void processTag(String tagId) {
        try (Connection conn = DatabaseConnection.getConnection()) {

            String query = "SELECT id, name FROM children WHERE nfc_uid = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, tagId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int childId = rs.getInt("id");
                String childName = rs.getString("name");

                // Check last scan (to prevent duplicate)
                String lastScanQuery = "SELECT scan_time FROM attendance WHERE child_id = ? ORDER BY scan_time DESC LIMIT 1";
                PreparedStatement lastScanStmt = conn.prepareStatement(lastScanQuery);
                lastScanStmt.setInt(1, childId);
                ResultSet lastScanRs = lastScanStmt.executeQuery();

                boolean allowInsert = true;

                if (lastScanRs.next()) {
                    LocalDateTime lastScanTime = lastScanRs.getTimestamp("scan_time").toLocalDateTime();
                    LocalDateTime now = LocalDateTime.now();
                    if (java.time.Duration.between(lastScanTime, now).toMinutes() < 1) {
                        allowInsert = false;
                    }
                }

                if (allowInsert) {

                    // 🔥 1. Insert to attendance
                    String insertSql = "INSERT INTO attendance (child_id, scan_time) VALUES (?, ?)";
                    PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                    insertStmt.setInt(1, childId);
                    insertStmt.setObject(2, LocalDateTime.now());
                    insertStmt.executeUpdate();

                    // 🔥 2. Update attendance_status (for pie chart and reports)
                    String statusSql = """
                        REPLACE INTO attendance_status 
                        (child_id, date, is_present, reason, scan_time)
                        VALUES (?, CURDATE(), 1, '', NOW())
                        """;
                    PreparedStatement statusStmt = conn.prepareStatement(statusSql);
                    statusStmt.setInt(1, childId);
                    statusStmt.executeUpdate();

                    System.out.println("✅ Attendance recorded for: " + childName);

                    // ✅ Update UI and chart
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Scan Successful");
                        alert.setHeaderText(null);
                        alert.setContentText("Attendance marked for:\n" + childName);
                        alert.showAndWait();
                    });

                    // ✅ Update all data views
                    AdminDashboard.updateDashboardData();
                    AttendanceView.markPresentByChildId(childId); // will tick the box
                    AttendanceView.updateChartFromStatic();  // updates the chart
                    AttendanceView.refreshUI();

                } else {
                    System.out.println("⚠️ Duplicate tap ignored: " + childName);

                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Duplicate Scan");
                        alert.setHeaderText(null);
                        alert.setContentText(childName + " already scanned recently!\nTry again later.");
                        alert.showAndWait();
                    });
                }

            } else {
                System.out.println("❌ Unknown card.");
                Platform.runLater(() -> {
                	AdminDashboard.handleNfcAttendance(tagId);
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
