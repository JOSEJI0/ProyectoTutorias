package itch.tspw.ProyectoTutorias.controller;

import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.repository.*;
import itch.tspw.ProyectoTutorias.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/tutor/pat")
public class PatTutorController {

    @Autowired private GrupoTutoriaService grupoService;
    @Autowired private PatGrupoService patGrupoService;
    @Autowired private PatService patService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private TutorRepository tutorRepository;
    @Autowired private PatRepository patRepository;
    @Autowired private ActividadPatService actividadPatService;

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
    public String seleccionarGrupo(@RequestParam("idGrupo") Integer idGrupo) {
        return "redirect:/tutor/pat/grupo/" + idGrupo;
    }

    @GetMapping("/grupo/{idGrupo}")
    public String gestionarPatGrupo(@PathVariable("idGrupo") Integer idGrupo, Model model) {
        GrupoTutoria grupo = grupoService.obtenerPorId(idGrupo);
        PatGrupo patGrupo = patGrupoService.obtenerPatDeGrupo(idGrupo);

        model.addAttribute("grupo", grupo);

        // CAMBIO CLAVE: Usamos el método blindado que exige Activo=True
        Optional<PatInstitucional> patInstOpt = patRepository.findFirstByPeriodo_IdPeriodoAndCarrera_IdCarreraAndActivoTrueOrderByIdPatDesc(
                grupo.getPeriodo().getIdPeriodo(), 
                grupo.getCarrera().getIdCarrera()
        );

        if (patInstOpt.isPresent()) {
            PatInstitucional patInst = patInstOpt.get();
            model.addAttribute("patInstitucional", patInst);
            model.addAttribute("actividadesBase", actividadPatService.listarPorPat(patInst.getIdPat()));
        } else {
            model.addAttribute("errorPatBase", true);
        }

        if (patGrupo != null) {
            model.addAttribute("patGrupo", patGrupo);
            model.addAttribute("actividadesGrupo", patGrupoService.obtenerActividadesDeGrupo(patGrupo.getIdPatGrupo()));
        }

        return "tutor/pat-gestion";
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

    @GetMapping("/actividad/{id}/editar")
    public String prepararModificacionActividad(@PathVariable("id") Integer idActividad, Model model) {
        ActividadPatGrupo actividad = patGrupoService.obtenerActividadPorId(idActividad);
        model.addAttribute("actividad", actividad);
        model.addAttribute("idGrupo", actividad.getPatGrupo().getGrupo().getIdGrupo()); 
        return "tutor/pat-actividad-editar";
    }

    @PostMapping("/actividad/guardar")
    public String almacenarActividadEditada(@RequestParam("idActividadGrupo") Integer idActividadGrupo,
                                          @RequestParam("idGrupo") Integer idGrupo,
                                          @RequestParam("titulo") String titulo,
                                          @RequestParam("descripcion") String descripcion,
                                          @RequestParam("estatus") String estatus,
                                          @RequestParam("semanaProgramada") Integer semanaProgramada) {
        
        patGrupoService.actualizarActividadCompleta(idActividadGrupo, titulo, descripcion, estatus, semanaProgramada);
        return "redirect:/tutor/pat/grupo/" + idGrupo + "?exito=actividad_actualizada";
    }

    @PostMapping("/actividad/{id}/eliminar")
    public String removerActividad(@PathVariable("id") Integer idActividad, @RequestParam("idGrupo") Integer idGrupo) {
        patGrupoService.eliminarActividad(idActividad);
        return "redirect:/tutor/pat/grupo/" + idGrupo + "?exito=actividad_eliminada";
    }

    @PostMapping("/grupo/{idGrupo}/eliminar-pat")
    public String removerPatGrupo(@PathVariable("idGrupo") Integer idGrupo) {
        patGrupoService.eliminarPatDeGrupo(idGrupo);
        return "redirect:/tutor/pat/grupo/" + idGrupo + "?exito=pat_eliminado";
    }
}