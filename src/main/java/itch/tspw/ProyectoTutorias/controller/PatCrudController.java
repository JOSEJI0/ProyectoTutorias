package itch.tspw.ProyectoTutorias.controller;

import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    public String obtenerListaPats(Model model) {
        model.addAttribute("pats", patService.listarTodos());
        model.addAttribute("periodos", periodoService.listarTodos());
        model.addAttribute("carreras", carreraService.listarTodas());
        return "coordinador/pat-lista";
    }

    @PostMapping("/guardar")
    public String almacenarPat(@RequestParam("idPeriodo") Integer idPeriodo,
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

    // NUEVO: Mostrar formulario de edición
    @GetMapping("/editar/{idPat}")
    public String prepararModificacionPat(@PathVariable("idPat") Integer idPat, Model model) {
        model.addAttribute("pat", patService.obtenerPorId(idPat));
        model.addAttribute("periodos", periodoService.listarTodos());
        model.addAttribute("carreras", carreraService.listarTodas());
        return "coordinador/pat-editar";
    }

    // NUEVO: Procesar la actualización
    @PostMapping("/actualizar")
    public String guardarCambiosPat(@RequestParam("idPat") Integer idPat,
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
    public String removerPat(@PathVariable("idPat") Integer idPat) {
        patService.eliminarLogico(idPat); 
        return "redirect:/coordinador/pat?exito=eliminado";
    }

    // ==========================================
    // 2. GESTIÓN DE ACTIVIDADES (DETALLE DEL MOLDE)
    // ==========================================
    @GetMapping("/{idPat}/actividades")
    public String obtenerActividades(@PathVariable("idPat") Integer idPat, Model model) {
        model.addAttribute("pat", patService.obtenerPorId(idPat));
        model.addAttribute("actividades", actividadService.listarPorPat(idPat));
        return "coordinador/pat-detalles";
    }

    @PostMapping("/{idPat}/actividades/guardar")
    public String almacenarActividad(@PathVariable("idPat") Integer idPat,
                                   @RequestParam("titulo") String titulo,
                                   @RequestParam("descripcion") String descripcion,
                                   @RequestParam("semana") Integer semana) {
        
        // 1. Validar rango de semanas
        if (semana < 1 || semana > 10) {
            return "redirect:/coordinador/pat/" + idPat + "/actividades?error=semana_invalida";
        }

        // 2. Validar que la semana no esté ocupada
        if (actividadService.existeActividadEnSemana(idPat, semana)) {
            return "redirect:/coordinador/pat/" + idPat + "/actividades?error=semana_ocupada";
        }

        // 3. Validar que el título no esté duplicado
        if (actividadService.existeActividadConTitulo(idPat, titulo)) {
            return "redirect:/coordinador/pat/" + idPat + "/actividades?error=titulo_duplicado";
        }

        // Si pasa las validaciones, guardamos
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
    public String removerActividad(@PathVariable("idPat") Integer idPat, 
                                    @PathVariable("idActividad") Integer idActividad) {
        actividadService.eliminarLogico(idActividad);
        return "redirect:/coordinador/pat/" + idPat + "/actividades?exito=actividad_eliminada";
    }
}