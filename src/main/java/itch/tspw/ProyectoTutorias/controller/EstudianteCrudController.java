package itch.tspw.ProyectoTutorias.controller;

import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/coordinador/estudiantes")
public class EstudianteCrudController {

    private final EstudianteService estudianteService;
    private final CarreraService carreraService;

    public EstudianteCrudController(EstudianteService estudianteService, CarreraService carreraService) {
        this.estudianteService = estudianteService;
        this.carreraService = carreraService;
    }

    @GetMapping
    public String cargarListaEstudiantes(@RequestParam(required = false) Integer semestre,
                                         @RequestParam(required = false) Integer idCarrera,
                                         Model model) {
        model.addAttribute("estudiantes", estudianteService.listarEstudiantes(semestre, idCarrera));
        model.addAttribute("carreras", carreraService.listarTodas()); 
        model.addAttribute("semestreFiltro", semestre);
        model.addAttribute("carreraFiltro", idCarrera);
        return "coordinador/estudiantes-lista";
    }

    @PostMapping("/guardar")
    public String almacenarEstudiante(@RequestParam("numControl") String numeroControl,
                                      @RequestParam("nombre") String nombre,
                                      @RequestParam("apellidos") String apellidos,
                                      @RequestParam("correo") String correo,
                                      @RequestParam("semestre") Integer semestre,
                                      @RequestParam("idCarrera") Integer idCarrera) {
        try {
            Estudiante estudiante = new Estudiante();
            estudiante.setUsuario(new Usuario());
            aplicarDatosFormulario(estudiante, numeroControl, nombre, apellidos, correo, semestre, idCarrera);
            estudianteService.guardarEstudiante(estudiante);
            return "redirect:/coordinador/estudiantes?exito=guardado";
        } catch (Exception e) {
            return "redirect:/coordinador/estudiantes?error=duplicado";
        }
    }

    @GetMapping("/editar/{id}")
    public String prepararFormularioModificacion(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("estudiante", estudianteService.obtenerPorId(id));
        model.addAttribute("carreras", carreraService.listarTodas()); 
        return "coordinador/estudiantes-editar";
    }

    @PostMapping("/actualizar")
    public String guardarCambiosEstudiante(@RequestParam("idEstudiante") Integer idEstudiante,
                                           @RequestParam("numControl") String numeroControl,
                                           @RequestParam("nombre") String nombre,
                                           @RequestParam("apellidos") String apellidos,
                                           @RequestParam("correo") String correo,
                                           @RequestParam("semestre") Integer semestre,
                                           @RequestParam("idCarrera") Integer idCarrera) {
        try {
            Estudiante estudiante = estudianteService.obtenerPorId(idEstudiante);
            aplicarDatosFormulario(estudiante, numeroControl, nombre, apellidos, correo, semestre, idCarrera);
            estudianteService.guardarEstudiante(estudiante);
            return "redirect:/coordinador/estudiantes?exito=actualizado";
        } catch (Exception e) {
            return "redirect:/coordinador/estudiantes/editar/" + idEstudiante + "?error=duplicado";
        }
    }

    private void aplicarDatosFormulario(Estudiante estudiante,
                                        String numeroControl,
                                        String nombre,
                                        String apellidos,
                                        String correo,
                                        Integer semestre,
                                        Integer idCarrera) {
        Usuario usuario = estudiante.getUsuario();
        if (usuario == null) {
            usuario = new Usuario();
            estudiante.setUsuario(usuario);
        }

        estudiante.setNumeroControl(numeroControl.trim());
        estudiante.setSemestreActual(semestre);
        estudiante.setCarrera(carreraService.obtenerPorId(idCarrera));
        usuario.setNombre(nombre.trim());
        usuario.setApellidos(apellidos.trim());
        usuario.setCorreoInstitucional(correo.trim());
    }

    @GetMapping("/eliminar/{id}")
    public String removerEstudiante(@PathVariable("id") Integer id) {
        try {
            estudianteService.eliminarEstudianteLogico(id);
            return "redirect:/coordinador/estudiantes?exito=eliminado";
        } catch (Exception e) {
            return "redirect:/coordinador/estudiantes?error=no_eliminado";
        }
    }
}
