package itch.tspw.ProyectoTutorias.controller;

import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.repository.*;
import itch.tspw.ProyectoTutorias.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/coordinador/grupos")
public class GrupoController {

    private final PatGrupoService patGrupoService;

    @Autowired
    private GrupoTutoriaRepository grupoRepository;

    @Autowired
    private GrupoTutoriaService grupoTutoriaService;

    @Autowired
    private TutorService tutorService;

    @Autowired
    private CarreraService carreraService;

    @Autowired
    private PeriodoEscolarService periodoService;

    GrupoController(PatGrupoService patGrupoService) {
        this.patGrupoService = patGrupoService;
    }

    @GetMapping
    public String obtenerListaGrupos(@RequestParam(value = "activos", defaultValue = "true") boolean activos, Model model) {
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
            GrupoTutoria grupoDb = grupoRepository.findById(grupo.getIdGrupo()).orElseThrow();
            grupo.setPeriodo(grupoDb.getPeriodo());
            grupo.setActivo(grupoDb.getActivo()); 
            
            if(grupo.getSemestre() == null || grupo.getSemestre() == 0) {
                 grupo.setSemestre(grupoDb.getSemestre());
            }
            if(grupo.getHorario() == null || grupo.getHorario().isEmpty()) {
                 grupo.setHorario(grupoDb.getHorario());
            }
        }
        
        // CAMBIO CLAVE: Atrapamos el grupo ya guardado (con su ID generado)
        GrupoTutoria grupoGuardado = grupoTutoriaService.asignarGrupo(grupo, null);
        
        // Le mandamos el grupoGuardado a la lógica del PAT para evitar el error nulo
        patGrupoService.asignarPatAutomatico(grupoGuardado);
        
        return "redirect:/coordinador/grupos?exito=grupo_actualizado";
    }

    @GetMapping("/eliminar/{id}")
    public String removerGrupo(@PathVariable Integer id) {
        try {
            grupoTutoriaService.eliminarGrupoSeguro(id);
            return "redirect:/coordinador/grupos?exito=grupo_eliminado";
        } catch (Exception e) {
            return "redirect:/coordinador/grupos?error=No se pudo eliminar el grupo";
        }
    }

    private void cargarCatalogos(Model model) {
        model.addAttribute("tutores", tutorService.listarTodos());
        model.addAttribute("carreras", carreraService.listarTodas());
    }
}