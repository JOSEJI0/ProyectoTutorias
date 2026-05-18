package itch.tspw.ProyectoTutorias.controller;

import java.time.LocalDate;
import java.util.*;

import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.repository.*;
import itch.tspw.ProyectoTutorias.service.*;

@Controller
@RequestMapping("/estudiante")
public class EstudianteController {

    private final UsuarioRepository usuarioRepository;
    private final EstudianteRepository estudianteRepository;
    private final PatGrupoService patGrupoService;
    private final AsistenciaRepository asistenciaRepository;
    private final ReporteService reporteService;
    private final NecesidadesRepository necesidadRepository;

    public EstudianteController(UsuarioRepository usuarioRepository,
                                EstudianteRepository estudianteRepository,
                                PatGrupoService patGrupoService,
                                AsistenciaRepository asistenciaRepository,
                                ReporteService reporteService,
                                NecesidadesRepository necesidadRepository) {
        this.usuarioRepository = usuarioRepository;
        this.estudianteRepository = estudianteRepository;
        this.patGrupoService = patGrupoService;
        this.asistenciaRepository = asistenciaRepository;
        this.reporteService = reporteService;
        this.necesidadRepository = necesidadRepository;
    }


    private Estudiante obtenerEstudianteLogueado(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByCorreoInstitucional(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return estudianteRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Perfil de Estudiante no encontrado"));
    }

    private int calcularPorcentajeAsistencia(List<Asistencia> historial) {
        if (historial.isEmpty()) return 0;
        long presentes = historial.stream().filter(a -> Boolean.TRUE.equals(a.getPresente())).count();
        return (int) ((presentes * 100) / historial.size());
    }


    @GetMapping("/panel")
    public String cargarDashboardEstudiante(Model model, Authentication authentication) {
        Estudiante estudiante = obtenerEstudianteLogueado(authentication);
        model.addAttribute("estudiante", estudiante);

        if (estudiante.getGrupo() != null) {
            GrupoTutoria grupo = estudiante.getGrupo();
            model.addAttribute("grupo", grupo);
            model.addAttribute("tutor", grupo.getTutor());

            PatGrupo patGrupo = patGrupoService.obtenerPatDeGrupo(grupo.getIdGrupo());
            if (patGrupo != null) {
                model.addAttribute("actividadesPAT", patGrupoService.obtenerActividadesDeGrupo(patGrupo.getIdPatGrupo()));
            }

            List<Asistencia> misAsistencias = asistenciaRepository.findByEstudiante_IdEstudiante(estudiante.getIdEstudiante());
            model.addAttribute("porcentajeAsistencia", calcularPorcentajeAsistencia(misAsistencias));
            model.addAttribute("totalAsistencias", misAsistencias.stream().filter(a -> Boolean.TRUE.equals(a.getPresente())).count());
            model.addAttribute("totalSesiones", misAsistencias.size());
            model.addAttribute("historialAsistencias", misAsistencias);
        }
        return "estudiante/dashboard-estudiante";
    }

    @GetMapping("/deteccion-necesidades")
    public String mostrarFormularioNecesidades(Model model, Authentication authentication) {
        model.addAttribute("estudiante", obtenerEstudianteLogueado(authentication));
        return "estudiante/formulario-necesidades";
    }

    @PostMapping("/deteccion-necesidades/enviar")
    public String enviarNecesidades(@RequestParam("area") String area,
                                    @RequestParam("descripcion") String descripcion,
                                    Authentication authentication) {

        Estudiante estudiante = obtenerEstudianteLogueado(authentication);

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
        List<Asistencia> historial = asistenciaRepository.findByEstudiante_IdEstudiante(estudiante.getIdEstudiante());
        historial.sort((a1, a2) -> a2.getSesion().getFechaImparticion().compareTo(a1.getSesion().getFechaImparticion()));

        long faltas = historial.stream().filter(a -> Boolean.FALSE.equals(a.getPresente())).count();
        model.addAttribute("estudiante", estudiante);
        model.addAttribute("historial", historial);
        model.addAttribute("totalAsistencias", historial.size() - faltas);
        model.addAttribute("totalFaltas", faltas);
        return "estudiante/historial";
    }

    @GetMapping("/perfil")
    public String obtenerPerfilEstudiante(Model model, Authentication authentication) {
        Estudiante estudiante = obtenerEstudianteLogueado(authentication);
        List<Asistencia> historial = asistenciaRepository.findByEstudiante_IdEstudiante(estudiante.getIdEstudiante());

        model.addAttribute("estudiante", estudiante);
        model.addAttribute("porcentajeAsistencia", calcularPorcentajeAsistencia(historial));
        model.addAttribute("totalSesiones", historial.size());
        return "estudiante/perfil";
    }

    // --- REPORTES PDF ---

    @GetMapping("/historial/pdf")
    public ResponseEntity<byte[]> descargarHistorialPdf(Authentication authentication) {
        Estudiante estudiante = obtenerEstudianteLogueado(authentication);
        List<Asistencia> historial = asistenciaRepository.findByEstudiante_IdEstudiante(estudiante.getIdEstudiante());
        historial.sort((a1, a2) -> a1.getSesion().getFechaImparticion().compareTo(a2.getSesion().getFechaImparticion()));
        long faltas = historial.stream().filter(a -> Boolean.FALSE.equals(a.getPresente())).count();

        Map<String, Object> variables = new HashMap<>();
        variables.put("estudiante", estudiante);
        variables.put("historial", historial);
        variables.put("totalAsistencias", historial.size() - faltas);
        variables.put("totalFaltas", faltas);
        variables.put("fechaImpresion", LocalDate.now());

        byte[] pdfBytes = reporteService.generarPdfDesdeHtml("pdf/estudiante-historial", variables);
        return crearPdfResponse(pdfBytes, "Kardex_" + estudiante.getNumeroControl() + ".pdf");
    }

    @GetMapping("/constancia/pdf")
    public ResponseEntity<byte[]> descargarConstanciaPdf(Authentication authentication) {
        Estudiante estudiante = obtenerEstudianteLogueado(authentication);
        List<Asistencia> historial = asistenciaRepository.findByEstudiante_IdEstudiante(estudiante.getIdEstudiante());

        if (calcularPorcentajeAsistencia(historial) < 80) {
            throw new RuntimeException("Acceso denegado: aun no cumples con el 80% de asistencia requerido.");
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("estudiante", estudiante);
        variables.put("grupo", estudiante.getGrupo());
        variables.put("fechaImpresion", LocalDate.now());
        variables.put("logoTecNM", reporteService.obtenerImagenComoDataUri("templates/logos/logoTecNM.jpg"));
        variables.put("logoITCH", reporteService.obtenerImagenComoDataUri("templates/logos/logotecnmchilpancingo.png"));

        byte[] pdfBytes = reporteService.generarPdfDesdeHtml("pdf/estudiante-constancia", variables);
        return crearPdfResponse(pdfBytes, "Constancia_Liberacion_" + estudiante.getNumeroControl() + ".pdf");
    }

    private ResponseEntity<byte[]> crearPdfResponse(byte[] content, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }
}
