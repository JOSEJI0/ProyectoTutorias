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

    // 1. LISTAR CON FILTRO DE SEMESTRE
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

    // 2. GUARDAR NUEVO ESTUDIANTE
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
            
            // Asignar carrera real desde la base de datos
            Carrera carrera = carreraService.obtenerPorId(idCarrera);
            estudiante.setCarrera(carrera);

            Usuario usuario = new Usuario();
            usuario.setNombre(nombre);
            usuario.setApellidos(apellidos);
            usuario.setCorreoInstitucional(correo);
            // El hash de contraseña y los perfiles se manejan automáticamente en EstudianteService
            
            estudiante.setUsuario(usuario);

            estudianteService.guardarEstudiante(estudiante);
            return "redirect:/coordinador/estudiantes?exito=guardado";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/coordinador/estudiantes?error=duplicado";
        }
    }

    // 3. MOSTRAR FORMULARIO DE EDICIÓN
    @GetMapping("/editar/{id}")
    public String prepararFormularioModificacion(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("estudiante", estudianteService.obtenerPorId(id));
        model.addAttribute("carreras", carreraService.listarTodas()); 
        return "coordinador/estudiantes-editar";
    }

    // 4. ACTUALIZAR ESTUDIANTE EXISTENTE
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
            // Recuperamos los datos que ya existen
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

    // 5. ELIMINAR 
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