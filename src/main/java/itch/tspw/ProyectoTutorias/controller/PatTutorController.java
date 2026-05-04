package itch.tspw.ProyectoTutorias.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import itch.tspw.ProyectoTutorias.model.GrupoTutoria;
import itch.tspw.ProyectoTutorias.model.PatGrupo;
import itch.tspw.ProyectoTutorias.service.GrupoTutoriaService;
import itch.tspw.ProyectoTutorias.service.PatGrupoService;
import itch.tspw.ProyectoTutorias.service.PatService;

@Controller
@RequestMapping("/tutor/pat")
public class PatTutorController {

    @Autowired
    private GrupoTutoriaService grupoService;

    @Autowired
    private PatGrupoService patGrupoService;

    @Autowired
    private PatService patService;

    @GetMapping("/grupo/{idGrupo}")
    public String gestionarPatGrupo(@PathVariable("idGrupo") Integer idGrupo, Model model) {
        GrupoTutoria grupo = grupoService.obtenerPorId(idGrupo);
        PatGrupo patGrupo = patGrupoService.obtenerPatDeGrupo(idGrupo);

        model.addAttribute("grupo", grupo);

        if (patGrupo == null) {
            model.addAttribute("patsDisponibles", patService.listarTodos());
            return "tutor/pat-clonar"; 
        } 
        else {
            model.addAttribute("patGrupo", patGrupo);
            model.addAttribute("actividades", patGrupoService.obtenerActividadesDeGrupo(patGrupo.getIdPatGrupo()));
            return "tutor/pat-gestion";
        }
    }

    @PostMapping("/grupo/{idGrupo}/clonar")
    public String clonarPat(@PathVariable("idGrupo") Integer idGrupo, 
                            @RequestParam("idPatInstitucional") Integer idPatInstitucional) {
        try {
            patGrupoService.clonarPatParaGrupo(idGrupo, idPatInstitucional);
            return "redirect:/tutor/pat/grupo/" + idGrupo + "?exito=clonado";
        } catch (Exception e) {
            return "redirect:/tutor/pat/grupo/" + idGrupo + "?error=ya_clonado";
        }
    }

    @PostMapping("/actividad/actualizar")
    public String actualizarActividad(@RequestParam("idActividadGrupo") Integer idActividadGrupo,
                                      @RequestParam("idGrupo") Integer idGrupo,
                                      @RequestParam("titulo") String titulo,
                                      @RequestParam("descripcion") String descripcion,
                                      @RequestParam("estatus") String estatus) {
        
        patGrupoService.actualizarActividad(idActividadGrupo, titulo, descripcion, estatus);
        return "redirect:/tutor/pat/grupo/" + idGrupo + "?exito=actividad_actualizada";
    }
}