package itch.tspw.ProyectoTutorias.controller;

import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/coordinador/estudiantes")
public class EstudianteCrudController {

    @Autowired
    private EstudianteService estudianteService;
    
    @Autowired
    private CarreraService carreraService;

    @GetMapping
    public String cargarListaEstudiantes(@RequestParam(value = "semestre", required = false) Integer semestre,
                                         @RequestParam(value = "idCarrera", required = false) Integer idCarrera,
                                         Model model) {
        model.addAttribute("estudiantes", estudianteService.listarEstudiantes(semestre, idCarrera));
        model.addAttribute("carreras", carreraService.listarTodas()); 
        model.addAttribute("semestreFiltro", semestre);
        model.addAttribute("carreraFiltro", idCarrera);
        return "coordinador/estudiantes-lista";
    }

    @PostMapping("/guardar")
    public String almacenarEstudiante(@RequestParam("nombre") String nombre,
                                    @RequestParam("apellidos") String apellidos,
                                    @RequestParam("correo") String correo,
                                    @RequestParam("numControl") String numControl,
                                    @RequestParam("semestre") Integer semestre,
                                    @RequestParam("idCarrera") Integer idCarrera) {
        try {
            Estudiante estudiante = new Estudiante();
            estudiante.setNumeroControl(numControl);
            estudiante.setSemestreActual(semestre);
            Carrera carrera = carreraService.obtenerPorId(idCarrera);
            estudiante.setCarrera(carrera);
            Usuario usuario = new Usuario();
            usuario.setNombre(nombre);
            usuario.setApellidos(apellidos);
            usuario.setCorreoInstitucional(correo);
            estudiante.setUsuario(usuario);
            estudianteService.guardarEstudiante(estudiante);
            return "redirect:/coordinador/estudiantes?exito=guardado";
        } catch (Exception e) {
            e.printStackTrace();
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
                                       @RequestParam("numControl") String numControl,
                                       @RequestParam("nombre") String nombre,
                                       @RequestParam("apellidos") String apellidos,
                                       @RequestParam("correo") String correo,
                                       @RequestParam("semestre") Integer semestre,
                                       @RequestParam("idCarrera") Integer idCarrera,
                                       @RequestParam(value = "activo", defaultValue = "true") Boolean activo) {
        try {
            Estudiante estudiante = estudianteService.obtenerPorId(idEstudiante);
            estudiante.setNumeroControl(numControl);
            estudiante.setSemestreActual(semestre);
            estudiante.setActivo(activo);
            Carrera carrera = carreraService.obtenerPorId(idCarrera);
            estudiante.setCarrera(carrera);
            Usuario usuario = estudiante.getUsuario();
            usuario.setNombre(nombre);
            usuario.setApellidos(apellidos);
            usuario.setCorreoInstitucional(correo);
            estudianteService.guardarEstudiante(estudiante);
            return "redirect:/coordinador/estudiantes?exito=actualizado";
        } catch (Exception e) {
            return "redirect:/coordinador/estudiantes/editar/" + idEstudiante + "?error=duplicado";
        }
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