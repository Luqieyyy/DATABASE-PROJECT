package nfc;

import com.fazecast.jSerialComm.SerialPort;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class ArduinoReader {
	public static SerialPort serialPort;

	public static void startReading() {
        SerialPort comPort = SerialPort.getCommPort("COM4"); // <-- set to your Arduino COM
        comPort.setBaudRate(115200);

        if (comPort.openPort()) {
            System.out.println("✅ Arduino connected!");

            Scanner scanner = new Scanner(comPort.getInputStream());
            new Thread(() -> {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine().trim();
                    System.out.println("📥 Arduino says: " + line);

                    if (line.startsWith("NFC Tag Detected:")) {
                        String uid = line.replace("NFC Tag Detected:", "").trim();
                        System.out.println("🎯 UID Read: " + uid);
                        saveAttendance(uid);
                    }
                }
            }).start();

        } else {
            System.out.println("❌ Cannot open Arduino COM port.");
        }
    }
	public static void safelyCloseSerialPort() {
	    if (serialPort != null) {
	        if (serialPort.isOpen()) {
	            serialPort.closePort();
	            System.out.println("✅ Serial port closed successfully!");
	        }
	        serialPort = null; // clear from memory
	    }
	}

    private static void saveAttendance(String uid) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO attendance (child_id, scan_time) SELECT id, NOW() FROM children WHERE nfc_uid = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, uid);
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("✅ Attendance saved for UID: " + uid);
            } else {
                System.out.println("⚠️ UID not found in children table: " + uid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
