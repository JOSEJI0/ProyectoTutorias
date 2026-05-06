package itch.tspw.ProyectoTutorias.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired private GrupoTutoriaService grupoTutoriaService;
    @Autowired private SesionService sesionService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private UploadFileService uploadFileService;
    @Autowired private TutorRepository tutorRepository;
    @Autowired private EvidenciaSesionRepository evidenciaRepository;
    @Autowired private SesionRepository sesionRepository;
    @Autowired private AsistenciaRepository asistenciaRepository;
    @Autowired private NecesidadRepository necesidadRepository;

    // Método auxiliar para evitar código repetido
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
 // Este método atrapa el clic en la tarjeta del grupo para ver los alumnos
    @GetMapping("/grupo/{id}/detalle")
    public String verDetalleGrupo(@PathVariable("id") Integer idGrupo, Model model) {
        GrupoTutoria grupo = grupoTutoriaService.obtenerPorId(idGrupo);
        model.addAttribute("grupo", grupo);
        // Mandamos la lista de alumnos activos a la vista
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
        GrupoTutoria grupo = grupoTutoriaService.obtenerPorId(idGrupo); 
        
        model.addAttribute("grupo", grupo);
        model.addAttribute("estudiantes", grupo.getEstudiantes());
        model.addAttribute("historialSesiones", sesionService.obtenerSesionesPorGrupo(idGrupo)); 
        
        return "tutor/asistencia";
    }

    @PostMapping("/guardar-asistencia")
    public String guardarAsistencia(@RequestParam("idGrupo") Integer idGrupo,
                                    @RequestParam("semana") Integer semana,
                                    @RequestParam("idActividad") Integer idActividad,
                                    @RequestParam(value = "asistencias", required = false) List<Integer> idEstudiantesPresentes,
                                    @RequestParam("fotoEvidencia") MultipartFile fotoEvidencia) { 
        
        Sesion sesionGuardada = sesionService.registrarAsistenciaCompleta(idGrupo, semana, idActividad, idEstudiantesPresentes);
        
        if (fotoEvidencia != null && !fotoEvidencia.isEmpty()) {
            try {
                String nombreFoto = uploadFileService.guardarImagen(fotoEvidencia);
                EvidenciaSesion evidencia = new EvidenciaSesion();
                evidencia.setSesion(sesionGuardada); 
                evidencia.setUrlArchivo(nombreFoto);
                evidencia.setEstatusValidacion("PENDIENTE"); 
                evidenciaRepository.save(evidencia);
            } catch (IOException e) {
                return "redirect:/tutor/panel?error=subida_fallida";
            }
        }

        return "redirect:/tutor/panel?exito=asistencia_guardada";
    }

    @GetMapping("/sesion/{id}")
    public String verDetalleSesion(@PathVariable("id") Integer idSesion, Model model) {
        Sesion sesion = sesionRepository.findById(idSesion)
                .orElseThrow(() -> new RuntimeException("Sesión no encontrada"));
        
        model.addAttribute("sesion", sesion);
        model.addAttribute("listaAsistencia", asistenciaRepository.findBySesion_IdSesion(idSesion));
        
        return "tutor/sesion-detalle";
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
            Usuario usuario = usuarioRepository.findByCorreoInstitucional(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            if (foto != null && !foto.isEmpty()) {
                if (!usuario.getFotoPerfil().equals("default.png")) {
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
}