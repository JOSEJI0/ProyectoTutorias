package itch.tspw.ProyectoTutorias.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import itch.tspw.ProyectoTutorias.model.Carrera;
import itch.tspw.ProyectoTutorias.model.GrupoTutoria;
import itch.tspw.ProyectoTutorias.repository.GrupoTutoriaRepository;
import itch.tspw.ProyectoTutorias.service.CarreraService;
import itch.tspw.ProyectoTutorias.service.GrupoTutoriaService;
import itch.tspw.ProyectoTutorias.service.PatGrupoService;
import itch.tspw.ProyectoTutorias.service.PeriodoEscolarService;
import itch.tspw.ProyectoTutorias.service.TutorService;

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
    public String listarGrupos(@RequestParam(value = "activos", defaultValue = "true") boolean activos, Model model) {
        model.addAttribute("grupos", grupoRepository.findByPeriodo_EstatusActivoAndActivoTrue(activos));
        model.addAttribute("mostrandoActivos", activos);
        return "coordinador/grupos-lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioCreacion(Model model) {
        model.addAttribute("nuevoGrupo", new GrupoTutoria());
        cargarCatalogos(model);
        return "coordinador/grupos-crear";
    }

    // NUEVO: MÉTODO PARA EDITAR GRUPO
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable Integer id, Model model) {
        GrupoTutoria grupo = grupoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado: " + id));
        
        model.addAttribute("nuevoGrupo", grupo); // Usamos el mismo nombre de objeto para reutilizar lógica
        model.addAttribute("esEdicion", true);
        cargarCatalogos(model);
        
        return "coordinador/grupos-crear"; // Reutilizaremos el HTML de creación
    }

    @PostMapping("/guardar")
    public String guardarGrupo(@ModelAttribute GrupoTutoria grupo) {
        Carrera carreraSeleccionada = carreraService.obtenerPorId(grupo.getCarrera().getIdCarrera());
        
        // Regeneramos el nombre por si cambió el semestre, carrera u horario
        if (carreraSeleccionada != null) {
            String nombreGenerado = carreraSeleccionada.getNombreCarrera() + 
                                    " - " + grupo.getSemestre() + "° Semestre " + 
                                    " (" + grupo.getHorario() + ")";
            grupo.setNombreGrupo(nombreGenerado);
        }

        // Si es edición, mantenemos el periodo original; si es nuevo, el activo
        if (grupo.getIdGrupo() == null) {
            grupo.setPeriodo(periodoService.obtenerActivo());
        } else {
            // Recuperamos el periodo actual de la BD para no perderlo
            GrupoTutoria grupoDb = grupoRepository.findById(grupo.getIdGrupo()).get();
            grupo.setPeriodo(grupoDb.getPeriodo());
        }
        patGrupoService.asignarPatAutomatico(grupo);
        grupoTutoriaService.asignarGrupo(grupo, null);
        return "redirect:/coordinador/grupos?exito=grupo_actualizado";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarGrupo(@PathVariable Integer id) {
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