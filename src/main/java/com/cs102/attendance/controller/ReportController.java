package com.cs102.attendance.controller;

import com.cs102.attendance.model.Student;
import com.cs102.attendance.service.SupabaseRestService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.pdfbox.pdmodel.PDDocument;
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
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private SupabaseRestService supabaseRestService;

    @RequestMapping(value = "/generate", method = {RequestMethod.POST, RequestMethod.GET})
    public void generateReport(@RequestParam String format, HttpServletResponse response) throws IOException {
        List<Student> students = supabaseRestService.read("students", Collections.emptyMap(), Student[].class);

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
                    s.getId(), s.getCode(), s.getName(), s.getClassName(),
                    s.getStudentGroup(), s.getEmail(), s.getPhone());
        }
        writer.flush();
    }

    private void exportPdf(List<Student> students, HttpServletResponse response) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            PDPageContentStream content = new PDPageContentStream(document, page);

            ClassPathResource fontResource = new ClassPathResource("fonts/NotoSans-Regular.ttf");
            try (InputStream fontStream = fontResource.getInputStream()) {
                PDType0Font font = PDType0Font.load(document, fontStream, true);

                float pageWidth = PDRectangle.LETTER.getWidth();
                float marginTop = 750;
                float y = marginTop;
                float marginX = 50;
                float availableWidth = pageWidth - 2 * marginX;

                // Table headers
                String[] headers = {"ID", "Code", "Name", "Class", "Group", "Email"};

                // Gather table content
                String[][] data = new String[students.size()][headers.length];
                for (int i = 0; i < students.size(); i++) {
                    Student s = students.get(i);
                    data[i] = new String[]{
                            truncate(s.getId().toString(), 8),
                            sanitize(s.getCode()),
                            sanitize(s.getName()),
                            sanitize(s.getClassName()),
                            sanitize(s.getStudentGroup()),
                            sanitize(s.getEmail())
                    };
                }

                // Calculate max text width per column
                float[] colWidths = new float[headers.length];
                for (int col = 0; col < headers.length; col++) {
                    float maxWidth = getStringWidth(font, 12, headers[col]);
                    for (int row = 0; row < data.length; row++) {
                        maxWidth = Math.max(maxWidth, getStringWidth(font, 12, data[row][col]));
                    }
                    colWidths[col] = maxWidth + 10; // Add small padding
                }

                // Scale columns if total width exceeds availableWidth
                float totalWidth = 0;
                for (float w : colWidths) totalWidth += w;
                if (totalWidth > availableWidth) {
                    float scale = availableWidth / totalWidth;
                    for (int i = 0; i < colWidths.length; i++) colWidths[i] *= scale;
                    totalWidth = availableWidth;
                }

                // Center table horizontally
                float startX = (pageWidth - totalWidth) / 2;

                // Title
                content.beginText();
                content.setFont(font, 16);
                content.newLineAtOffset(startX, y);
                content.showText("Students Report");
                content.endText();
                y -= 30;

                // Draw table headers
                y = drawRowWithBorders(content, font, y, headers, colWidths, startX, true);

                // Draw table content
                for (String[] row : data) {
                    y = drawRowWithBorders(content, font, y, row, colWidths, startX, false);
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

    private float drawRowWithBorders(PDPageContentStream content, PDType0Font font, float y, String[] texts, float[] colWidths, float startX, boolean isHeader) throws IOException {
        float x = startX;
        int fontSize = isHeader ? 12 : 10;

        // Wrap text for each column
        String[][] wrappedText = new String[texts.length][];
        int maxLines = 1;
        for (int i = 0; i < texts.length; i++) {
            wrappedText[i] = wrapText(font, fontSize, texts[i], colWidths[i] - 4); // padding 2px each side
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

        // Draw text inside each cell
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

    private String[] wrapText(PDType0Font font, int fontSize, String text, float maxWidth) throws IOException {
        if (text == null) return new String[]{""};
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            String temp = line.length() == 0 ? word : line + " " + word;
            if (getStringWidth(font, fontSize, temp) > maxWidth) {
                if (line.length() > 0) lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(temp);
            }
        }
        lines.add(line.toString());
        return lines.toArray(new String[0]);
    }

    private float getStringWidth(PDType0Font font, int fontSize, String text) throws IOException {
        return font.getStringWidth(text) / 1000 * fontSize;
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
