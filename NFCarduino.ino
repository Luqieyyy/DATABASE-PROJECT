#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include <PN532_I2C.h>
#include <PN532.h>
#include <NfcAdapter.h>

// LCD
LiquidCrystal_I2C lcd(0x27, 16, 2);

// NFC Module
PN532_I2C pn532_i2c(Wire);
PN532 nfc(pn532_i2c); // <-- CHANGE: use PN532 directly!

// Buzzer
#define BUZZER_PIN 8

void setup() {
  delay(3000);  // Wait 3 seconds to stabilize USB serial
  Serial.begin(115200);
  lcd.init();
  lcd.backlight();
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Waiting for NFC");
  lcd.setCursor(0, 1);
  lcd.print("Scan your card!");

  nfc.begin();
  nfc.SAMConfig(); // important to config as reader mode

  pinMode(BUZZER_PIN, OUTPUT);
  digitalWrite(BUZZER_PIN, LOW);
}

void beepBuzzer() {
  digitalWrite(BUZZER_PIN, HIGH);
  delay(200);
  digitalWrite(BUZZER_PIN, LOW);
}

void loop() {
  uint8_t success;
  uint8_t uid[7]; // maximum UID length
  uint8_t uidLength;

  success = nfc.readPassiveTargetID(PN532_MIFARE_ISO14443A, uid, &uidLength);

  if (success) {
    String uidString = "";
    for (uint8_t i = 0; i < uidLength; i++) {
      if (uid[i] < 0x10) uidString += "0"; // Add leading zero
      uidString += String(uid[i], HEX);
    }
    uidString.toUpperCase(); // Convert to uppercase for database

    Serial.println(uidString); // ✅ Only send clean UID

    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("Tag Detected!");
    lcd.setCursor(0, 1);
    lcd.print(uidString);

    beepBuzzer();

    delay(2000);

    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("Waiting for NFC");
    lcd.setCursor(0, 1);
    lcd.print("Scan your card!");
  }

  delay(300); // Small delay
}
