package itch.tspw.ProyectoTutorias.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    
    @Autowired
    private ActividadPatRepository actividadPatRepository;
    
    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @GetMapping("/panel")
    public String cargarDashboardCoordinador(Model model) {
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
    public String cargarCentroDeBusquedas() {
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
    
    @GetMapping("/reportes")
    public String centroDeReportes(Model model) {
        // Mandamos los grupos y los PATs para llenar los menús desplegables (selects)
        model.addAttribute("grupos", grupoTutoriaRepository.findAll());
        model.addAttribute("patsInstitucionales", patRepository.findAll());
        
        return "coordinador/reportes";
    }
    
    @GetMapping("/reportes/grupos/pdf")
    public org.springframework.http.ResponseEntity<byte[]> descargarPdfGrupos() {
        
        List<GrupoTutoria> grupos = grupoTutoriaRepository.findAll();
        
        java.util.Map<String, Object> variables = new java.util.HashMap<>();
        variables.put("grupos", grupos);
        variables.put("fechaImpresion", java.time.LocalDate.now());
        
        // USAMOS EL REPORTE SERVICE
        byte[] pdfBytes = reporteService.generarPdfDesdeHtml("pdf/reporte-grupos", variables);
        
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Listado_Grupos_Tutorias_ITCH.pdf");
        
        return new org.springframework.http.ResponseEntity<>(pdfBytes, headers, org.springframework.http.HttpStatus.OK);
    }
    
    @GetMapping("/reportes/grupo-detalle/pdf")
    public ResponseEntity<byte[]> descargarPdfGrupoAlumnos(@RequestParam("idGrupo") Integer idGrupo) {
        
        // 1. Buscamos el grupo específico
        GrupoTutoria grupo = grupoTutoriaRepository.findById(idGrupo)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado: " + idGrupo));
        
        // 2. Preparamos las variables (pasamos el grupo y su lista de estudiantes)
        Map<String, Object> variables = new HashMap<>();
        variables.put("grupo", grupo);
        variables.put("estudiantes", grupo.getEstudiantes());
        variables.put("fechaImpresion", LocalDate.now());
        
        // 3. Generamos el PDF
        byte[] pdfBytes = reporteService.generarPdfDesdeHtml("pdf/reporte-grupo-alumnos", variables);
        
        // 4. Preparamos la descarga con un nombre de archivo dinámico
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String nombreArchivo = "Lista_Alumnos_Grupo_" + grupo.getNombreGrupo() + ".pdf";
        headers.setContentDispositionFormData("attachment", nombreArchivo);
        
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
    
    @GetMapping("/reportes/tutores/pdf")
    public ResponseEntity<byte[]> descargarPdfTutores() {
        
        // 1. Buscamos a todos los tutores en la base de datos
        List<Tutor> tutores = tutorRepository.findAll();
        
        // 2. Preparamos las variables para la plantilla
        Map<String, Object> variables = new HashMap<>();
        variables.put("tutores", tutores);
        variables.put("fechaImpresion", LocalDate.now());
        
        // 3. Generamos el PDF con nuestra nueva plantilla
        byte[] pdfBytes = reporteService.generarPdfDesdeHtml("pdf/reporte-tutores", variables);
        
        // 4. Preparamos la respuesta para la descarga
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Plantilla_Docente_Tutorias_ITCH.pdf");
        
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
    
    @GetMapping("/reportes/pats/pdf")
    public org.springframework.http.ResponseEntity<byte[]> descargarPdfPats() {
        
        // 1. Buscamos todos los PATs institucionales en la base de datos
        List<PatInstitucional> pats = patRepository.findAll();
        
        // 2. Preparamos las variables
        java.util.Map<String, Object> variables = new java.util.HashMap<>();
        variables.put("pats", pats);
        variables.put("fechaImpresion", java.time.LocalDate.now());
        
        // 3. Generamos el PDF
        byte[] pdfBytes = reporteService.generarPdfDesdeHtml("pdf/reporte-pats", variables);
        
        // 4. Preparamos la respuesta para la descarga
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Catalogo_PATs_ITCH.pdf");
        
        return new org.springframework.http.ResponseEntity<>(pdfBytes, headers, org.springframework.http.HttpStatus.OK);
    }
    
    @GetMapping("/reportes/pat-detalle/pdf")
    public org.springframework.http.ResponseEntity<byte[]> descargarPdfPatDetalle(@RequestParam("idPat") Integer idPat) {
        
        // 1. Buscamos el PAT Institucional específico
        PatInstitucional pat = patRepository.findById(idPat)
                .orElseThrow(() -> new IllegalArgumentException("PAT no encontrado: " + idPat));
        
        // 2. Buscamos sus actividades usando el repositorio
        List<ActividadPat> actividades = actividadPatRepository.findByPat_IdPat(idPat);
        
        // 3. Preparamos las variables
        java.util.Map<String, Object> variables = new java.util.HashMap<>();
        variables.put("pat", pat);
        variables.put("actividades", actividades); 
        variables.put("fechaImpresion", java.time.LocalDate.now());
        
        // 4. Generamos el PDF usando nuestra plantilla
        byte[] pdfBytes = reporteService.generarPdfDesdeHtml("pdf/reporte-pat-cronograma", variables);
        
        // 5. Preparamos la respuesta para la descarga
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
        String nombreArchivo = "Cronograma_PAT_" + pat.getVersion().replaceAll(" ", "_") + ".pdf";
        headers.setContentDispositionFormData("attachment", nombreArchivo);
        
        return new org.springframework.http.ResponseEntity<>(pdfBytes, headers, org.springframework.http.HttpStatus.OK);
    }
    
    @GetMapping("/reportes/constancias/pdf")
    public org.springframework.http.ResponseEntity<byte[]> descargarConstanciasLib(@RequestParam("idGrupo") Integer idGrupo) {
        
        GrupoTutoria grupo = grupoTutoriaRepository.findById(idGrupo)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado: " + idGrupo));
        
        // Lista donde guardaremos solo a los que pasaron
        List<Estudiante> alumnosAcreditados = new java.util.ArrayList<>();
        
        // Evaluamos a cada estudiante del grupo
        for (Estudiante estudiante : grupo.getEstudiantes()) {
            List<Asistencia> misAsistencias = asistenciaRepository.findByEstudiante_IdEstudiante(estudiante.getIdEstudiante());
            
            int totalSesiones = misAsistencias.size();
            if (totalSesiones > 0) {
                long asistenciasPositivas = misAsistencias.stream()
                        .filter(a -> Boolean.TRUE.equals(a.getPresente()))
                        .count();
                
                int porcentaje = (int) ((asistenciasPositivas * 100) / totalSesiones);
                
                // Si tiene 80% o más, se gana la constancia
                if (porcentaje >= 80) {
                    alumnosAcreditados.add(estudiante);
                }
            }
        }
        
        java.util.Map<String, Object> variables = new java.util.HashMap<>();
        variables.put("grupo", grupo);
        variables.put("alumnos", alumnosAcreditados);
        variables.put("fechaImpresion", java.time.LocalDate.now());
        
        byte[] pdfBytes = reporteService.generarPdfDesdeHtml("pdf/reporte-constancias", variables);
        
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
        String nombreArchivo = "Constancias_Liberacion_" + grupo.getNombreGrupo() + ".pdf";
        headers.setContentDispositionFormData("attachment", nombreArchivo);
        
        return new org.springframework.http.ResponseEntity<>(pdfBytes, headers, org.springframework.http.HttpStatus.OK);
    }
    
    @GetMapping("/grupo/{id}/detalle")
    public String obtenerDetalleGrupo(@PathVariable Integer id, Model model) {
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
    public String obtenerDetalleEstudiante(@PathVariable("id") Integer idEstudiante, Model model) {
        Estudiante estudiante = estudianteRepository.findById(idEstudiante)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado: " + idEstudiante));
        
        var historial = grupoTutoriaService.buscarHistorialTutoriasDeEstudiante(idEstudiante);

        model.addAttribute("estudiante", estudiante);
        model.addAttribute("historialTutorias", historial);
        
        return "coordinador/estudiantes-detalle";
    }
    @GetMapping("/necesidades")
    public String obtenerTodasNecesidades(Model model) {
        model.addAttribute("necesidades", necesidadRepository.findAllByOrderByFechaSolicitudDesc());
        return "coordinador/necesidades-lista";
    }
    @GetMapping("/perfil")
    public String obtenerPerfilCoordinador(Model model, org.springframework.security.core.Authentication authentication) {
        // Obtenemos el usuario logueado
        Usuario usuario = usuarioRepository.findByCorreoInstitucional(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        model.addAttribute("usuario", usuario);
        
        // Pasamos estadísticas rápidas para adornar su perfil
        model.addAttribute("totalTutores", tutorRepository.count());
     // Le agregamos .size() al final para que cuente cuántos objetos hay en la lista
        model.addAttribute("totalEstudiantes", estudianteRepository.findByActivoTrue().size());
        
        return "coordinador/perfil";
    }
}