package nfc;

public class MainLauncher {
    public static void main(String[] args) {
        NFCReader reader = new NFCReader("COM4"); // replace COM3 if needed
        reader.startReading();
    }
}
