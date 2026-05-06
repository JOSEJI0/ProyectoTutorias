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
    @Autowired private NecesidadRepository necesidadRepository; // Agrega esto

    private Estudiante obtenerEstudianteLogueado(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByCorreoInstitucional(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return estudianteRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Perfil de Estudiante no encontrado"));
    }

    @GetMapping("/panel")
    public String mostrarDashboardEstudiante(Model model, Authentication authentication) {
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
            
            // ¿Cuántas veces el maestro ha pasado lista para él? (Total de registros)
            int totalSesiones = misAsistencias.size();
            
            // ¿En cuántas de esas tuvo "Presente = true"?
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
    public String mostrarFormularioNecesidades(Model model, Authentication authentication) {
        model.addAttribute("estudiante", obtenerEstudianteLogueado(authentication));
        return "estudiante/formulario-necesidades";
    }

    @PostMapping("/deteccion-necesidades/enviar")
    public String enviarNecesidades(@RequestParam("area") String area, 
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
    public String verHistorialAsistencias(Model model, Authentication authentication) {
        Estudiante estudiante = obtenerEstudianteLogueado(authentication);
        model.addAttribute("estudiante", estudiante);
        
        // Obtenemos su historial y lo ordenamos por fecha descendente (lo más nuevo primero)
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
    public String verPerfilEstudiante(Model model, Authentication authentication) {
        Estudiante estudiante = obtenerEstudianteLogueado(authentication);
        model.addAttribute("estudiante", estudiante);
        return "estudiante/perfil";
    }
}