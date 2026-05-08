package itch.tspw.ProyectoTutorias.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@Service
public class ReporteService {

    private final TemplateEngine templateEngine;

    public ReporteService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] generarPdfDesdeHtml(String templateName, Map<String, Object> datos) {
        Context context = new Context();
        context.setVariables(datos);        
        String htmlContent = templateEngine.process(templateName, context);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, "/");
            builder.toStream(outputStream);
            builder.run();
            
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error crítico al generar el PDF: " + templateName, e);
        }
    }
}