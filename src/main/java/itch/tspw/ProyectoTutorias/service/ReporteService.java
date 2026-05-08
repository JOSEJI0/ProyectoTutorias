package itch.tspw.ProyectoTutorias.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Service
public class ReporteService {

    private final TemplateEngine templateEngine;
    private final ResourceLoader resourceLoader;

    public ReporteService(TemplateEngine templateEngine, ResourceLoader resourceLoader) {
        this.templateEngine = templateEngine;
        this.resourceLoader = resourceLoader;
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
            throw new RuntimeException("Error critico al generar el PDF: " + templateName, e);
        }
    }

    public String obtenerImagenComoDataUri(String classpathLocation) {
        Resource resource = resourceLoader.getResource("classpath:" + classpathLocation);

        try {
            byte[] bytes = resource.getInputStream().readAllBytes();
            String mimeType = obtenerMimeType(classpathLocation);
            return "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar la imagen para el PDF: " + classpathLocation, e);
        }
    }

    private String obtenerMimeType(String path) {
        String lowerPath = path.toLowerCase();
        if (lowerPath.endsWith(".png")) {
            return "image/png";
        }
        if (lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        return "application/octet-stream";
    }
}
