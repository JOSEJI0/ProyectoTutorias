package itch.tspw.ProyectoTutorias.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.service.*;
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
        // Enviamos los grupos que están "activos" (no borrados lógicamente ni de semestres pasados)
        model.addAttribute("gruposDisponibles", grupoService.listarGruposPorEstatus(true));
        
        // Enviamos los estudiantes que no tienen grupo asignado
        model.addAttribute("estudiantes", estudianteService.listarSinGrupo());
        
        // Objeto vacío para vincular el formulario si fuera necesario, aunque usaremos RequestParams
        model.addAttribute("asignacion", new GrupoTutoria());
        
        return "coordinador/asignacion-grupos";
    }

    @PostMapping("/guardar")
    public String guardarAsignacion(@RequestParam(value = "idGrupo", required = false) Integer idGrupo,
                                    @RequestParam(value = "idEstudiantes", required = false) List<Integer> idEstudiantes) {
        
        // Validación de seguridad por si envían el formulario vacío
        if (idGrupo == null || idEstudiantes == null || idEstudiantes.isEmpty()) {
            return "redirect:/coordinador/asignacion?error=debe_seleccionar_grupo_y_alumnos";
        }

        // Buscamos el grupo seleccionado desde el dropdown
        GrupoTutoria grupo = grupoService.obtenerPorId(idGrupo);
        
        if(grupo != null) {
            // Asignamos los alumnos al grupo utilizando el servicio
            grupoService.asignarGrupo(grupo, idEstudiantes);
            return "redirect:/coordinador/grupos?exito=asignacion_correcta";
        } else {
            return "redirect:/coordinador/asignacion?error=grupo_no_encontrado";
        }
    }
}