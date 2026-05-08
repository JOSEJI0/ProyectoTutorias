package itch.tspw.ProyectoTutorias.controller;

import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.service.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/coordinador/pat")
public class PatCrudController {

    private final PatService patService;
    private final ActividadPatService actividadService;
    private final PeriodoEscolarService periodoService;
    private final CarreraService carreraService;
    private final PatGrupoService patGrupoService;

    public PatCrudController(PatService patService, ActividadPatService actividadService,
                             PeriodoEscolarService periodoService, CarreraService carreraService,
                             PatGrupoService patGrupoService) {
        this.patService = patService;
        this.actividadService = actividadService;
        this.periodoService = periodoService;
        this.carreraService = carreraService;
        this.patGrupoService = patGrupoService;
    }

    @GetMapping
    public String obtenerListaPats(Model model) {
        model.addAttribute("pats", patService.listarTodos());
        model.addAttribute("periodos", periodoService.listarTodos());
        model.addAttribute("carreras", carreraService.listarTodas());
        return "coordinador/pat-lista";
    }

    @PostMapping("/guardar")
    public String almacenarPat(@ModelAttribute PatInstitucional pat,
                               @RequestParam Integer idPeriodo,
                               @RequestParam Integer idCarrera) {
        try {
            if (patService.existePatParaCarreraEnPeriodo(idPeriodo, idCarrera)) {
                return "redirect:/coordinador/pat?error=duplicado_carrera";
            }

            pat.setPeriodo(periodoService.obtenerPorId(idPeriodo));
            pat.setCarrera(carreraService.obtenerPorId(idCarrera));
            pat.setFechaPublicacion(LocalDate.now());
            
            PatInstitucional patGuardado = patService.guardar(pat);
            patGrupoService.asignarPatAGruposExistentes(patGuardado);
            
            return "redirect:/coordinador/pat?exito=guardado";
            
        } catch (DataIntegrityViolationException dive) {
            return "redirect:/coordinador/pat?error=bd_constraint";
        } catch (Exception e) {
            return "redirect:/coordinador/pat?error=interno";
        }
    }

    @GetMapping("/editar/{idPat}")
    public String prepararModificacionPat(@PathVariable Integer idPat, Model model) {
        model.addAttribute("pat", patService.obtenerPorId(idPat));
        model.addAttribute("periodos", periodoService.listarTodos());
        model.addAttribute("carreras", carreraService.listarTodas());
        return "coordinador/pat-editar";
    }

    @PostMapping("/actualizar")
    public String guardarCambiosPat(@ModelAttribute PatInstitucional pat,
                                    @RequestParam Integer idPeriodo,
                                    @RequestParam Integer idCarrera) {
        try {
            pat.setPeriodo(periodoService.obtenerPorId(idPeriodo));
            pat.setCarrera(carreraService.obtenerPorId(idCarrera));
            
            patService.guardar(pat);
            return "redirect:/coordinador/pat?exito=actualizado";
        } catch (Exception e) {
            return "redirect:/coordinador/pat/editar/" + pat.getIdPat() + "?error=duplicado";
        }
    }

    @GetMapping("/eliminar/{idPat}")
    public String removerPat(@PathVariable Integer idPat) {
        patService.eliminarLogico(idPat); 
        return "redirect:/coordinador/pat?exito=eliminado";
    }

    @GetMapping("/{idPat}/actividades")
    public String obtenerActividades(@PathVariable Integer idPat, Model model) {
        model.addAttribute("pat", patService.obtenerPorId(idPat));
        model.addAttribute("actividades", actividadService.listarPorPat(idPat));
        return "coordinador/pat-detalles";
    }

    @PostMapping("/{idPat}/actividades/guardar")
    public String almacenarActividad(@PathVariable Integer idPat,
                                     @ModelAttribute ActividadPat actividad) {
        
        Integer semana = actividad.getSemanaProgramada();

        if (semana == null) {
            return "redirect:/coordinador/pat/" + idPat + "/actividades?error=semana_invalida";
        }
        if (semana < 1 || semana > 10) {
            return "redirect:/coordinador/pat/" + idPat + "/actividades?error=semana_invalida";
        }
        if (actividadService.existeActividadEnSemana(idPat, semana)) {
            return "redirect:/coordinador/pat/" + idPat + "/actividades?error=semana_ocupada";
        }
        if (actividadService.existeActividadConTitulo(idPat, actividad.getTitulo())) {
            return "redirect:/coordinador/pat/" + idPat + "/actividades?error=titulo_duplicado";
        }

        actividad.setPat(patService.obtenerPorId(idPat));
        actividadService.guardar(actividad);
        return "redirect:/coordinador/pat/" + idPat + "/actividades?exito=actividad_guardada";
    }

    @GetMapping("/{idPat}/actividades/eliminar/{idActividad}")
    public String removerActividad(@PathVariable Integer idPat, @PathVariable Integer idActividad) {
        actividadService.eliminarLogico(idActividad);
        return "redirect:/coordinador/pat/" + idPat + "/actividades?exito=actividad_eliminada";
    }
}
