package itch.tspw.ProyectoTutorias.controller;

import itch.tspw.ProyectoTutorias.model.GrupoTutoria;
import itch.tspw.ProyectoTutorias.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/coordinador/asignaciones")
public class AsignacionController {

    private final GrupoTutoriaService grupoService;
    private final EstudianteService estudianteService;

    public AsignacionController(GrupoTutoriaService grupoService, 
                                EstudianteService estudianteService) {
        this.grupoService = grupoService;
        this.estudianteService = estudianteService;
    }

    @GetMapping
    public String mostrarFormulario(Model model) {
        
    	// Obtenemos grupos activos
        model.addAttribute("gruposDisponibles", grupoService.listarGruposPorEstatus(true));
        
        // Estudiantes sin grupo asignado
        model.addAttribute("estudiantes", estudianteService.listarSinGrupo());
        
        // Objeto para vinculación en la vista
        model.addAttribute("asignacion", new GrupoTutoria());
        
        return "coordinador/asignacion-grupos";
    }

    @PostMapping("/guardar")
    public String guardarAsignacion(@RequestParam(value = "idGrupo", required = false) Integer idGrupo,
                                    @RequestParam(value = "idEstudiantes", required = false) List<Integer> idEstudiantes) {
        
        // Validación de datos nulos o vacíos con mensajes específicos del código nuevo
        if (idGrupo == null || idEstudiantes == null || idEstudiantes.isEmpty()) {
            return "redirect:/coordinador/asignaciones?error=debe_seleccionar_grupo_y_alumnos";
        }

        GrupoTutoria grupo = grupoService.obtenerPorId(idGrupo);
        
        if (grupo != null) {
            // Procesamos la asignación masiva
            grupoService.asignarGrupo(grupo, idEstudiantes);
            return "redirect:/coordinador/grupos?exito=asignacion_correcta";
        } else {
            return "redirect:/coordinador/asignaciones?error=grupo_no_encontrado";
        }
    }
}