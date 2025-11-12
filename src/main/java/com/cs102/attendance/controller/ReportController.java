package com.cs102.attendance.controller;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.core.io.ClassPathResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import com.cs102.attendance.model.AttendanceRecord;
import com.cs102.attendance.model.Groups;
import com.cs102.attendance.model.Session;
import com.cs102.attendance.model.Student;
import com.cs102.attendance.service.AttendanceRecordService;
import com.cs102.attendance.service.GroupsService;
import com.cs102.attendance.service.SessionService;
import com.cs102.attendance.service.StudentService;

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

    @GetMapping("/summary")
    public List<Map<String, Object>> getAttendanceSummary(
            @RequestParam(required = false) String className) {

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

        Map<String, Integer> totalClasses = new HashMap<>();
        Map<String, Integer> presentCount = new HashMap<>();

        for (Session session : sessions) {
            Groups group = groups.stream()
                    .filter(g -> g.getClassCode().equals(session.getClassCode())
                            && g.getGroupNumber().equals(session.getGroupNumber()))
                    .findFirst()
                    .orElse(null);

            if (group == null || group.getStudentList() == null) continue;

            for (String studentId : group.getStudentList()) {
                totalClasses.merge(studentId, 1, Integer::sum);

                boolean isPresent = attendanceRecords.stream()
                        .anyMatch(r -> r.getSession_id().toString().equals(session.getId().toString())
                                && r.getStudent_id().toString().equals(studentId)
                                && "PRESENT".equalsIgnoreCase(r.getStatus()));

                if (isPresent) {
                    presentCount.merge(studentId, 1, Integer::sum);
                }
            }
        }

        List<Map<String, Object>> summaryList = new ArrayList<>();
        for (String studentId : totalClasses.keySet()) {
            Student student = studentMap.get(studentId);
            if (student == null) continue;
            
            int total = totalClasses.getOrDefault(studentId, 0);
            int present = presentCount.getOrDefault(studentId, 0);
            double rate = total > 0 ? (present * 100.0 / total) : 0;

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("name", student != null ? student.getName() : "Unknown");
            entry.put("totalClasses", total);
            entry.put("present", present);
            entry.put("attendanceRate", Math.round(rate));

            summaryList.add(entry);
        }

        // Sort by attendance rate
        summaryList.sort(Comparator.comparingDouble(e -> -((Number) e.get("attendanceRate")).doubleValue()));
        return summaryList;
    }


   @RequestMapping(value = "/generate", method = {RequestMethod.GET, RequestMethod.POST})
    public void generateReport(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) throws IOException {

        List<Session> sessions = sessionService.getAll().stream()
        .filter(s -> className == null || className.isEmpty() || className.equalsIgnoreCase(s.getClassCode()))
        .filter(s -> startDate == null || (s.getDate() != null && !s.getDate().isBefore(startDate)))
        .filter(s -> endDate == null || (s.getDate() != null && !s.getDate().isAfter(endDate)))
        .collect(Collectors.toList());

        //Sort by date
        sessions.sort(Comparator.comparing(Session::getDate, Comparator.nullsLast(Comparator.naturalOrder())));

        List<Groups> groups = groupsService.getAll();
        List<Student> students = studentService.getAll();
        List<AttendanceRecord> attendanceRecords = attendanceService.getAll();

        Map<String, Student> studentMap = students.stream()
                .collect(Collectors.toMap(s -> s.getId().toString(), s -> s));

        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Name", "Status", "Confidence", "Method", "Timestamp",
                "Session Name", "Date", "Group"});

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (Session session : sessions) {
            // Match both class code AND group number for this session
            Groups group = groups.stream()
                    .filter(g -> g.getClassCode().equals(session.getClassCode())
                            && g.getGroupNumber().equals(session.getGroupNumber()))
                    .findFirst()
                    .orElse(null);

            if (group == null || group.getStudentList() == null) continue;

            String dateStr = session.getDate() != null
                    ? session.getDate().format(DateTimeFormatter.ofPattern("dd/MMM/yyyy", Locale.ENGLISH))
                    : "";

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
                String confidence = record != null && record.getConfidence() != null
                        ? String.valueOf(record.getConfidence()) : "";
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
                        dateStr,
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

            PDDocumentInformation info = new PDDocumentInformation();
            info.setTitle("Attendance Report");
            document.setDocumentInformation(info);

            ClassPathResource fontResource = new ClassPathResource("fonts/NotoSans-Regular.ttf");
            PDType0Font font = PDType0Font.load(document, fontResource.getInputStream(), true);

            float marginY = 50;
            float y = PDRectangle.A4.getHeight() - marginY;

            PDPageContentStream content = new PDPageContentStream(document, page);
            int fontSize = 10;

            // ===== Title =====
            String title = "Attendance Report" + (classCode != null && !classCode.isEmpty() ? " - " + classCode : "");
            float titleWidth = getStringWidth(font, 14, title);
            content.beginText();
            content.setNonStrokingColor(new java.awt.Color(44, 62, 80)); // dark gray-blue
            content.setFont(font, 14);
            content.newLineAtOffset((PDRectangle.A4.getWidth() - titleWidth) / 2, y);
            content.showText(title);
            content.endText();
            y -= 20;

            // ===== Generated timestamp =====
            String generated = "Generated on: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMMM uuuu - h:mm a"));
            float genWidth = getStringWidth(font, 10, generated);
            content.beginText();
            content.setNonStrokingColor(new java.awt.Color(127, 140, 141)); // light gray
            content.setFont(font, 10);
            content.newLineAtOffset((PDRectangle.A4.getWidth() - genWidth) / 2, y);
            content.showText(generated);
            content.endText();
            y -= 25;

            // ===== Column width calculation =====
            int cols = rows.get(0).length;
            float[] colWidths = new float[cols];
            for (int c = 0; c < cols; c++) {
                float maxWidth = getStringWidth(font, fontSize, rows.get(0)[c]);
                for (int r = 1; r < rows.size(); r++) {
                    float w = getStringWidth(font, fontSize, rows.get(r)[c]);
                    if (w > maxWidth) maxWidth = w;
                }
                colWidths[c] = Math.min(maxWidth + 10, 100);
            }

            float totalWidth = 0;
            for (float w : colWidths) totalWidth += w;
            float startX = (PDRectangle.A4.getWidth() - totalWidth) / 2;

            // ===== Draw rows =====
            for (int r = 0; r < rows.size(); r++) {
                String[] row = rows.get(r);

                // Calculate wrapping
                int maxLines = 1;
                String[][] wrappedText = new String[cols][];
                for (int c = 0; c < cols; c++) {
                    wrappedText[c] = wrapTextWithTruncation(font, fontSize, row[c], colWidths[c] - 4);
                    if (wrappedText[c].length > maxLines) maxLines = wrappedText[c].length;
                }
                float rowHeight = (fontSize + 4) * maxLines + 4;

                // Page overflow check
                if (y - rowHeight < marginY) {
                    content.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    content = new PDPageContentStream(document, page);
                    y = PDRectangle.A4.getHeight() - marginY;
                }

                // ===== Row backgrounds =====
                if (r == 0) {
                    // Header row - blue
                    content.setNonStrokingColor(new java.awt.Color(100, 100, 250));
                } else if (r % 2 == 0) {
                    // Alternating light gray
                    content.setNonStrokingColor(new java.awt.Color(245, 247, 250));
                } else {
                    // White
                    content.setNonStrokingColor(java.awt.Color.WHITE);
                }
                content.addRect(startX, y - rowHeight + 4, totalWidth, rowHeight);
                content.fill();

                // ===== Borders =====
                content.setStrokingColor(new java.awt.Color(189, 195, 199));
                float x = startX;
                for (int c = 0; c < cols; c++) {
                    content.addRect(x, y - rowHeight + 4, colWidths[c], rowHeight);
                    x += colWidths[c];
                }
                content.stroke();

                // ===== Text =====
                x = startX;
                for (int c = 0; c < cols; c++) {
                    String[] lines = wrappedText[c];
                    for (int line = 0; line < lines.length; line++) {
                        float textWidth = getStringWidth(font, fontSize, lines[line]);
                        float offsetX = (r == 0) ? x + (colWidths[c] - textWidth) / 2 : x + 2;
                        java.awt.Color textColor;
                        if (r == 0) {
                            textColor = java.awt.Color.WHITE;
                        } else if (c == 1) {
                            String txt = lines[line].trim().toUpperCase();
                            textColor = "PRESENT".equals(txt) ? new java.awt.Color(0, 175, 0)
                                    : "ABSENT".equals(txt) ? java.awt.Color.RED
                                    : java.awt.Color.BLACK;
                        } else {
                            textColor = java.awt.Color.BLACK;
                        }

                        // Draw text
                        content.beginText();
                        content.setNonStrokingColor(textColor);
                        content.setFont(font, fontSize);
                        content.newLineAtOffset(offsetX, y - (fontSize + 4) * line - fontSize);
                        content.showText(lines[line]);
                        content.endText();
                    }
                    x += colWidths[c];
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