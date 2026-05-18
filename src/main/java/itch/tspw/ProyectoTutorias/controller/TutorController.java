package itch.tspw.ProyectoTutorias.controller;

import java.time.LocalDate;
import java.util.*;

import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.repository.*;
import itch.tspw.ProyectoTutorias.service.*;

@Controller
@RequestMapping("/tutor")
public class TutorController {

    private final GrupoTutoriaService grupoTutoriaService;
    private final SesionService sesionService;
    private final UsuarioRepository usuarioRepository;
    private final UploadFileService uploadFileService;
    private final TutorRepository tutorRepository;
    private final EvidenciaSesionRepository evidenciaRepository;
    private final SesionRepository sesionRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final GrupoTutoriaRepository grupoTutoriaRepository;
    private final ReporteService reporteService;
    private final PatRepository patRepository;
    private final ActividadPatRepository actividadPatRepository;
    private final NecesidadesRepository necesidadRepository;

    public TutorController(GrupoTutoriaService grupoTutoriaService, SesionService sesionService,
                           UsuarioRepository usuarioRepository, UploadFileService uploadFileService,
                           TutorRepository tutorRepository, EvidenciaSesionRepository evidenciaRepository,
                           SesionRepository sesionRepository, AsistenciaRepository asistenciaRepository,
                           GrupoTutoriaRepository grupoTutoriaRepository, ReporteService reporteService,
                           PatRepository patRepository, ActividadPatRepository actividadPatRepository,
                           NecesidadesRepository necesidadRepository) {
        this.grupoTutoriaService = grupoTutoriaService;
        this.sesionService = sesionService;
        this.usuarioRepository = usuarioRepository;
        this.uploadFileService = uploadFileService;
        this.tutorRepository = tutorRepository;
        this.evidenciaRepository = evidenciaRepository;
        this.sesionRepository = sesionRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.grupoTutoriaRepository = grupoTutoriaRepository;
        this.reporteService = reporteService;
        this.patRepository = patRepository;
        this.actividadPatRepository = actividadPatRepository;
        this.necesidadRepository = necesidadRepository;
    }

