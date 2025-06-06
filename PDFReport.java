package nfc;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;

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
		    Document document = new Document(PageSize.A4, 36, 36, 36, 36);
		    PdfWriter.getInstance(document, new FileOutputStream(filePath));
		    document.open();

		    // --- Header: full-width yellow box with logo and bold black title ---
		    PdfPTable headerTable = new PdfPTable(2);
		    headerTable.setWidthPercentage(100);
		    headerTable.setWidths(new float[]{1.2f, 8f});

		    Image logo = Image.getInstance(logoPath);
		    logo.scaleToFit(65, 65);
		    PdfPCell logoCell = new PdfPCell(logo, false);
		    logoCell.setBorder(Rectangle.NO_BORDER);
		    logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		    logoCell.setBackgroundColor(new Color(255, 203, 60));
		    logoCell.setPaddingLeft(4);
		    logoCell.setPaddingTop(4);
		    logoCell.setPaddingBottom(4);

		    PdfPCell titleCell = new PdfPCell();
		    titleCell.setBorder(Rectangle.NO_BORDER);
		    titleCell.setBackgroundColor(new Color(255, 203, 60));
		    Paragraph title = new Paragraph("BEE CALIPH STUDENT REPORT",
		            new Font(Font.HELVETICA, 26, Font.BOLD, Color.BLACK));
		    title.setAlignment(Element.ALIGN_LEFT);
		    title.setSpacingBefore(5f);
		    titleCell.addElement(title);
		    headerTable.addCell(logoCell);
		    headerTable.addCell(titleCell);

		    document.add(headerTable);
		    document.add(Chunk.NEWLINE);

		    // --- Info Section (2 columns, all fields left-aligned, no extra box) ---
		    PdfPTable infoTable = new PdfPTable(4);
		    infoTable.setWidthPercentage(100);
		    infoTable.setSpacingAfter(14f);
		    infoTable.setWidths(new float[]{2.7f, 4f, 2.4f, 3.5f});

		    Font infoLabelFont = new Font(Font.HELVETICA, 13, Font.BOLD);
		    Font infoValueFont = new Font(Font.HELVETICA, 13, Font.NORMAL);

		    // Row 1
		    infoTable.addCell(makeInfoCell("CHILD ID:", infoLabelFont, Element.ALIGN_LEFT));
		    infoTable.addCell(makeInfoCell(info.childId, infoValueFont, Element.ALIGN_LEFT));
		    infoTable.addCell(makeInfoCell("MONTH:", infoLabelFont, Element.ALIGN_LEFT));
		    infoTable.addCell(makeInfoCell(month.toUpperCase(), infoValueFont, Element.ALIGN_LEFT));

		    // Row 2
		    infoTable.addCell(makeInfoCell("NAME:", infoLabelFont, Element.ALIGN_LEFT));
		    infoTable.addCell(makeInfoCell(info.childName, infoValueFont, Element.ALIGN_LEFT));
		    infoTable.addCell(makeInfoCell("YEAR:", infoLabelFont, Element.ALIGN_LEFT));
		    infoTable.addCell(makeInfoCell(year, infoValueFont, Element.ALIGN_LEFT));

		    // Row 3 (parent name, no right-side columns)
		    infoTable.addCell(makeInfoCell("PARENT NAME:", infoLabelFont, Element.ALIGN_LEFT));
		    infoTable.addCell(makeInfoCell(info.parentName, infoValueFont, Element.ALIGN_LEFT));
		    // Empty right columns for this row:
		    infoTable.addCell(makeInfoCell("", infoLabelFont, Element.ALIGN_LEFT));
		    infoTable.addCell(makeInfoCell("", infoLabelFont, Element.ALIGN_LEFT));

		    // Row 4 (contact number, no right-side columns)
		    infoTable.addCell(makeInfoCell("CONTACT NUMBER:", infoLabelFont, Element.ALIGN_LEFT));
		    infoTable.addCell(makeInfoCell(info.parentContact, infoValueFont, Element.ALIGN_LEFT));
		    infoTable.addCell(makeInfoCell("", infoLabelFont, Element.ALIGN_LEFT));
		    infoTable.addCell(makeInfoCell("", infoLabelFont, Element.ALIGN_LEFT));

		    document.add(infoTable);
		    // --- Attendance Table (styled) ---
		    PdfPTable table = new PdfPTable(5);
		    table.setWidthPercentage(100);
		    table.setWidths(new float[]{2.2f, 2.1f, 2.0f, 2.0f, 3.2f});
		    Font tableHeaderFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.BLACK);

		    String[] headers = {"DATE", "STATUS", "CHECK-IN", "CHECK-OUT", "REASON"};
		    for (String h : headers) {
		        PdfPCell cell = new PdfPCell(new Phrase(h, tableHeaderFont));
		        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		        cell.setPadding(7f);
		        cell.setBackgroundColor(new Color(255, 203, 60));
		        table.addCell(cell);
		    }

		    DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		    DateTimeFormatter displayFormat = DateTimeFormatter.ofPattern("h:mm a");
		    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("d MMM yyyy");

		    Font statusBoldRed = new Font(Font.HELVETICA, 12, Font.BOLD, Color.RED);
		    Font statusBoldBlack = new Font(Font.HELVETICA, 12, Font.BOLD, Color.BLACK);
		    Font reasonRed = new Font(Font.HELVETICA, 12, Font.NORMAL, Color.RED);
		    Font regular = new Font(Font.HELVETICA, 12, Font.NORMAL);

		    for (AttendanceRow row : days) {
		        // Date
		        PdfPCell dateCell = new PdfPCell(new Phrase(row.getDate().format(dateFormat), regular));
		        dateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		        dateCell.setPadding(7f);
		        table.addCell(dateCell);

		        // Status
		        boolean isAbsence = "ABSENCE".equalsIgnoreCase(row.getStatus());
		        PdfPCell statusCell = new PdfPCell(new Phrase(row.getStatus().toUpperCase(),
		                isAbsence ? statusBoldRed : statusBoldBlack));
		        statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		        statusCell.setPadding(7f);
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
		            formattedCheckIn = checkInStr;
		        }
		        PdfPCell checkInCell = new PdfPCell(new Phrase(formattedCheckIn, regular));
		        checkInCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		        checkInCell.setPadding(7f);
		        table.addCell(checkInCell);

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
		        PdfPCell checkOutCell = new PdfPCell(new Phrase(formattedCheckOut, regular));
		        checkOutCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		        checkOutCell.setPadding(7f);
		        table.addCell(checkOutCell);

		        // Reason (red if not empty)
		        String reason = row.getReason() != null ? row.getReason() : "";
		        PdfPCell reasonCell = new PdfPCell(new Phrase(reason, (reason.trim().isEmpty() ? regular : reasonRed)));
		        reasonCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		        reasonCell.setPadding(7f);
		        table.addCell(reasonCell);
		    }

		    document.add(table);
		    
		    Paragraph spacer = new Paragraph();
		    spacer.setSpacingBefore(120f); // adjust as needed
		    document.add(spacer);

		    LineSeparator line = new LineSeparator();
		    line.setLineWidth(1f);
		    line.setPercentage(30);
		    line.setAlignment(Element.ALIGN_RIGHT);
		    line.setLineColor(Color.GRAY);
		    document.add(line);

		    Paragraph sign = new Paragraph("PENGURUS BEE CALIPH", new Font(Font.HELVETICA, 14, Font.BOLD, Color.BLACK));
		    sign.setAlignment(Element.ALIGN_RIGHT);
		    document.add(sign);

		    document.close();
		}

		// --- Helper for info cells ---
	private static PdfPCell makeInfoCell(String value, Font font, int alignment) {
	    PdfPCell cell = new PdfPCell(new Phrase(value, font));
	    cell.setBorder(Rectangle.NO_BORDER);
	    cell.setHorizontalAlignment(alignment);
	    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
	    cell.setPaddingLeft(3f);
	    cell.setPaddingBottom(6f);
	    return cell;
	}
}
