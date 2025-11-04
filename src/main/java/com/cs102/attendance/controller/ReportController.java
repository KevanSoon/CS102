package com.cs102.attendance.controller;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import com.cs102.attendance.model.*;
import com.cs102.attendance.service.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final SessionService sessionService;
    private final GroupsService groupsService;
    private final AttendanceRecordService attendanceService;
    private final StudentService studentService;

    public ReportController(SessionService sessionService, GroupsService groupsService,
                            AttendanceRecordService attendanceService, StudentService studentService) {
        this.sessionService = sessionService;
        this.groupsService = groupsService;
        this.attendanceService = attendanceService;
        this.studentService = studentService;
    }

    @RequestMapping(value = "/generate", method = {RequestMethod.GET, RequestMethod.POST})
    public void generateReport(
        @RequestParam(defaultValue = "csv") String format,
        @RequestParam(required = false) String className,
        HttpServletResponse response) throws IOException {

        List<Session> sessions = sessionService.getAll();

        if (className != null && !className.isEmpty()) {
        sessions = sessions.stream()
                .filter(s -> className.equalsIgnoreCase(s.getClassCode()))
                .toList();
            }
        List<Groups> groups = groupsService.getAll();
        List<Student> students = studentService.getAll();
        List<AttendanceRecord> attendanceRecords = attendanceService.getAll();

        Map<String, Student> studentMap = students.stream()
                .collect(Collectors.toMap(s -> s.getId().toString(), s -> s));

        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Name", "Status", "Confidence", "Method", "Date/Time",
                "Session Name", "Class", "Group"});

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (Session session : sessions) {
            Groups group = groups.stream()
                    .filter(g -> g.getClassCode().equals(session.getClassCode())
                            && g.getGroupNumber().equals(session.getGroupNumber()))
                    .findFirst()
                    .orElse(null);
            if (group == null || group.getStudentList() == null) continue;

            for (String studentId : group.getStudentList()) {
                Student student = studentMap.get(studentId);
                if (student == null) continue;

                // Find record if student attended this session
                AttendanceRecord record = attendanceRecords.stream()
                        .filter(r -> r.getSession_id().toString().equals(session.getId().toString())
                                && r.getStudent_id().toString().equals(studentId))
                        .findFirst()
                        .orElse(null);

                String status = record != null ? record.getStatus() : "ABSENT";
                String confidence = record != null && record.getConfidence() != null ? String.valueOf(record.getConfidence()) : "NULL";
                String method = record != null ? record.getMethod() : "";
                String timestamp = record != null && record.getMarked_at() != null
                        ? dtf.format(record.getMarked_at())
                        : "";

                rows.add(new String[]{
                        student.getName(),
                        status,
                        confidence,
                        method,
                        timestamp,
                        session.getName(),
                        session.getClassCode(),
                        session.getGroupNumber()
                });
            }
        }

        if (format.equalsIgnoreCase("pdf")) {
            exportPdf(rows, response, className);
        } else {
            exportCsv(rows, response);
        }
    }

    private void exportCsv(List<String[]> rows, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"attendance_report.csv\"");
        try (PrintWriter writer = response.getWriter()) {
            for (String[] row : rows) {
                writer.println(String.join(",", Arrays.stream(row)
                        .map(cell -> "\"" + cell.replace("\"", "\"\"") + "\"")
                        .toArray(String[]::new)));
            }
        }
    }

    private void exportPdf(List<String[]> rows, HttpServletResponse response, String classCode) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=\"attendance_report.pdf\"");

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDDocumentInformation documentInformation = new PDDocumentInformation();
            documentInformation.setTitle("Attendance Report");
            document.setDocumentInformation(documentInformation);

            ClassPathResource fontResource = new ClassPathResource("fonts/NotoSans-Regular.ttf");
            PDType0Font font = PDType0Font.load(document, fontResource.getInputStream(), true);

            // float marginX = 50;
            float marginY = 50;
            float y = PDRectangle.A4.getHeight() - marginY;

            PDPageContentStream content = new PDPageContentStream(document, page);

            int fontSize = 10;

            String title = "Attendance Report" + (classCode != null && !classCode.isEmpty() ? " - " + classCode : "");
            float titleWidth = getStringWidth(font, 14, title);
            content.beginText();
            content.setFont(font, 14);
            content.newLineAtOffset((PDRectangle.A4.getWidth() - titleWidth) / 2, y);
            content.showText(title);
            content.endText();
            y -= 30;

            // Calculate max column widths based on header and first N rows
            int cols = rows.get(0).length;
            float[] colWidths = new float[cols];
            for (int c = 0; c < cols; c++) {
                float maxWidth = getStringWidth(font, fontSize, rows.get(0)[c]); // header width
                for (int r = 1; r < rows.size(); r++) {
                    float w = getStringWidth(font, fontSize, rows.get(r)[c]);
                    if (w > maxWidth) maxWidth = w;
                }
                colWidths[c] = Math.min(maxWidth + 10, 150); // padding + clamp max width
            }

            float totalWidth = 0;
            for (float w : colWidths) totalWidth += w;
            float startX = (PDRectangle.A4.getWidth() - totalWidth) / 2;

            // Draw rows
            for (String[] row : rows) {
                // Calculate row height
                int maxLines = 1;
                String[][] wrappedText = new String[cols][];
                for (int c = 0; c < cols; c++) {
                    wrappedText[c] = wrapTextWithTruncation(font, fontSize, row[c], colWidths[c] - 4);
                    if (wrappedText[c].length > maxLines) maxLines = wrappedText[c].length;
                }
                float rowHeight = (fontSize + 4) * maxLines + 4;

                // Check for new page
                if (y - rowHeight < marginY) {
                    content.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    content = new PDPageContentStream(document, page);
                    y = PDRectangle.A4.getHeight() - marginY;
                }

                // Draw cell borders
                float x = startX;
                for (int c = 0; c < cols; c++) {
                    content.addRect(x, y - rowHeight + 4, colWidths[c], rowHeight);
                    x += colWidths[c];
                }
                content.stroke();

                // Draw text inside cells
                for (int line = 0; line < maxLines; line++) {
                    x = startX;
                    for (int c = 0; c < cols; c++) {
                        String txt = line < wrappedText[c].length ? wrappedText[c][line] : "";
                        content.beginText();
                        content.setFont(font, fontSize);
                        content.newLineAtOffset(x + 2, y - (fontSize + 4) * line - fontSize);
                        content.showText(txt);
                        content.endText();
                        x += colWidths[c];
                    }
                }

                y -= rowHeight;
            }

            content.close();
            document.save(response.getOutputStream());
        }
    }

    // Wrap text with truncation for cell width
    private String[] wrapTextWithTruncation(PDType0Font font, int fontSize, String text, float maxWidth) throws IOException {
        if (text == null) return new String[]{""};
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            String temp = line.length() == 0 ? word : line + " " + word;
            if (getStringWidth(font, fontSize, temp) > maxWidth) {
                if (line.length() > 0) lines.add(line.toString());
                line = new StringBuilder(word);
                if (getStringWidth(font, fontSize, word) > maxWidth) {
                    lines.add(truncateWordToWidth(font, fontSize, word, maxWidth));
                    line = new StringBuilder();
                }
            } else {
                line = new StringBuilder(temp);
            }
        }
        if (line.length() > 0) lines.add(line.toString());
        return lines.toArray(new String[0]);
    }

    // Truncate word with ellipsis
    private String truncateWordToWidth(PDType0Font font, int fontSize, String word, float maxWidth) throws IOException {
        String ellipsis = "…";
        for (int i = 1; i <= word.length(); i++) {
            String substr = word.substring(0, i) + ellipsis;
            if (getStringWidth(font, fontSize, substr) > maxWidth) return word.substring(0, i - 1) + ellipsis;
        }
        return word;
    }

    // Get string width in points
    private float getStringWidth(PDType0Font font, int fontSize, String text) throws IOException {
        return font.getStringWidth(text != null ? text : "") / 1000f * fontSize;
    }

}