    private Tutor obtenerTutorLogueado(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByCorreoInstitucional(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return tutorRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Perfil de Tutor no encontrado"));
    }

    @GetMapping("/panel")
    public String mostrarDashboardTutor(Model model, Authentication authentication) {
        Tutor tutor = obtenerTutorLogueado(authentication);
        List<GrupoTutoria> grupos = grupoTutoriaService.obtenerGruposActivosPorTutor(tutor.getIdTutor());

        model.addAttribute("tutor", tutor);
        model.addAttribute("grupos", grupos);
        model.addAttribute("tutoradosActivos", grupos.stream()
                .mapToInt(g -> (int) g.getEstudiantes().stream().filter(Estudiante::getActivo).count()).sum()); 
        model.addAttribute("sesionesImpartidas", sesionService.contarSesionesPorTutor(tutor.getIdTutor()));
        model.addAttribute("alertas", 0);
        return "tutor/dashboard-tutor";
    }

    @GetMapping("/grupos")
    public String misGrupos(Model model, Authentication authentication) {
        Tutor tutor = obtenerTutorLogueado(authentication);
        model.addAttribute("grupos", grupoTutoriaService.obtenerGruposActivosPorTutor(tutor.getIdTutor()));
        return "tutor/mis-grupos";
    }

    @GetMapping("/grupo/{id}/detalle")
    public String verDetalleGrupo(@PathVariable("id") Integer idGrupo, Model model) {
        GrupoTutoria grupo = grupoTutoriaService.obtenerPorId(idGrupo);
        model.addAttribute("grupo", grupo);
        model.addAttribute("estudiantes", grupo.getEstudiantes());
        return "tutor/grupo-detalle";
    }

    @GetMapping("/asistencia")
    public String seleccionarGrupoAsistencia(Model model, Authentication authentication) {
        Tutor tutor = obtenerTutorLogueado(authentication);
        model.addAttribute("grupos", grupoTutoriaService.obtenerGruposActivosPorTutor(tutor.getIdTutor()));
        return "tutor/asistencia-seleccion";
    }

    @PostMapping("/asistencia")
    public String prepararAsistencia(@RequestParam("idGrupo") Integer idGrupo, Model model) {
        cargarModeloAsistencia(idGrupo, model);
        return "tutor/asistencia";
    }

    private void cargarModeloAsistencia(Integer idGrupo, Model model) {
        GrupoTutoria grupo = grupoTutoriaService.obtenerPorId(idGrupo);
        List<Estudiante> estudiantesActivos = grupo.getEstudiantes().stream()
                .filter(Estudiante::getActivo)
                .toList();
        List<ActividadPat> actividadesPat = patRepository
                .findFirstByPeriodo_IdPeriodoAndCarrera_IdCarreraAndActivoTrueOrderByIdPatDesc(
                        grupo.getPeriodo().getIdPeriodo(),
                        grupo.getCarrera().getIdCarrera())
                .map(pat -> actividadPatRepository.findByPat_IdPatAndActivoTrueOrderBySemanaProgramadaAsc(pat.getIdPat()))
                .orElseGet(Collections::emptyList);

        model.addAttribute("grupo", grupo);
        model.addAttribute("estudiantes", estudiantesActivos);
        model.addAttribute("actividadesPat", actividadesPat);
        model.addAttribute("historialSesiones", sesionService.obtenerSesionesPorGrupo(idGrupo));
    }

    @PostMapping("/guardar-asistencia")
    public String guardarAsistencia(
            @RequestParam("idGrupo") Integer idGrupo,
            @RequestParam("semana") Integer semana,
            @RequestParam("idActividad") Integer idActividad,
            @RequestParam(value = "asistencias", required = false) List<Integer> idEstudiantesPresentes,
            @RequestParam(value = "fotoEvidencia", required = false) MultipartFile fotoEvidencia) {
        
        try {
            if (idEstudiantesPresentes == null) idEstudiantesPresentes = new ArrayList<>();
            if (fotoEvidencia == null || fotoEvidencia.isEmpty()) return "redirect:/tutor/panel?error=falta_evidencia";

            String nombreFoto = uploadFileService.guardarImagen(fotoEvidencia);
            Sesion sesionGuardada = sesionService.registrarAsistenciaCompleta(idGrupo, semana, idActividad, idEstudiantesPresentes);

            EvidenciaSesion evidencia = new EvidenciaSesion();
            evidencia.setSesion(sesionGuardada);
            evidencia.setUrlArchivo(nombreFoto);
            evidencia.setEstatusValidacion("PENDIENTE");
            evidenciaRepository.save(evidencia);

            return "redirect:/tutor/panel?exito=asistencia_guardada";
        } catch (Exception e) {
            return "redirect:/tutor/panel?error=error_registro";
        }
    }

    @GetMapping("/sesion/{id}")
    public String verDetalleSesion(@PathVariable("id") Integer idSesion, Model model) {
        Sesion sesion = sesionRepository.findById(idSesion)
                .orElseThrow(() -> new RuntimeException("Sesion no encontrada"));
        model.addAttribute("sesion", sesion);
        model.addAttribute("listaAsistencia", asistenciaRepository.findBySesion_IdSesion(idSesion));
        return "tutor/sesion-detalle";
    }

    @GetMapping("/sesion/{id}/pdf")
    public ResponseEntity<byte[]> descargarPdfAsistenciaSesion(@PathVariable("id") Integer idSesion) {
        Sesion sesion = sesionRepository.findById(idSesion)
                .orElseThrow(() -> new RuntimeException("Sesion no encontrada"));

        Map<String, Object> variables = new HashMap<>();
        variables.put("sesion", sesion);
        variables.put("grupo", sesion.getGrupo());
        variables.put("listaAsistencia", asistenciaRepository.findBySesion_IdSesion(idSesion));
        variables.put("fechaImpresion", LocalDate.now());
        variables.put("logoTecNM", reporteService.obtenerImagenComoDataUri("templates/logos/logoTecNM.jpg"));
        variables.put("logoITCH", reporteService.obtenerImagenComoDataUri("templates/logos/logotecnmchilpancingo.png"));

        byte[] pdf = reporteService.generarPdfDesdeHtml("documentos/tutor-lista-asistencia", variables);
        return crearPdfResponse(pdf, "Asistencia_Semana_" + sesion.getSemanaNumero() + "_" + sesion.getGrupo().getNombreGrupo() + ".pdf");
    }

    @GetMapping("/reportes")
    public String verReportesTutor(Model model, Authentication authentication) {
        Tutor tutor = obtenerTutorLogueado(authentication);
        model.addAttribute("grupos", grupoTutoriaRepository.findByTutor_IdTutor(tutor.getIdTutor()));
        model.addAttribute("necesidades", necesidadRepository.findByEstudiante_Grupo_Tutor_IdTutorOrderByFechaSolicitudDesc(tutor.getIdTutor()));
        return "tutor/reportes";
    }

    @GetMapping("/reportes/lista-asistencia/pdf")
    public ResponseEntity<byte[]> descargarListaAsistencia(@RequestParam("idGrupo") Integer idGrupo) {
        GrupoTutoria grupo = grupoTutoriaRepository.findById(idGrupo).orElseThrow();

        Map<String, Object> variables = new HashMap<>();
        variables.put("grupo", grupo);
        variables.put("estudiantes", grupo.getEstudiantes());
        variables.put("fechaImpresion", LocalDate.now());

        byte[] pdf = reporteService.generarPdfDesdeHtml("pdf/tutor-lista-asistencia", variables);
        return crearPdfResponse(pdf, "Lista_Asistencia_" + grupo.getNombreGrupo() + ".pdf");
    }

    @GetMapping("/reportes/reporte-final/pdf")
    public ResponseEntity<byte[]> descargarReporteFinal(@RequestParam("idGrupo") Integer idGrupo) {
        GrupoTutoria grupo = grupoTutoriaRepository.findById(idGrupo).orElseThrow();

        Map<String, Object> variables = new HashMap<>();
        variables.put("grupo", grupo);
        variables.put("estudiantes", grupo.getEstudiantes());
        variables.put("totalSesiones", sesionRepository.findByGrupo_IdGrupo(idGrupo).size());
        variables.put("fechaImpresion", LocalDate.now());

        byte[] pdf = reporteService.generarPdfDesdeHtml("pdf/tutor-reporte-final", variables);
        return crearPdfResponse(pdf, "Reporte_Final_" + grupo.getNombreGrupo() + ".pdf");
    }

    @GetMapping("/necesidades")
    public String verNecesidadesAlumnos(Model model, Authentication authentication) {
        Tutor tutor = obtenerTutorLogueado(authentication);
        model.addAttribute("necesidades", necesidadRepository.findByEstudiante_Grupo_Tutor_IdTutorOrderByFechaSolicitudDesc(tutor.getIdTutor()));
        return "tutor/necesidades-lista";
    }

    @PostMapping("/necesidades/{id}/atender")
    public String marcarNecesidadComoAtendida(@PathVariable Integer id) {
        NecesidadEstudiante necesidad = necesidadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No encontrada"));
        necesidad.setEstatus("Atendida");
        necesidadRepository.save(necesidad);
        return "redirect:/tutor/necesidades?exito=atendida";
    }

    @GetMapping("/reportes/necesidad/pdf/{id}")
    public ResponseEntity<byte[]> descargarNecesidadPdf(@PathVariable("id") Integer idNecesidad) {
        NecesidadEstudiante necesidad = necesidadRepository.findById(idNecesidad).orElseThrow();

        Map<String, Object> variables = new HashMap<>();
        variables.put("necesidad", necesidad);
        variables.put("fechaImpresion", LocalDate.now());

        byte[] pdf = reporteService.generarPdfDesdeHtml("pdf/tutor-necesidad", variables);
        return crearPdfResponse(pdf, "Derivacion_" + necesidad.getEstudiante().getNumeroControl() + ".pdf");
    }

    private ResponseEntity<byte[]> crearPdfResponse(byte[] content, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }

    @GetMapping("/perfil")
    public String verPerfil(Model model, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByCorreoInstitucional(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        model.addAttribute("usuario", usuario);
        return "tutor/perfil";
    }

    @PostMapping("/perfil/actualizar-foto")
    public String actualizarFoto(@RequestParam("foto") MultipartFile foto, Authentication authentication) {
        try {
            Usuario usuario = usuarioRepository.findByCorreoInstitucional(authentication.getName()).orElseThrow();
            if (foto != null && !foto.isEmpty()) {
                if (!"default.png".equals(usuario.getFotoPerfil())) {
                    uploadFileService.eliminarImagen(usuario.getFotoPerfil());
                }
                usuario.setFotoPerfil(uploadFileService.guardarImagen(foto));
                usuarioRepository.save(usuario);
            }
            return "redirect:/tutor/perfil?exito=foto_actualizada";
        } catch (Exception e) {
            return "redirect:/tutor/perfil?error=subida_fallida";
        }
    }
}
