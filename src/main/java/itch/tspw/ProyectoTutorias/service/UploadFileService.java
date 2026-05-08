package itch.tspw.ProyectoTutorias.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class UploadFileService {

    private final Path rootPath = Paths.get("uploads");

    public String guardarImagen(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return "default.png";
        }
        if (!Files.exists(rootPath)) {
            Files.createDirectories(rootPath);
        }
        String nombreUnico = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path rutaCompleta = rootPath.resolve(nombreUnico);
        Files.copy(file.getInputStream(), rutaCompleta, StandardCopyOption.REPLACE_EXISTING);
        return nombreUnico;
    }

    public void eliminarImagen(String nombreImagen) {
        if (nombreImagen == null || nombreImagen.equals("default.png")) {
            return;
        }

        try {
            Path ruta = rootPath.resolve(nombreImagen);
            Files.deleteIfExists(ruta);
        } catch (IOException e) {
        }
    }
}