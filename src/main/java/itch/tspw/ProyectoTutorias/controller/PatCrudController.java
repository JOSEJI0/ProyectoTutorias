package itch.tspw.ProyectoTutorias.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import itch.tspw.ProyectoTutorias.model.ActividadPat;
import itch.tspw.ProyectoTutorias.model.PatInstitucional;
import itch.tspw.ProyectoTutorias.service.ActividadPatService;
import itch.tspw.ProyectoTutorias.service.CarreraService;
import itch.tspw.ProyectoTutorias.service.PatGrupoService;
import itch.tspw.ProyectoTutorias.service.PatService;
import itch.tspw.ProyectoTutorias.service.PeriodoEscolarService;

import java.time.LocalDate;

@Controller
@RequestMapping("/coordinador/pat")
public class PatCrudController {

    @Autowired
    private PatService patService;
    @Autowired
    private ActividadPatService actividadService;
    @Autowired
    private PeriodoEscolarService periodoService;
    @Autowired
    private CarreraService carreraService;
    @Autowired
    private PatGrupoService patGrupoService;

    @GetMapping
    public String listarPats(Model model) {
        model.addAttribute("pats", patService.listarTodos());
        model.addAttribute("periodos", periodoService.listarTodos());
        model.addAttribute("carreras", carreraService.listarTodas());
        return "coordinador/pat-lista";
    }

    @PostMapping("/guardar")
    public String guardarPat(@RequestParam("idPeriodo") Integer idPeriodo,
                             @RequestParam("idCarrera") Integer idCarrera,
                             @RequestParam("version") String version) {
        try {
            if (patService.existePatParaCarreraEnPeriodo(idPeriodo, idCarrera)) {
                return "redirect:/coordinador/pat?error=duplicado_carrera";
            }

            PatInstitucional pat = new PatInstitucional();
            pat.setPeriodo(periodoService.obtenerPorId(idPeriodo));
            pat.setCarrera(carreraService.obtenerPorId(idCarrera));
            pat.setVersion(version);
            pat.setFechaPublicacion(LocalDate.now());
            
            PatInstitucional patGuardado = patService.guardar(pat);
            patGrupoService.asignarPatAGruposExistentes(patGuardado);
            
            return "redirect:/coordinador/pat?exito=guardado";
            
        } catch (org.springframework.dao.DataIntegrityViolationException dive) {
            return "redirect:/coordinador/pat?error=bd_constraint";
        } catch (Exception e) {
            e.printStackTrace(); 
            return "redirect:/coordinador/pat?error=interno";
        }
    }

    @GetMapping("/editar/{idPat}")
    public String editarPat(@PathVariable("idPat") Integer idPat, Model model) {
        model.addAttribute("pat", patService.obtenerPorId(idPat));
        model.addAttribute("periodos", periodoService.listarTodos());
        model.addAttribute("carreras", carreraService.listarTodas());
        return "coordinador/pat-editar";
    }

    @PostMapping("/actualizar")
    public String actualizarPat(@RequestParam("idPat") Integer idPat,
                                @RequestParam("idPeriodo") Integer idPeriodo,
                                @RequestParam("idCarrera") Integer idCarrera,
                                @RequestParam("version") String version) {
        try {
            PatInstitucional pat = patService.obtenerPorId(idPat);
            pat.setPeriodo(periodoService.obtenerPorId(idPeriodo));
            pat.setCarrera(carreraService.obtenerPorId(idCarrera));
            pat.setVersion(version);
            
            patService.guardar(pat);
            return "redirect:/coordinador/pat?exito=actualizado";
        } catch (Exception e) {
            return "redirect:/coordinador/pat/editar/" + idPat + "?error=duplicado";
        }
    }

    @GetMapping("/eliminar/{idPat}")
    public String eliminarPat(@PathVariable("idPat") Integer idPat) {
        patService.eliminarLogico(idPat); 
        return "redirect:/coordinador/pat?exito=eliminado";
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
        
        if (semana < 1 || semana > 10) {
            return "redirect:/coordinador/pat/" + idPat + "/actividades?error=semana_invalida";
        }

        if (actividadService.existeActividadEnSemana(idPat, semana)) {
            return "redirect:/coordinador/pat/" + idPat + "/actividades?error=semana_ocupada";
        }

        if (actividadService.existeActividadConTitulo(idPat, titulo)) {
            return "redirect:/coordinador/pat/" + idPat + "/actividades?error=titulo_duplicado";
        }

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
        actividadService.eliminarLogico(idActividad);
        return "redirect:/coordinador/pat/" + idPat + "/actividades?exito=actividad_eliminada";
    }
}