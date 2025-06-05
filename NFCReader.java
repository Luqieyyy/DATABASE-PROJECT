package nfc;

import com.fazecast.jSerialComm.SerialPort;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
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
            String query = "SELECT child_id, name FROM children WHERE nfc_uid = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, tagId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int childId = rs.getInt("child_id");
                String childName = rs.getString("name");

                LocalDate today = LocalDate.now();

                // Check if attendance record exists for today
                String checkSql = "SELECT * FROM attendance_status WHERE child_id = ? AND date = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setInt(1, childId);
                checkStmt.setDate(2, java.sql.Date.valueOf(today));
                ResultSet checkRs = checkStmt.executeQuery();

                LocalDateTime now = LocalDateTime.now();

                if (checkRs.next()) {
                    // Record exists, determine if we update check-in or check-out time

                    Timestamp checkInTimestamp = checkRs.getTimestamp("check_in_time");
                    Timestamp checkOutTimestamp = checkRs.getTimestamp("check_out_time");

                    boolean shouldUpdateCheckOut = false;

                    if (checkInTimestamp == null) {
                        // No check-in time yet, so update check-in time
                        String updateSql = "UPDATE attendance_status SET check_in_time = ?, is_present = 1 WHERE child_id = ? AND date = ?";
                        PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                        updateStmt.setTimestamp(1, Timestamp.valueOf(now));
                        updateStmt.setInt(2, childId);
                        updateStmt.setDate(3, java.sql.Date.valueOf(today));
                        updateStmt.executeUpdate();

                        System.out.println("✅ Check-in updated for: " + childName);

                    } else {
                        // Check-in exists, check if 8 hours passed to allow check-out
                        LocalDateTime checkInTime = checkInTimestamp.toLocalDateTime();
                        if (java.time.Duration.between(checkInTime, now).toHours() >= 8) {
                            shouldUpdateCheckOut = true;
                        } else {
                            // To prevent duplicate check-outs within 8 hours
                            System.out.println("⚠️ Check-out not allowed before 8 hours for: " + childName);
                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.WARNING);
                                alert.setHeaderText(null);
                                alert.setContentText("Check-out allowed only after 8 hours from check-in for:\n" + childName);
                                alert.showAndWait();
                            });
                        }

                        if (shouldUpdateCheckOut) {
                            String updateSql = "UPDATE attendance_status SET check_out_time = ?, is_present = 1 WHERE child_id = ? AND date = ?";
                            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                            updateStmt.setTimestamp(1, Timestamp.valueOf(now));
                            updateStmt.setInt(2, childId);
                            updateStmt.setDate(3, java.sql.Date.valueOf(today));
                            updateStmt.executeUpdate();

                            System.out.println("✅ Check-out updated for: " + childName);
                        }
                    }
                } else {
                    // No record exists for today - insert new record with check-in time
                    String insertSql = "INSERT INTO attendance_status (child_id, date, check_in_time, is_present) VALUES (?, ?, ?, 1)";
                    PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                    insertStmt.setInt(1, childId);
                    insertStmt.setDate(2, java.sql.Date.valueOf(today));
                    insertStmt.setTimestamp(3, Timestamp.valueOf(now));
                    insertStmt.executeUpdate();

                    System.out.println("✅ Attendance recorded for: " + childName);
                }

                // Update UI and charts
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Scan Successful");
                    alert.setHeaderText(null);
                    alert.setContentText("Attendance processed for:\n" + childName);
                    alert.showAndWait();
                });

                AdminDashboard.updateDashboardData();
                AttendanceView.markPresentByChildId(childId);
                AttendanceView.updateChartFromStatic();
                AttendanceView.refreshUI();

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
