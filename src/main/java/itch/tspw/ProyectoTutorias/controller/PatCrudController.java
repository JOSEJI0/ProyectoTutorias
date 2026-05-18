package itch.tspw.ProyectoTutorias.controller;

import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.service.*;
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

    public PatCrudController(PatService patService,
                              ActividadPatService actividadService,
                              PeriodoEscolarService periodoService,
                              CarreraService carreraService,
                              PatGrupoService patGrupoService) {
        this.patService = patService;
        this.actividadService = actividadService;
        this.periodoService = periodoService;
        this.carreraService = carreraService;
        this.patGrupoService = patGrupoService;
    }

    @GetMapping
    public String listarPats(Model model) {
        model.addAttribute("patsGenerales", patService.listarMoldesGenerales());
        model.addAttribute("patsCarrera", patService.listarPatsPorCarrera());
        model.addAttribute("periodos", periodoService.listarTodos());
        model.addAttribute("carreras", carreraService.listarTodas());
        return "coordinador/pat-lista";
    }

    @PostMapping("/guardar")
    public String guardarPat(@RequestParam("idPeriodo") Integer idPeriodo,
                             @RequestParam("version") String version) {
        try {
            if (patService.existeMoldeGeneral(idPeriodo, version)) {
                return "redirect:/coordinador/pat?error=duplicado_molde";
            }

            PatInstitucional pat = new PatInstitucional();
            pat.setPeriodo(periodoService.obtenerPorId(idPeriodo));
            pat.setCarrera(null);
            pat.setVersion(version);
            pat.setFechaPublicacion(LocalDate.now());
            pat.setActivo(true);
            patService.guardar(pat);

            return "redirect:/coordinador/pat?exito=molde_guardado";

        } catch (Exception e) {
            return "redirect:/coordinador/pat?error=interno";
        }
    }

    @PostMapping("/asignar-carrera")
    public String asignarMoldeACarrera(@RequestParam("idPatBase") Integer idPatBase,
                                       @RequestParam("idCarrera") Integer idCarrera) {
        try {
            PatInstitucional patBase = patService.obtenerPorId(idPatBase);

            if (patBase.getCarrera() != null) {
                return "redirect:/coordinador/pat?error=no_es_molde";
            }
            if (patService.existePatParaCarreraEnPeriodo(patBase.getPeriodo().getIdPeriodo(), idCarrera)) {
                return "redirect:/coordinador/pat?error=duplicado_carrera";
            }

            PatInstitucional patCarrera = new PatInstitucional();
            patCarrera.setPeriodo(patBase.getPeriodo());
            patCarrera.setVersion(patBase.getVersion());
            patCarrera.setCarrera(carreraService.obtenerPorId(idCarrera));
            patCarrera.setPatBase(patBase);
            patCarrera.setFechaPublicacion(LocalDate.now());
            patCarrera.setActivo(true);

            PatInstitucional patGuardado = patService.guardar(patCarrera);
            patGrupoService.asignarPatAGruposExistentes(patGuardado);

            return "redirect:/coordinador/pat?exito=asignado_carrera";
        } catch (Exception e) {
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
                                @RequestParam(value = "idCarrera", required = false) Integer idCarrera,
                                @RequestParam("version") String version) {
        try {
            PatInstitucional pat = patService.obtenerPorId(idPat);
            pat.setPeriodo(periodoService.obtenerPorId(idPeriodo));
            pat.setCarrera(idCarrera != null ? carreraService.obtenerPorId(idCarrera) : null);
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
        PatInstitucional pat = patService.obtenerPorId(idPat);
        if (pat.getCarrera() == null) {
            return "redirect:/coordinador/pat?error=molde_sin_temas";
        }
        model.addAttribute("pat", pat);
        model.addAttribute("actividades", actividadService.listarPorPat(idPat));
        return "coordinador/pat-detalles";
    }

    @PostMapping("/{idPat}/actividades/guardar")
    public String guardarActividad(@PathVariable("idPat") Integer idPat,
                                   @RequestParam("titulo") String titulo,
                                   @RequestParam("descripcion") String descripcion,
                                   @RequestParam("semanaProgramada") Integer semana) {
        
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
        patGrupoService.sincronizarPatConGruposExistentes(pat);
        return "redirect:/coordinador/pat/" + idPat + "/actividades?exito=actividad_guardada";
    }

    @GetMapping("/{idPat}/actividades/eliminar/{idActividad}")
    public String eliminarActividad(@PathVariable("idPat") Integer idPat,
                                    @PathVariable("idActividad") Integer idActividad) {
        actividadService.eliminarLogico(idActividad);
        return "redirect:/coordinador/pat/" + idPat + "/actividades?exito=actividad_eliminada";
    }
}
