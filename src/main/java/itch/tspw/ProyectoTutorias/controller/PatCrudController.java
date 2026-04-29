package itch.tspw.ProyectoTutorias.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import itch.tspw.ProyectoTutorias.model.ActividadPat;
import itch.tspw.ProyectoTutorias.model.PatInstitucional;
import itch.tspw.ProyectoTutorias.model.PeriodoEscolar;
import itch.tspw.ProyectoTutorias.service.ActividadPatService;
import itch.tspw.ProyectoTutorias.service.PatService;
import itch.tspw.ProyectoTutorias.service.PeriodoEscolarService;

@Controller
@RequestMapping("/coordinador/pat")
public class PatCrudController {

    @Autowired
    private PatService patService;
    @Autowired
    private ActividadPatService actividadService;
    @Autowired
    private PeriodoEscolarService periodoService;

    @GetMapping
    public String listarPats(Model model) {
        model.addAttribute("pats", patService.listarTodos());
        model.addAttribute("periodos", periodoService.listarTodos());
        return "coordinador/pat-lista";
    }

    @PostMapping("/guardar")
    public String guardarPat(@RequestParam("idPeriodo") Integer idPeriodo) {
        PatInstitucional pat = new PatInstitucional();
        
        PeriodoEscolar periodo = new PeriodoEscolar();
        periodo.setIdPeriodo(idPeriodo);
        pat.setPeriodo(periodo);
        
        patService.guardar(pat);
        return "redirect:/coordinador/pat?exito=guardado";
    }

    @GetMapping("/{idPat}/actividades")
    public String verActividades(@PathVariable("idPat") Integer idPat, Model model) {
        model.addAttribute("pat", patService.obtenerPorId(idPat));
        model.addAttribute("actividades", actividadService.listarPorPat(idPat));
        return "coordinador/pat-detalles";
    }

    @PostMapping("/{idPat}/actividades/guardar")
    public String guardarActividad(@PathVariable("idPat") Integer idPat,
                                   @RequestParam("titulo") String titulo,
                                   @RequestParam("descripcion") String descripcion,
                                   @RequestParam("semana") Integer semana) {
        ActividadPat actividad = new ActividadPat();
        actividad.setTitulo(titulo);
        actividad.setDescripcion(descripcion);
        actividad.setSemanaProgramada(semana);
        
        PatInstitucional pat = patService.obtenerPorId(idPat);
        actividad.setPat(pat);
        
        actividadService.guardar(actividad);
        return "redirect:/coordinador/pat/" + idPat + "/actividades?exito=actividad_guardada";
    }

    @GetMapping("/{idPat}/actividades/eliminar/{idActividad}")
    public String eliminarActividad(@PathVariable("idPat") Integer idPat, 
                                    @PathVariable("idActividad") Integer idActividad) {
        actividadService.eliminar(idActividad);
        return "redirect:/coordinador/pat/" + idPat + "/actividades?exito=actividad_eliminada";
    }
}