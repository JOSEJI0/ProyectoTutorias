package itch.tspw.ProyectoTutorias.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import itch.tspw.ProyectoTutorias.model.GrupoTutoria;
import itch.tspw.ProyectoTutorias.service.CarreraService;
import itch.tspw.ProyectoTutorias.service.EstudianteService;
import itch.tspw.ProyectoTutorias.service.GrupoTutoriaService;
import itch.tspw.ProyectoTutorias.service.PeriodoEscolarService;
import itch.tspw.ProyectoTutorias.service.TutorService;

import java.util.List;

@Controller
@RequestMapping("/coordinador/asignacion")
public class AsignacionController {

    @Autowired
    private GrupoTutoriaService grupoService;
    @Autowired
    private TutorService tutorService;
    @Autowired
    private EstudianteService estudianteService;
    @Autowired
    private CarreraService carreraService;
    @Autowired
    private PeriodoEscolarService periodoService;

    @GetMapping
    public String mostrarFormulario(Model model) {
        model.addAttribute("tutores", tutorService.listarTodos());
        model.addAttribute("estudiantes", estudianteService.listarTodos());
        model.addAttribute("carreras", carreraService.listarTodas());
        model.addAttribute("periodos", periodoService.listarTodos());
        
        model.addAttribute("nuevoGrupo", new GrupoTutoria());
        return "coordinador/asignacion-grupos";
    }

    @PostMapping("/guardar")
    public String guardarAsignacion(@ModelAttribute GrupoTutoria grupo,
                                    @RequestParam(value = "idEstudiantes", required = false) List<Integer> idEstudiantes) {
        
        grupo.setPeriodo(periodoService.obtenerActivo());
        
        grupoService.asignarGrupo(grupo, idEstudiantes);
        return "redirect:/coordinador/panel?exito=asignacion_completa";
    }
}