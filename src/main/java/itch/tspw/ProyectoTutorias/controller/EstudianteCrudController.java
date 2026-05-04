package itch.tspw.ProyectoTutorias.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
    public String listarEstudiantes(@RequestParam(value = "semestre", required = false) Integer semestre,
                                    @RequestParam(value = "idCarrera", required = false) Integer idCarrera,
                                    Model model) {
        model.addAttribute("estudiantes", estudianteService.listarEstudiantes(semestre, idCarrera));
        model.addAttribute("carreras", carreraService.listarTodas()); 
        model.addAttribute("semestreFiltro", semestre);
        model.addAttribute("carreraFiltro", idCarrera);
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
                                       @RequestParam("semestre") Integer semestre,
                                       @RequestParam("idCarrera") Integer idCarrera,
                                       @RequestParam(value = "activo", defaultValue = "true") Boolean activo) {
        try {
            Estudiante est = estudianteService.obtenerPorId(idEstudiante);
            est.setNumeroControl(numControl);
            est.setSemestreActual(semestre);
            est.setActivo(activo);
            
            Carrera carrera = carreraService.obtenerPorId(idCarrera);
            est.setCarrera(carrera);
            
            Usuario usr = est.getUsuario();
            usr.setNombre(nombre);
            usr.setApellidos(apellidos);
            usr.setCorreoInstitucional(correo);
            
            estudianteService.guardarEstudiante(est);
            return "redirect:/coordinador/estudiantes?exito=actualizado";
        } catch (Exception e) {
            return "redirect:/coordinador/estudiantes/editar/" + idEstudiante + "?error=duplicado";
        }
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarEstudiante(@PathVariable("id") Integer id) {
        try {
            estudianteService.eliminarEstudianteLogico(id);
            return "redirect:/coordinador/estudiantes?exito=eliminado";
        } catch (Exception e) {
            return "redirect:/coordinador/estudiantes?error=no_eliminado";
        }
    }
}