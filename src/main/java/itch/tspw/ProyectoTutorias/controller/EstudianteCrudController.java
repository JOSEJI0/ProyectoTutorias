package itch.tspw.ProyectoTutorias.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import itch.tspw.ProyectoTutorias.model.Carrera;
import itch.tspw.ProyectoTutorias.model.Estudiante;
import itch.tspw.ProyectoTutorias.model.Usuario;
import itch.tspw.ProyectoTutorias.service.CarreraService;
import itch.tspw.ProyectoTutorias.service.EstudianteService;

@Controller
@RequestMapping("/coordinador/estudiantes")
public class EstudianteCrudController {

    @Autowired
    private EstudianteService estudianteService;
    @Autowired
    private CarreraService carreraService;

    @GetMapping
    public String listarEstudiantes(Model model) {
        model.addAttribute("estudiantes", estudianteService.listarTodos());
        model.addAttribute("carreras", carreraService.listarTodas()); 
        return "coordinador/estudiantes-lista";
    }

    @PostMapping("/guardar")
    public String guardarEstudiante(@RequestParam("nombre") String nombre,
                                    @RequestParam("apellidos") String apellidos,
                                    @RequestParam("correo") String correo,
                                    @RequestParam("numControl") String numControl,
                                    @RequestParam("semestre") Integer semestre,
                                    @RequestParam("idCarrera") Integer idCarrera) {
        try {
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setNombre(nombre);
            nuevoUsuario.setApellidos(apellidos);
            nuevoUsuario.setCorreoInstitucional(correo);
            nuevoUsuario.setPasswordHash("1234");
            nuevoUsuario.setActivo(true);

            Estudiante nuevoEst = new Estudiante();
            nuevoEst.setNumeroControl(numControl);
            nuevoEst.setSemestreActual(semestre);
            
            Carrera carrera = new Carrera();
            carrera.setIdCarrera(idCarrera);
            nuevoEst.setCarrera(carrera);
            
            nuevoEst.setUsuario(nuevoUsuario);

            estudianteService.guardarEstudiante(nuevoEst);
            return "redirect:/coordinador/estudiantes?exito=guardado";
        } catch (DataIntegrityViolationException e) {
            return "redirect:/coordinador/estudiantes?error=duplicado";
        }
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("estudiante", estudianteService.obtenerPorId(id));
        model.addAttribute("carreras", carreraService.listarTodas()); 
        return "coordinador/estudiantes-editar";
    }

    @PostMapping("/actualizar")
    public String actualizarEstudiante(@RequestParam("idEstudiante") Integer idEstudiante,
                                       @RequestParam("numControl") String numControl,
                                       @RequestParam("nombre") String nombre,
                                       @RequestParam("apellidos") String apellidos,
                                       @RequestParam("correo") String correo,
                                       @RequestParam("semestre") Integer semestre) {
        try {
            Estudiante est = estudianteService.obtenerPorId(idEstudiante);
            est.setNumeroControl(numControl);
            est.setSemestreActual(semestre);
            
            Usuario usr = est.getUsuario();
            usr.setNombre(nombre);
            usr.setApellidos(apellidos);
            usr.setCorreoInstitucional(correo);
            
            estudianteService.guardarEstudiante(est);
            return "redirect:/coordinador/estudiantes?exito=actualizado";
        } catch (DataIntegrityViolationException e) {
            return "redirect:/coordinador/estudiantes/editar/" + idEstudiante + "?error=duplicado";
        }
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarEstudiante(@PathVariable("id") Integer id) {
        estudianteService.eliminarEstudiante(id);
        return "redirect:/coordinador/estudiantes?exito=eliminado";
    }
}