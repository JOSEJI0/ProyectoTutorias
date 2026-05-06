package itch.tspw.ProyectoTutorias.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.repository.*;
import itch.tspw.ProyectoTutorias.service.*;

@Controller
@RequestMapping("/estudiante")
public class EstudianteController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private EstudianteRepository estudianteRepository;
    @Autowired private PatGrupoService patGrupoService;
    @Autowired private SesionRepository sesionRepository;
    @Autowired private AsistenciaRepository asistenciaRepository;
    @Autowired private NecesidadRepository necesidadRepository;
    @Autowired private ReporteService reporteService;

    private Estudiante obtenerEstudianteLogueado(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByCorreoInstitucional(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return estudianteRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Perfil de Estudiante no encontrado"));
    }

    @GetMapping("/panel")
    public String cargarDashboardEstudiante(Model model, Authentication authentication) {
        Estudiante estudiante = obtenerEstudianteLogueado(authentication);
        model.addAttribute("estudiante", estudiante);

        if (estudiante.getGrupo() != null) {
            GrupoTutoria grupo = estudiante.getGrupo();
            model.addAttribute("grupo", grupo);
            model.addAttribute("tutor", grupo.getTutor());

            // 1. Cargar el PAT para el cronograma
            PatGrupo patGrupo = patGrupoService.obtenerPatDeGrupo(grupo.getIdGrupo());
            if (patGrupo != null) {
                model.addAttribute("actividadesPAT", patGrupoService.obtenerActividadesDeGrupo(patGrupo.getIdPatGrupo()));
            }

         // 2. Lógica de Asistencias e Historial
            // Obtenemos toooodo el historial de este alumno
            List<Asistencia> misAsistencias = asistenciaRepository.findByEstudiante_IdEstudiante(estudiante.getIdEstudiante());
            
            int totalSesiones = misAsistencias.size();

            long asistenciasPositivas = misAsistencias.stream()
                    .filter(a -> Boolean.TRUE.equals(a.getPresente()))
                    .count();
            
            // Cálculo del porcentaje evitando división por cero
            int porcentaje = 0;
            if (totalSesiones > 0) {
                porcentaje = (int) ((asistenciasPositivas * 100) / totalSesiones);
            }

            model.addAttribute("porcentajeAsistencia", porcentaje);
            model.addAttribute("totalAsistencias", asistenciasPositivas);
            model.addAttribute("totalSesiones", totalSesiones);
            model.addAttribute("historialAsistencias", misAsistencias);
        }
        
        return "estudiante/dashboard-estudiante";
    }
    
    // Vista para el formulario
    @GetMapping("/deteccion-necesidades")
    public String prepararFormularioNecesidades(Model model, Authentication authentication) {
        model.addAttribute("estudiante", obtenerEstudianteLogueado(authentication));
        return "estudiante/formulario-necesidades";
    }

    @PostMapping("/deteccion-necesidades/enviar")
    public String submitNecesidades(@RequestParam("area") String area, 
                                    @RequestParam("descripcion") String descripcion, 
                                    Authentication authentication) {
        
        Estudiante estudiante = obtenerEstudianteLogueado(authentication);
        
        // Creamos y guardamos el registro en la BD
        NecesidadEstudiante necesidad = new NecesidadEstudiante();
        necesidad.setEstudiante(estudiante);
        necesidad.setArea(area);
        necesidad.setDescripcion(descripcion);
        necesidadRepository.save(necesidad);
        
        return "redirect:/estudiante/panel?exito=necesidades_enviadas";
    }
    
    @GetMapping("/historial")
    public String obtenerHistorialAsistencias(Model model, Authentication authentication) {
        Estudiante estudiante = obtenerEstudianteLogueado(authentication);
        model.addAttribute("estudiante", estudiante);
        
        // Obtenemos su historial y lo ordenamos por fecha descendente 
        List<Asistencia> historial = asistenciaRepository.findByEstudiante_IdEstudiante(estudiante.getIdEstudiante());
        historial.sort((a1, a2) -> a2.getSesion().getFechaImparticion().compareTo(a1.getSesion().getFechaImparticion()));
        
        model.addAttribute("historial", historial);
        
        // Calculamos sus faltas para un pequeño resumen superior
        long faltas = historial.stream().filter(a -> Boolean.FALSE.equals(a.getPresente())).count();
        long asistencias = historial.size() - faltas;
        
        model.addAttribute("totalAsistencias", asistencias);
        model.addAttribute("totalFaltas", faltas);
        
        return "estudiante/historial";
    }
    
    @GetMapping("/perfil")
    public String obtenerPerfilEstudiante(Model model, Authentication authentication) {
        Estudiante estudiante = obtenerEstudianteLogueado(authentication);
        model.addAttribute("estudiante", estudiante);
        
        // Calculamos su porcentaje de asistencia actual
        List<Asistencia> historial = asistenciaRepository.findByEstudiante_IdEstudiante(estudiante.getIdEstudiante());
        int totalSesiones = historial.size();
        long asistencias = historial.stream().filter(a -> Boolean.TRUE.equals(a.getPresente())).count();
        int porcentaje = totalSesiones > 0 ? (int) ((asistencias * 100) / totalSesiones) : 0;
        
        model.addAttribute("porcentajeAsistencia", porcentaje);
        model.addAttribute("totalSesiones", totalSesiones);
        
        return "estudiante/perfil";
    }
    
    @GetMapping("/historial/pdf")
    public org.springframework.http.ResponseEntity<byte[]> descargarHistorialPdf(Authentication authentication) {
        Estudiante estudiante = obtenerEstudianteLogueado(authentication);
        
        List<Asistencia> historial = asistenciaRepository.findByEstudiante_IdEstudiante(estudiante.getIdEstudiante());
        // Lo ordenamos del más antiguo al más nuevo para que tenga sentido en el PDF
        historial.sort((a1, a2) -> a1.getSesion().getFechaImparticion().compareTo(a2.getSesion().getFechaImparticion()));
        
        long faltas = historial.stream().filter(a -> Boolean.FALSE.equals(a.getPresente())).count();
        long asistencias = historial.size() - faltas;
        
        java.util.Map<String, Object> variables = new java.util.HashMap<>();
        variables.put("estudiante", estudiante);
        variables.put("historial", historial);
        variables.put("totalAsistencias", asistencias);
        variables.put("totalFaltas", faltas);
        variables.put("fechaImpresion", java.time.LocalDate.now());
        
        byte[] pdfBytes = reporteService.generarPdfDesdeHtml("pdf/estudiante-historial", variables);
        
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Kardex_Tutorias_" + estudiante.getNumeroControl() + ".pdf");
        
        return new org.springframework.http.ResponseEntity<>(pdfBytes, headers, org.springframework.http.HttpStatus.OK);
    }

    @GetMapping("/constancia/pdf")
    public org.springframework.http.ResponseEntity<byte[]> descargarConstanciaPdf(Authentication authentication) {
        Estudiante estudiante = obtenerEstudianteLogueado(authentication);
        
        // Doble validación de seguridad: Verificamos que realmente tenga el 80%
        List<Asistencia> historial = asistenciaRepository.findByEstudiante_IdEstudiante(estudiante.getIdEstudiante());
        int totalSesiones = historial.size();
        long asistencias = historial.stream().filter(a -> Boolean.TRUE.equals(a.getPresente())).count();
        int porcentaje = totalSesiones > 0 ? (int) ((asistencias * 100) / totalSesiones) : 0;
        
        if (porcentaje < 80) {
            throw new RuntimeException("Acceso denegado: Aún no cumples con el 80% de asistencia requerido.");
        }
        
        java.util.Map<String, Object> variables = new java.util.HashMap<>();
        variables.put("estudiante", estudiante);
        variables.put("grupo", estudiante.getGrupo());
        variables.put("fechaImpresion", java.time.LocalDate.now());
        
        byte[] pdfBytes = reporteService.generarPdfDesdeHtml("pdf/estudiante-constancia", variables);
        
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Constancia_Liberacion_" + estudiante.getNumeroControl() + ".pdf");
        
        return new org.springframework.http.ResponseEntity<>(pdfBytes, headers, org.springframework.http.HttpStatus.OK);
    }
}