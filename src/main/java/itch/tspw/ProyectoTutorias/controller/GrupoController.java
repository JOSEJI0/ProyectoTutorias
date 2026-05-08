package itch.tspw.ProyectoTutorias.controller;

import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.repository.*;
import itch.tspw.ProyectoTutorias.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/coordinador/grupos")
public class GrupoController {

    private final PatGrupoService patGrupoService;
    private final GrupoTutoriaRepository grupoRepository;
    private final GrupoTutoriaService grupoTutoriaService;
    private final TutorService tutorService;
    private final CarreraService carreraService;
    private final PeriodoEscolarService periodoService;

    public GrupoController(PatGrupoService patGrupoService, 
                           GrupoTutoriaRepository grupoRepository,
                           GrupoTutoriaService grupoTutoriaService, 
                           TutorService tutorService,
                           CarreraService carreraService, 
                           PeriodoEscolarService periodoService) {
        this.patGrupoService = patGrupoService;
        this.grupoRepository = grupoRepository;
        this.grupoTutoriaService = grupoTutoriaService;
        this.tutorService = tutorService;
        this.carreraService = carreraService;
        this.periodoService = periodoService;
    }

    @GetMapping
    public String obtenerListaGrupos(@RequestParam(defaultValue = "true") boolean activos, Model model) {
        model.addAttribute("grupos", grupoRepository.findByPeriodo_EstatusActivoAndActivoTrue(activos));
        model.addAttribute("mostrandoActivos", activos);
        return "coordinador/grupos-lista";
    }

    @GetMapping("/nuevo")
    public String prepararFormularioCreacion(Model model) {
        model.addAttribute("nuevoGrupo", new GrupoTutoria());
        cargarCatalogos(model);
        return "coordinador/grupos-crear";
    }

    @GetMapping("/editar/{id}")
    public String prepararFormularioEdicion(@PathVariable Integer id, Model model) {
        GrupoTutoria grupo = grupoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado: " + id));
        
        model.addAttribute("nuevoGrupo", grupo); 
        model.addAttribute("esEdicion", true);
        cargarCatalogos(model);
        
        return "coordinador/grupos-crear"; 
    }

    @PostMapping("/guardar")
    public String almacenarGrupo(@ModelAttribute GrupoTutoria grupo) {
        
        if (grupo.getIdGrupo() == null) {
            grupo.setPeriodo(periodoService.obtenerActivo());
            grupo.setActivo(true); 
        } else {
            grupoRepository.findById(grupo.getIdGrupo()).ifPresent(grupoDb -> {
                grupo.setPeriodo(grupoDb.getPeriodo());
                grupo.setActivo(grupoDb.getActivo()); 
                
                if (grupo.getSemestre() == null || grupo.getSemestre() == 0) {
                    grupo.setSemestre(grupoDb.getSemestre());
                }
                if (grupo.getHorario() == null || grupo.getHorario().isEmpty()) {
                    grupo.setHorario(grupoDb.getHorario());
                }
            });
        }
        
        GrupoTutoria grupoGuardado = grupoTutoriaService.asignarGrupo(grupo, null);
        patGrupoService.asignarPatAutomatico(grupoGuardado);
        
        return "redirect:/coordinador/grupos?exito=grupo_actualizado";
    }

    @GetMapping("/eliminar/{id}")
    public String removerGrupo(@PathVariable Integer id) {
        try {
            grupoTutoriaService.eliminarGrupoSeguro(id);
            return "redirect:/coordinador/grupos?exito=grupo_eliminado";
        } catch (Exception e) {
            return "redirect:/coordinador/grupos?error=No_se_pudo_eliminar_el_grupo";
        }
    }

    private void cargarCatalogos(Model model) {
        model.addAttribute("tutores", tutorService.listarTodos());
        model.addAttribute("carreras", carreraService.listarTodas());
    }
}