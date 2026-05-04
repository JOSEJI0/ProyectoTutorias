package itch.tspw.ProyectoTutorias.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import itch.tspw.ProyectoTutorias.model.GrupoTutoria;
import itch.tspw.ProyectoTutorias.service.EstudianteService;
import itch.tspw.ProyectoTutorias.service.GrupoTutoriaService;
import itch.tspw.ProyectoTutorias.service.PeriodoEscolarService;

import java.util.List;

@Controller
@RequestMapping("/coordinador/asignacion")
public class AsignacionController {

    @Autowired
    private GrupoTutoriaService grupoService;
    
    @Autowired
    private EstudianteService estudianteService;
    
    @Autowired
    private PeriodoEscolarService periodoService;

    @GetMapping
    public String mostrarFormulario(Model model) {
        model.addAttribute("gruposDisponibles", grupoService.listarGruposPorEstatus(true));
        
        model.addAttribute("estudiantes", estudianteService.listarSinGrupo());
        
        model.addAttribute("asignacion", new GrupoTutoria());
        
        return "coordinador/asignacion-grupos";
    }

    @PostMapping("/guardar")
    public String guardarAsignacion(@RequestParam(value = "idGrupo", required = false) Integer idGrupo,
                                    @RequestParam(value = "idEstudiantes", required = false) List<Integer> idEstudiantes) {
        
        if (idGrupo == null || idEstudiantes == null || idEstudiantes.isEmpty()) {
            return "redirect:/coordinador/asignacion?error=debe_seleccionar_grupo_y_alumnos";
        }

        GrupoTutoria grupo = grupoService.obtenerPorId(idGrupo);
        
        if(grupo != null) {
            grupoService.asignarGrupo(grupo, idEstudiantes);
            return "redirect:/coordinador/grupos?exito=asignacion_correcta";
        } else {
            return "redirect:/coordinador/asignacion?error=grupo_no_encontrado";
        }
    }
}