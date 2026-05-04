package itch.tspw.ProyectoTutorias.service;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@Service
public class ReporteService {

    @Autowired
    private TemplateEngine templateEngine;

    public byte[] generarReportePat(Map<String, Object> datos) {
        Context context = new Context();
        context.setVariables(datos);
        
        String htmlContent = templateEngine.process("reportes/template-pat", context);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, "/");
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al compilar el PDF", e);
        }
    }
}