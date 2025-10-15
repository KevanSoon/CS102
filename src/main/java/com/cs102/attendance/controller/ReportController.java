package com.cs102.attendance.controller;

import com.cs102.attendance.entity.Student;
import com.cs102.attendance.service.SupabaseRestService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private SupabaseRestService supabaseRestService;

    // Frontend
    @GetMapping("/generate")
    @ResponseBody
    public String generateReportPage() {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head><meta charset="UTF-8"/><title>Generate Report</title></head>
                <body>
                    <h3>Generate Students Report</h3>
                    <label for="format">Select format:</label>
                    <select id="format">
                        <option value="csv">CSV</option>
                        <option value="pdf">PDF</option>
                    </select>
                    <button id="generateBtn">Generate</button>

                    <script>
                        document.getElementById('generateBtn').addEventListener('click', () => {
                            const format = document.getElementById('format').value;
                            fetch(`/api/reports/generate?format=${format}`, { method: 'POST' })
                                .then(res => {
                                    if(!res.ok) {
                                        alert('Failed to generate report');
                                        return;
                                    }
                                    return res.blob();
                                })
                                .then(blob => {
                                    if(!blob) return;
                                    const url = window.URL.createObjectURL(blob);
                                    const a = document.createElement('a');
                                    a.href = url;
                                    a.download = 'students.' + format;
                                    document.body.appendChild(a);
                                    a.click();
                                    a.remove();
                                    window.URL.revokeObjectURL(url);
                                })
                                .catch(() => alert('Error generating report'));
                        });
                    </script>
                </body>
                </html>
                """;
    }

    // Endpoint to generate PDF/CSV
    @PostMapping("/generate")
    public void generateReport(@RequestParam String format, HttpServletResponse response) throws IOException {
        List<Student> students = supabaseRestService.read("students",Collections.emptyMap(),Student[].class);

        switch (format.toLowerCase()) {
            case "pdf":
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "attachment; filename=students.pdf");
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

    // CSV export
    private void exportCsv(List<Student> students, PrintWriter writer) {
        writer.println("ID,Code,Name,ClassName,StudentGroup,Email,Phone");
        for (Student s : students) {
            writer.printf("%s,%s,%s,%s,%s,%s,%s%n",
                    s.getId(), s.getCode(), s.getName(), s.getClassName(),
                    s.getStudentGroup(), s.getEmail(), s.getPhone());
        }
        writer.flush();
    }

    // PDF export using PDFBox, Standard14Fonts for fonts
    private void exportPdf(List<Student> students, HttpServletResponse response) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            PDPageContentStream content = new PDPageContentStream(document, page);

            PDType1Font headerFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font bodyFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            // Title
            content.setFont(headerFont, 14);
            content.beginText();
            content.newLineAtOffset(50, 700);
            content.showText("Students Report");
            content.endText();

            // Student details
            content.setFont(bodyFont, 12);
            float y = 670;
            for (Student s : students) {
                if (y < 50) {
                    content.close();
                    page = new PDPage(PDRectangle.LETTER);
                    document.addPage(page);
                    content = new PDPageContentStream(document, page);
                    content.setFont(bodyFont, 12);
                    y = 700;
                }
                content.beginText();
                content.newLineAtOffset(50, y);
                String line = String.format("ID: %s, Code: %s, Name: %s, Class: %s",
                        s.getId(), s.getCode(), s.getName(), s.getClassName());
                content.showText(line);
                content.endText();
                y -= 20;
            }

            content.close();
            document.save(response.getOutputStream());
        }
    }
}
