package itch.tspw.ProyectoTutorias.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.repository.*;
import itch.tspw.ProyectoTutorias.service.*;

@Controller
@RequestMapping("/coordinador")
public class CoordinadorController {

    @Autowired
    private EvidenciaService evidenciaService;

    @Autowired
    private GrupoTutoriaService grupoTutoriaService;

    @Autowired
    private SesionService sesionService;
    
    @Autowired
    private ReporteService reporteService;
    
    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;
    
    @Autowired
    private CarreraRepository carreraRepository;

    @Autowired
    private GrupoTutoriaRepository grupoTutoriaRepository;

    @Autowired
    private SesionRepository sesionRepository;
    
    @Autowired
    private NecesidadRepository necesidadRepository;
    // Inyectamos el repositorio del PAT
    @Autowired
    private PatRepository patRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PatGrupoService patGrupoService;

    @GetMapping("/panel")
    public String mostrarDashboardCoordinador(Model model) {
        // Obtenemos los grupos activos para el horario
        List<GrupoTutoria> gruposActivos = grupoTutoriaRepository.findByPeriodo_EstatusActivoAndActivoTrue(true);
        
        model.addAttribute("gruposActivos", gruposActivos);
        model.addAttribute("totalGruposActivos", gruposActivos.size());
        model.addAttribute("evidenciasPendientes", evidenciaService.obtenerEvidenciasPendientes());
        
        // Contamos a los alumnos que aún no tienen grupo
        model.addAttribute("alumnosSinGrupo", estudianteRepository.findByGrupoIsNullAndActivoTrue().size());
        
        model.addAttribute("totalTutores", tutorRepository.count()); 
        model.addAttribute("alertasAcademicas", 0); // Placeholder para futuras alertas
        
        return "coordinador/dashboard-coordinador";
    }

    @GetMapping("/busquedas")
    public String mostrarCentroDeBusquedas() {
        return "coordinador/busquedas";
    }

    @GetMapping("/buscar-tutores")
    public String buscarTutoresPorSemestre(@RequestParam("idPeriodo") Integer idPeriodo, Model model) {
        model.addAttribute("resultados", grupoTutoriaService.buscarTutoriasPorPeriodo(idPeriodo));
        model.addAttribute("periodoId", idPeriodo);
        return "coordinador/resultados-tutores";
    }

    @GetMapping("/buscar-historial-alumno")
    public String buscarHistorialAlumno(@RequestParam("idEstudiante") Integer idEstudiante, Model model) {
        model.addAttribute("resultados", grupoTutoriaService.buscarHistorialTutoriasDeEstudiante(idEstudiante));
        model.addAttribute("estudianteId", idEstudiante);
        return "coordinador/resultados-historial";
    }

    @GetMapping("/buscar-actividades")
    public String buscarActividadesPorFecha(@RequestParam("fecha") LocalDate fecha, Model model) {
        model.addAttribute("resultados", sesionService.buscarActividadesPorFecha(fecha));
        model.addAttribute("fechaBusqueda", fecha);
        return "coordinador/resultados-actividades";
    }

    @PostMapping("/validar-evidencia")
    public String validarEvidencia(@RequestParam("idEvidencia") Integer idEvidencia,
                                   @RequestParam("estatus") String estatus,
                                   @RequestParam(value = "notas", required = false) String notas) {
        evidenciaService.validarEvidencia(idEvidencia, estatus, notas);
        return "redirect:/coordinador/panel?exito=evidencia_validada";
    }
    
    @GetMapping("/reportes/descargar-pat")
    public ResponseEntity<byte[]> descargarPdf() {
        long totalTutoresReales = tutorRepository.count();
        long totalEstudiantesReales = estudianteRepository.count();
        
        List<Carrera> todasLasCarreras = carreraRepository.findAll();
        List<Map<String, Object>> desgloseCarreras = todasLasCarreras.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("nombre", c.getNombreCarrera());
            map.put("alumnos", estudianteRepository.countByCarrera(c)); 
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> datos = new HashMap<>();
        datos.put("totalTutores", totalTutoresReales);
        datos.put("alumnosAtendidos", totalEstudiantesReales);
        datos.put("periodo", "Enero - Junio 2026");
        datos.put("carreras", desgloseCarreras); 
        
        byte[] pdfBytes = reporteService.generarReportePat(datos);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Reporte_Institucional_Tutorias.pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
    
    @GetMapping("/grupo/{id}/detalle")
    public String verDetalleGrupo(@PathVariable Integer id, Model model) {
        GrupoTutoria grupo = grupoTutoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado: " + id));

        // Mantenemos las sesiones por si luego quieres poner un "Historial de Asistencia"
        List<Sesion> sesiones = sesionRepository.findByGrupo_IdGrupo(id);

        Optional<PatInstitucional> patInstitucionalOpt = patRepository.findFirstByPeriodo_IdPeriodoAndCarrera_IdCarreraAndActivoTrueOrderByIdPatDesc(
                grupo.getPeriodo().getIdPeriodo(),
                grupo.getCarrera().getIdCarrera()
        );

        // NUEVO: Buscamos el PAT clonado de este grupo específico y sus actividades
        PatGrupo patGrupo = patGrupoService.obtenerPatDeGrupo(id);
        if (patGrupo != null) {
            model.addAttribute("actividadesGrupo", patGrupoService.obtenerActividadesDeGrupo(patGrupo.getIdPatGrupo()));
        }

        model.addAttribute("grupo", grupo);
        model.addAttribute("sesiones", sesiones);
        model.addAttribute("totalEstudiantes", grupo.getEstudiantes().size());
        
        patInstitucionalOpt.ifPresent(patInstitucional -> model.addAttribute("patInstitucional", patInstitucional));
        
        return "coordinador/detalle-grupo";
    }
    @GetMapping("/estudiantes/detalle/{id}")
    public String verDetalleEstudiante(@PathVariable("id") Integer idEstudiante, Model model) {
        Estudiante estudiante = estudianteRepository.findById(idEstudiante)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado: " + idEstudiante));
        
        var historial = grupoTutoriaService.buscarHistorialTutoriasDeEstudiante(idEstudiante);

        model.addAttribute("estudiante", estudiante);
        model.addAttribute("historialTutorias", historial);
        
        return "coordinador/estudiantes-detalle";
    }
    @GetMapping("/necesidades")
    public String verTodasNecesidades(Model model) {
        model.addAttribute("necesidades", necesidadRepository.findAllByOrderByFechaSolicitudDesc());
        return "coordinador/necesidades-lista";
    }
    @GetMapping("/perfil")
    public String verPerfilCoordinador(Model model, org.springframework.security.core.Authentication authentication) {
        // Obtenemos el usuario logueado
        Usuario usuario = usuarioRepository.findByCorreoInstitucional(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        model.addAttribute("usuario", usuario);
        
        // Pasamos estadísticas rápidas para adornar su perfil
        model.addAttribute("totalTutores", tutorRepository.count());
        model.addAttribute("totalEstudiantes", estudianteRepository.findByActivoTrue());
        
        return "coordinador/perfil";
    }
}