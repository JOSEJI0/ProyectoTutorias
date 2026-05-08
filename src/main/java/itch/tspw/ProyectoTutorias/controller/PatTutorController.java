package itch.tspw.ProyectoTutorias.controller;

import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.repository.*;
import itch.tspw.ProyectoTutorias.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tutor/pat")
public class PatTutorController {

    private final GrupoTutoriaService grupoService;
    private final PatGrupoService patGrupoService;
    private final PatService patService;
    private final UsuarioRepository usuarioRepository;
    private final TutorRepository tutorRepository;
    private final PatRepository patRepository;
    private final ActividadPatService actividadPatService;

    public PatTutorController(GrupoTutoriaService grupoService, PatGrupoService patGrupoService,
                              PatService patService, UsuarioRepository usuarioRepository,
                              TutorRepository tutorRepository, PatRepository patRepository,
                              ActividadPatService actividadPatService) {
        this.grupoService = grupoService;
        this.patGrupoService = patGrupoService;
        this.patService = patService;
        this.usuarioRepository = usuarioRepository;
        this.tutorRepository = tutorRepository;
        this.patRepository = patRepository;
        this.actividadPatService = actividadPatService;
    }

    private Tutor obtenerTutorLogueado(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByCorreoInstitucional(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return tutorRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Perfil de Tutor no encontrado"));
    }

    @GetMapping
    public String cargarInicioPat(Model model, Authentication authentication) {
        Tutor tutor = obtenerTutorLogueado(authentication);
        model.addAttribute("grupos", grupoService.obtenerGruposActivosPorTutor(tutor.getIdTutor()));
        model.addAttribute("patsInstitucionales", patService.listarTodos());
        return "tutor/pat-inicio";
    }

    @PostMapping("/seleccionar")
    public String seleccionarGrupo(@RequestParam Integer idGrupo) {
        return "redirect:/tutor/pat/grupo/" + idGrupo;
    }

    @GetMapping("/grupo/{idGrupo}")
    public String gestionarPatGrupo(@PathVariable Integer idGrupo, Model model) {
        GrupoTutoria grupo = grupoService.obtenerPorId(idGrupo);
        PatGrupo patGrupo = patGrupoService.obtenerPatDeGrupo(idGrupo);

        model.addAttribute("grupo", grupo);

        patRepository.findFirstByPeriodo_IdPeriodoAndCarrera_IdCarreraAndActivoTrueOrderByIdPatDesc(
                grupo.getPeriodo().getIdPeriodo(), 
                grupo.getCarrera().getIdCarrera()
        ).ifPresentOrElse(
            patInst -> {
                model.addAttribute("patInstitucional", patInst);
                model.addAttribute("actividadesBase", actividadPatService.listarPorPat(patInst.getIdPat()));
            },
            () -> model.addAttribute("errorPatBase", true)
        );

        if (patGrupo != null) {
            model.addAttribute("patGrupo", patGrupo);
            model.addAttribute("actividadesGrupo", patGrupoService.obtenerActividadesDeGrupo(patGrupo.getIdPatGrupo()));
        }

        return "tutor/pat-gestion";
    }

    @PostMapping("/grupo/{idGrupo}/clonar")
    public String clonarPat(@PathVariable Integer idGrupo, @RequestParam Integer idPatInstitucional) {
        try {
            patGrupoService.clonarPatParaGrupo(idGrupo, idPatInstitucional);
            return "redirect:/tutor/pat/grupo/" + idGrupo + "?exito=clonado";
        } catch (Exception e) {
            return "redirect:/tutor/pat/grupo/" + idGrupo + "?error=ya_clonado";
        }
    }

    @GetMapping("/actividad/{id}/editar")
    public String prepararModificacionActividad(@PathVariable("id") Integer idActividad, Model model) {
        ActividadPatGrupo actividad = patGrupoService.obtenerActividadPorId(idActividad);
        model.addAttribute("actividad", actividad);
        model.addAttribute("idGrupo", actividad.getPatGrupo().getGrupo().getIdGrupo()); 
        return "tutor/pat-actividad-editar";
    }

    @PostMapping("/actividad/guardar")
    public String almacenarActividadEditada(@ModelAttribute ActividadPatGrupo actividad, 
                                            @RequestParam Integer idGrupo) {
        
        patGrupoService.actualizarActividadCompleta(
            actividad.getIdActividadGrupo(), 
            actividad.getTitulo(), 
            actividad.getDescripcion(), 
            actividad.getEstatus(), 
            actividad.getSemanaProgramada()
        );
        return "redirect:/tutor/pat/grupo/" + idGrupo + "?exito=actividad_actualizada";
    }

    @PostMapping("/actividad/{id}/eliminar")
    public String removerActividad(@PathVariable("id") Integer idActividad, @RequestParam Integer idGrupo) {
        patGrupoService.eliminarActividad(idActividad);
        return "redirect:/tutor/pat/grupo/" + idGrupo + "?exito=actividad_eliminada";
    }

    @PostMapping("/grupo/{idGrupo}/eliminar-pat")
    public String removerPatGrupo(@PathVariable Integer idGrupo) {
        patGrupoService.eliminarPatDeGrupo(idGrupo);
        return "redirect:/tutor/pat/grupo/" + idGrupo + "?exito=pat_eliminado";
    }
}