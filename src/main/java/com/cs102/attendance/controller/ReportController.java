package com.cs102.attendance.controller;

import com.cs102.attendance.model.Student;
import com.cs102.attendance.service.StudentService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private StudentService studentService;

    @RequestMapping(value = "/generate", method = {RequestMethod.POST, RequestMethod.GET})
    public void generateReport(@RequestParam String format, HttpServletResponse response) throws IOException {
        List<Student> students = studentService.getAll();

        switch (format.toLowerCase()) {
            case "pdf":
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "inline; filename=students.pdf"); // open in browser
                exportPdf(students, response);
                break;

            case "csv":
                response.setContentType("text/csv");
                response.setHeader("Content-Disposition", "attachment; filename=students.csv");
                exportCsv(students, response.getWriter());
                break;

            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid format: " + format);
        }
    }

    private void exportCsv(List<Student> students, PrintWriter writer) {
        writer.println("ID,Code,Name,ClassName,StudentGroup,Email,Phone");
        for (Student s : students) {
            writer.printf("%s,%s,%s,%s,%s,%s,%s%n",
                    s.getId(), s.getCode(), s.getName(), s.getClass_name(),
                    s.getStudent_group(), s.getEmail(), s.getPhone());
        }
        writer.flush();
    }

    private void exportPdf(List<Student> students, HttpServletResponse response) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            PDDocumentInformation documentInformation = new PDDocumentInformation();
            documentInformation.setTitle("Students Report");
            document.setDocumentInformation(documentInformation);

            PDPageContentStream content = new PDPageContentStream(document, page);

            ClassPathResource fontResource = new ClassPathResource("fonts/NotoSans-Regular.ttf");
            try (InputStream fontStream = fontResource.getInputStream()) {
                PDType0Font font = PDType0Font.load(document, fontStream, true);

                float pageWidth = PDRectangle.LETTER.getWidth();
                float marginTop = 750;
                float y = marginTop;
                float marginX = 50;
                float availableWidth = pageWidth - 2 * marginX;

                String[] headers = {"ID", "Code", "Name", "Class", "Group", "Email"};

                String[][] data = new String[students.size()][headers.length];
                for (int i = 0; i < students.size(); i++) {
                    Student s = students.get(i);
                    data[i] = new String[]{
                            truncate(s.getId().toString(), 8),
                            sanitize(s.getCode()),
                            sanitize(s.getName()),
                            sanitize(s.getClass_name()),
                            sanitize(s.getStudent_group()),
                            sanitize(s.getEmail())
                    };
                }

                // Define max allowable width per column in points
                float[] maxColWidths = new float[]{60, 60, 140, 60, 60, 140};

                // Calculate natural column widths (max text width per column + padding)
                float[] colWidths = new float[headers.length];
                for (int col = 0; col < headers.length; col++) {
                    float maxWidth = getStringWidth(font, 12, headers[col]);
                    for (int row = 0; row < data.length; row++) {
                        maxWidth = Math.max(maxWidth, getStringWidth(font, 12, data[row][col]));
                    }
                    colWidths[col] = maxWidth + 10; // padding
                    // Clamp width by max allowed width
                    colWidths[col] = Math.min(colWidths[col], maxColWidths[col]);
                }

                // Sum total column widths
                float totalWidth = 0;
                for (float w : colWidths) totalWidth += w;

                // If total width exceeds available width, scale all columns down proportionally
                if (totalWidth > availableWidth) {
                    float scale = availableWidth / totalWidth;
                    for (int i = 0; i < colWidths.length; i++) {
                        colWidths[i] *= scale;
                    }
                    totalWidth = availableWidth;
                }

                float startX = (pageWidth - totalWidth) / 2;

                // Title
                content.beginText();
                content.setFont(font, 16);
                content.newLineAtOffset(startX, y);
                content.showText("Students Report");
                content.endText();
                y -= 30;

                // Draw headers with wrapping
                y = drawRowWithBorders(content, font, y, headers, colWidths, startX, true);

                // Draw each row's content, wrap or truncate as fits column width
                for (String[] row : data) {
                    y = drawRowWithBorders(content, font, y, row, colWidths, startX, false);
                    // Add new page if near bottom
                    if (y < 50) {
                        content.close();
                        page = new PDPage(PDRectangle.LETTER);
                        document.addPage(page);
                        content = new PDPageContentStream(document, page);
                        y = marginTop;
                    }
                }

                content.close();
                document.save(response.getOutputStream());
            }
        }
    }

    private float drawRowWithBorders(PDPageContentStream content, PDType0Font font, float y,
    String[] texts, float[] colWidths, float startX, boolean isHeader) throws IOException {
        float x = startX;
        int fontSize = isHeader ? 12 : 10;

        // Wrap or truncate text per cell to fit col width with padding
        String[][] wrappedText = new String[texts.length][];
        int maxLines = 1;
        for (int i = 0; i < texts.length; i++) {
            // Use wrapText with max width = colWidth - padding (4 pts)
            wrappedText[i] = wrapTextWithTruncation(font, fontSize, texts[i], colWidths[i] - 4);
            maxLines = Math.max(maxLines, wrappedText[i].length);
        }

        float rowHeight = (fontSize + 4) * maxLines + 4;

        // Draw cell borders
        x = startX;
        float cellY = y;
        for (int i = 0; i < texts.length; i++) {
            content.addRect(x, cellY - rowHeight + 4, colWidths[i], rowHeight);
            x += colWidths[i];
        }
        content.stroke();

        // Draw text in each cell line by line
        for (int line = 0; line < maxLines; line++) {
            x = startX;
            for (int col = 0; col < texts.length; col++) {
                String txt = line < wrappedText[col].length ? wrappedText[col][line] : "";
                content.beginText();
                content.setFont(font, fontSize);
                content.newLineAtOffset(x + 2, y - (fontSize + 4) * line - fontSize);
                content.showText(txt);
                content.endText();
                x += colWidths[col];
            }
        }

        return y - rowHeight;
    }

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
                // If single word too long, truncate with ellipsis
                if (getStringWidth(font, fontSize, word) > maxWidth) {
                    String truncated = truncateWordToWidth(font, fontSize, word, maxWidth);
                    lines.add(truncated);
                    line = new StringBuilder();
                }
            } else {
                line = new StringBuilder(temp);
            }
        }
        if (line.length() > 0) lines.add(line.toString());

        return lines.toArray(new String[0]);
    }

    private String truncateWordToWidth(PDType0Font font, int fontSize, String word, float maxWidth) throws IOException {
        String ellipsis = "…";
        for (int i = 1; i <= word.length(); i++) {
            String substr = word.substring(0, i) + ellipsis;
            if (getStringWidth(font, fontSize, substr) > maxWidth) {
                return word.substring(0, i - 1) + ellipsis;
            }
        }
        return word;
    }

    private float getStringWidth(PDType0Font font, int fontSize, String text) throws IOException {
        return font.getStringWidth(text) / 1000f * fontSize;
    }

    private String sanitize(String text) {
        if (text == null) return "";
        return text.replace("\n", " ").replace("\r", " ");
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 1) + "…";
    }


}
