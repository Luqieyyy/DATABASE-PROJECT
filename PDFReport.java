package nfc;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.awt.Color;

public class PDFReport {
    public static void generateStudentMonthlyReport(
        String filePath, String logoPath, StudentInfo info, List<AttendanceRow> days, String month, String year
    ) throws Exception {
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        // Header with logo and title
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidths(new int[]{1, 4});
        Image logo = Image.getInstance(logoPath);
        logo.scaleToFit(80, 80);
        PdfPCell logoCell = new PdfPCell(logo, false);
        logoCell.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(logoCell);

        PdfPCell titleCell = new PdfPCell();
        titleCell.addElement(new Paragraph("BEE CALIPH STUDENT REPORT", new Font(Font.HELVETICA, 18, Font.BOLD, new Color(255, 179, 0))));
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        titleCell.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(titleCell);

        document.add(headerTable);
        document.add(Chunk.NEWLINE);

        // Info section
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidths(new int[]{1, 2});
        infoTable.addCell("CHILD ID:");
        infoTable.addCell(info.childId);
        infoTable.addCell("NAME:");
        infoTable.addCell(info.childName);
        infoTable.addCell("PARENT NAME:");
        infoTable.addCell(info.parentName);
        infoTable.addCell("CONTACT NUMBER:");
        infoTable.addCell(info.parentContact);
        infoTable.addCell("MONTH:");
        infoTable.addCell(month.toUpperCase());
        infoTable.addCell("YEAR:");
        infoTable.addCell(year);

        document.add(infoTable);
        document.add(Chunk.NEWLINE);

        // Attendance Table
        PdfPTable table = new PdfPTable(5);
        table.setWidths(new int[]{2, 2, 2, 2, 3});
        Font bold = new Font(Font.HELVETICA, 12, Font.BOLD);
        table.addCell(new PdfPCell(new Phrase("DATE", bold)));
        table.addCell(new PdfPCell(new Phrase("STATUS", bold)));
        table.addCell(new PdfPCell(new Phrase("CHECK-IN", bold)));
        table.addCell(new PdfPCell(new Phrase("CHECK-OUT", bold)));
        table.addCell(new PdfPCell(new Phrase("REASON", bold)));

        DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter displayFormat = DateTimeFormatter.ofPattern("h:mm a");
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("d MMM yyyy");

        for (AttendanceRow row : days) {
            // Date column
            table.addCell(row.getDate().format(dateFormat));
            // Status column
            PdfPCell statusCell = new PdfPCell(new Phrase(row.getStatus().toUpperCase(),
                "ABSENCE".equalsIgnoreCase(row.getStatus()) ?
                    new Font(Font.HELVETICA, 12, Font.BOLD, Color.RED) :
                    new Font(Font.HELVETICA, 12, Font.BOLD, Color.BLACK)
            ));
            table.addCell(statusCell);
         // Check-in
            String checkInStr = row.getCheckInTime();
            String formattedCheckIn = "";
            try {
                if (checkInStr != null && !checkInStr.isEmpty()) {
                    LocalDateTime dt = LocalDateTime.parse(checkInStr, inputFormat);
                    formattedCheckIn = dt.format(displayFormat);
                }
            } catch (Exception e) {
                formattedCheckIn = checkInStr; // fallback to raw
            }
            table.addCell(formattedCheckIn);
            // Check-out
            String checkOutStr = row.getCheckOutTime();
            String formattedCheckOut = "";
            try {
                if (checkOutStr != null && !checkOutStr.isEmpty()) {
                    LocalDateTime dt = LocalDateTime.parse(checkOutStr, inputFormat);
                    formattedCheckOut = dt.format(displayFormat);
                }
            } catch (Exception e) {
                formattedCheckOut = checkOutStr;
            }
            table.addCell(formattedCheckOut);
            // Reason
            table.addCell(row.getReason() != null ? row.getReason() : "");
        }

        document.add(table);
        document.close();
    }
}
