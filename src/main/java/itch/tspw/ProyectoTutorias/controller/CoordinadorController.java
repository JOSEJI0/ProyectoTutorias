package itch.tspw.ProyectoTutorias.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import itch.tspw.ProyectoTutorias.model.Carrera;
import itch.tspw.ProyectoTutorias.model.GrupoTutoria;
import itch.tspw.ProyectoTutorias.model.Sesion;
import itch.tspw.ProyectoTutorias.repository.CarreraRepository;
import itch.tspw.ProyectoTutorias.repository.EstudianteRepository;
import itch.tspw.ProyectoTutorias.repository.GrupoTutoriaRepository;
import itch.tspw.ProyectoTutorias.repository.SesionRepository;
import itch.tspw.ProyectoTutorias.repository.TutorRepository;
import itch.tspw.ProyectoTutorias.service.EvidenciaService;
import itch.tspw.ProyectoTutorias.service.GrupoTutoriaService;
import itch.tspw.ProyectoTutorias.service.ReporteService;
import itch.tspw.ProyectoTutorias.service.SesionService;

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

    @GetMapping("/panel")
    public String mostrarDashboardCoordinador(Model model) {
        List<GrupoTutoria> gruposActivos = grupoTutoriaRepository.findByPeriodo_EstatusActivoAndActivoTrue(true);
        
        model.addAttribute("gruposActivos", gruposActivos);
        model.addAttribute("totalGruposActivos", gruposActivos.size());
        model.addAttribute("evidenciasPendientes", evidenciaService.obtenerEvidenciasPendientes());
        model.addAttribute("alumnosSinGrupo", estudianteRepository.findByGrupoIsNullAndActivoTrue().size());
        model.addAttribute("totalTutores", tutorRepository.count()); 
        model.addAttribute("alertasAcademicas", 0);
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

        List<Sesion> sesiones = sesionRepository.findByGrupo_IdGrupo(id);

        model.addAttribute("grupo", grupo);
        model.addAttribute("sesiones", sesiones);
        model.addAttribute("totalEstudiantes", grupo.getEstudiantes().size());
        
        return "coordinador/detalle-grupo";
    }
}