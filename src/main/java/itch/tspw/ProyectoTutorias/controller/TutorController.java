package itch.tspw.ProyectoTutorias.controller;

import java.io.IOException;
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

    public TutorController(GrupoTutoriaService grupoTutoriaService, SesionService sesionService,
                           UsuarioRepository usuarioRepository, UploadFileService uploadFileService,
                           TutorRepository tutorRepository, EvidenciaSesionRepository evidenciaRepository,
                           SesionRepository sesionRepository, AsistenciaRepository asistenciaRepository,
                           GrupoTutoriaRepository grupoTutoriaRepository,
                           ReporteService reporteService, PatRepository patRepository,
                           ActividadPatRepository actividadPatRepository) {
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
    public String verDetalleGrupo(@PathVariable Integer id, Model model) {
        GrupoTutoria grupo = grupoTutoriaService.obtenerPorId(id);
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
    public String prepararAsistencia(@RequestParam Integer idGrupo, Model model) {
        cargarModeloAsistencia(idGrupo, model);
        return "tutor/asistencia";
    }

    private void cargarModeloAsistencia(Integer idGrupo, Model model) {
        GrupoTutoria grupo = grupoTutoriaService.obtenerPorId(idGrupo);
        List<Estudiante> estudiantesActivos = grupo.getEstudiantes().stream()
                .filter(estudiante -> Boolean.TRUE.equals(estudiante.getActivo()))
                .toList();
        List<ActividadPat> actividadesPat = patRepository
                .findFirstByPeriodo_IdPeriodoAndCarrera_IdCarreraAndActivoTrueOrderByIdPatDesc(
                        grupo.getPeriodo().getIdPeriodo(),
                        grupo.getCarrera().getIdCarrera()
                )
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
        	
        	if (idEstudiantesPresentes == null) {
                idEstudiantesPresentes = new ArrayList<>();
            }
        	
            if (fotoEvidencia == null || fotoEvidencia.isEmpty()) {
                return "redirect:/tutor/panel?error=falta_evidencia";
            }

            String nombreFoto = uploadFileService.guardarImagen(fotoEvidencia);
            Sesion sesionGuardada = sesionService.registrarAsistenciaCompleta(idGrupo, semana, idActividad, idEstudiantesPresentes);

            EvidenciaSesion evidencia = new EvidenciaSesion();
            evidencia.setSesion(sesionGuardada);
            evidencia.setUrlArchivo(nombreFoto);
            evidencia.setEstatusValidacion("PENDIENTE");

            evidenciaRepository.save(evidencia);

            return "redirect:/tutor/panel?exito=asistencia_guardada";

        } catch (IOException e) {
            return "redirect:/tutor/panel?error=subida_fallida";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/tutor/panel?error=error_registro";
        }
    }

    @GetMapping("/sesion/{id}")
    public String verDetalleSesion(@PathVariable Integer id, Model model) {
        Sesion sesion = sesionRepository.findById(id).orElseThrow(() -> new RuntimeException("Sesión no encontrada"));
        model.addAttribute("sesion", sesion);
        model.addAttribute("listaAsistencia", asistenciaRepository.findBySesion_IdSesion(id));
        return "tutor/sesion-detalle";
    }
    
    @GetMapping("/reportes")
    public String verReportesTutor(Model model, Authentication authentication) {
        Tutor tutor = obtenerTutorLogueado(authentication);
        model.addAttribute("grupos", grupoTutoriaRepository.findByTutor_IdTutor(tutor.getIdTutor()));
        return "tutor/reportes";
    }

    @GetMapping("/sesion/{id}/pdf")
    public ResponseEntity<byte[]> descargarPdfAsistenciaSesion(@PathVariable Integer id) {
        Sesion sesion = sesionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sesión no encontrada"));
        
        List<Asistencia> listaAsistencia = asistenciaRepository.findBySesion_IdSesion(id);

        Map<String, Object> variables = new HashMap<>();
        variables.put("sesion", sesion);
        variables.put("grupo", sesion.getGrupo());
        variables.put("listaAsistencia", listaAsistencia);
        variables.put("fechaImpresion", LocalDate.now());

        byte[] pdf = reporteService.generarPdfDesdeHtml("documentos/tutor-lista-asistencia", variables);
        return crearPdfResponse(pdf, "Asistencia_Semana_" + sesion.getSemanaNumero() + "_" + sesion.getGrupo().getNombreGrupo() + ".pdf");
    }

    @GetMapping("/reportes/reporte-final/pdf")
    public ResponseEntity<byte[]> descargarReporteFinal(@RequestParam Integer idGrupo) {
        GrupoTutoria grupo = grupoTutoriaRepository.findById(idGrupo).orElseThrow();
        Map<String, Object> variables = Map.of(
            "grupo", grupo,
            "estudiantes", grupo.getEstudiantes(),
            "totalSesiones", sesionRepository.findByGrupo_IdGrupo(idGrupo).size(),
            "fechaImpresion", LocalDate.now()
        );
        byte[] pdf = reporteService.generarPdfDesdeHtml("pdf/tutor-reporte-final", variables);
        return crearPdfResponse(pdf, "Reporte_Final_" + grupo.getNombreGrupo() + ".pdf");
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
    public String actualizarFoto(@RequestParam MultipartFile foto, Authentication authentication) {
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