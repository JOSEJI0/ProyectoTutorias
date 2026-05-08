package itch.tspw.ProyectoTutorias.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import itch.tspw.ProyectoTutorias.model.GrupoTutoria;
import itch.tspw.ProyectoTutorias.service.*;
import java.util.List;

@Controller
@RequestMapping("/coordinador/asignacion")
public class AsignacionController {

    private final GrupoTutoriaService grupoService;
    private final EstudianteService estudianteService;

    public AsignacionController(GrupoTutoriaService grupoService, EstudianteService estudianteService) {
        this.grupoService = grupoService;
        this.estudianteService = estudianteService;
    }

    @GetMapping
    public String mostrarFormulario(Model model) {
        model.addAttribute("gruposDisponibles", grupoService.listarGruposPorEstatus(true));
        model.addAttribute("estudiantes", estudianteService.listarSinGrupo());
        model.addAttribute("asignacion", new GrupoTutoria());
        return "coordinador/asignacion-grupos";
    }

    @PostMapping("/guardar")
    public String guardarAsignacion(@RequestParam(required = false) Integer idGrupo,
                                    @RequestParam(required = false) List<Integer> idEstudiantes) {
        
        if (idGrupo == null || idEstudiantes == null || idEstudiantes.isEmpty()) {
            return "redirect:/coordinador/asignacion?error=faltan_datos";
        }

        GrupoTutoria grupo = grupoService.obtenerPorId(idGrupo);
        if (grupo == null) {
            return "redirect:/coordinador/asignacion?error=grupo_no_encontrado";
        }

        grupoService.asignarGrupo(grupo, idEstudiantes);
        return "redirect:/coordinador/grupos?exito=asignacion_correcta";
    }
}