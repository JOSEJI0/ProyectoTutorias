package itch.tspw.ProyectoTutorias.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class UploadFileService {

    // Carpeta raíz donde se guardarán las fotos
    private final String FOLDER = "uploads//";

    public String guardarImagen(MultipartFile file) throws IOException {
        if (!file.isEmpty()) {
            String nombreOriginal = file.getOriginalFilename();
            String nombreUnico = UUID.randomUUID().toString() + "_" + nombreOriginal;
            
            Path rutaAbsoluta = Paths.get(FOLDER + nombreUnico);
            
            // Crea la carpeta 'uploads'
            File directorio = new File(FOLDER);
            if (!directorio.exists()) {
                directorio.mkdirs();
            }
            
            // Guarda el archivo
            Files.write(rutaAbsoluta, file.getBytes());
            return nombreUnico;
        }
        return "default.png";
    }

    public void eliminarImagen(String nombreImagen) {
        String ruta = FOLDER + nombreImagen;
        File file = new File(ruta);
        if (file.exists() && !nombreImagen.equals("default.png")) {
            file.delete();
        }
    }
}