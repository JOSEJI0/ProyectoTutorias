package itch.tspw.ProyectoTutorias.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import itch.tspw.ProyectoTutorias.model.Estudiante;
import itch.tspw.ProyectoTutorias.model.GrupoTutoria;
import itch.tspw.ProyectoTutorias.model.Usuario;
import itch.tspw.ProyectoTutorias.repository.UsuarioRepository;
import itch.tspw.ProyectoTutorias.service.GrupoTutoriaService;
import itch.tspw.ProyectoTutorias.service.SesionService;
import itch.tspw.ProyectoTutorias.service.UploadFileService;

@Controller
@RequestMapping("/tutor")
public class TutorController {

    @Autowired
    private GrupoTutoriaService grupoTutoriaService;

    @Autowired
    private SesionService sesionService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private UploadFileService uploadFileService;

    @GetMapping("/panel")
    public String mostrarDashboardTutor(Model model) {
        Integer idTutorLogueado = 1; 
        model.addAttribute("grupos", grupoTutoriaService.obtenerGruposActivosPorTutor(idTutorLogueado));
        return "tutor/dashboard-tutor";
    }

    @PostMapping("/asistencia")
    public String prepararAsistencia(@RequestParam("idGrupo") Integer idGrupo, Model model) {
        GrupoTutoria grupo = grupoTutoriaService.obtenerPorId(idGrupo);
        List<Estudiante> estudiantes = grupo.getEstudiantes();
        
        model.addAttribute("grupo", grupo);
        model.addAttribute("estudiantes", estudiantes);
        return "tutor/asistencia";
    }

    @PostMapping("/guardar-asistencia")
    public String guardarAsistencia(@RequestParam("idGrupo") Integer idGrupo,
                                    @RequestParam("semana") Integer semana,
                                    @RequestParam("idActividad") Integer idActividad,
                                    @RequestParam(value = "asistencias", required = false) List<Integer> idEstudiantesPresentes) {
        
        sesionService.registrarAsistenciaCompleta(idGrupo, semana, idActividad, idEstudiantesPresentes);
                return "redirect:/tutor/panel?exito=asistencia_guardada";
    }
    
    @GetMapping("/perfil")
    public String verPerfil(Model model, Authentication authentication) {
        String correo = authentication.getName();
        Usuario usuario = usuarioRepository.findByCorreoInstitucional(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        model.addAttribute("usuario", usuario);
        return "tutor/perfil";
    }
    @PostMapping("/perfil/actualizar-foto")
    public String actualizarFoto(@RequestParam("foto") MultipartFile foto, 
                                 Authentication authentication) {
        try {
            String correo = authentication.getName();
            Usuario usuario = usuarioRepository.findByCorreoInstitucional(correo)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            if (foto != null && !foto.isEmpty()) {
                if (!usuario.getFotoPerfil().equals("default.png")) {
                    uploadFileService.eliminarImagen(usuario.getFotoPerfil());
                }
                
                String nombreFoto = uploadFileService.guardarImagen(foto);
                usuario.setFotoPerfil(nombreFoto);
                usuarioRepository.save(usuario);
            }
            return "redirect:/tutor/perfil?exito=foto_actualizada";
        } catch (Exception e) {
            return "redirect:/tutor/perfil?error=subida_fallida";
        }
    }
}