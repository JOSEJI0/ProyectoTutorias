package itch.tspw.ProyectoTutorias.controller;

import itch.tspw.ProyectoTutorias.model.Carrera;
import itch.tspw.ProyectoTutorias.service.CarreraService;
import itch.tspw.ProyectoTutorias.service.EstudianteService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/coordinador/carreras")
public class CarreraCrudController {

    private final CarreraService carreraService;
    private final EstudianteService estudianteService;

    public CarreraCrudController(CarreraService carreraService, EstudianteService estudianteService) {
        this.carreraService = carreraService;
        this.estudianteService = estudianteService;
    }

    // 1. Listar todas las carreras
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("carreras", carreraService.listarTodas());
        return "coordinador/carreras-lista";
    }

    // 2. Guardar nueva carrera con validación de duplicados
    @PostMapping("/guardar")
    public String guardar(@RequestParam("nombre") String nombre,
                          @RequestParam("clave") String clave) {
        try {
            Carrera carrera = new Carrera();
            carrera.setNombreCarrera(nombre);
            carrera.setClaveOficial(clave);
            carreraService.guardar(carrera);
            return "redirect:/coordinador/carreras?exito=guardado";
        } catch (DataIntegrityViolationException e) {
            return "redirect:/coordinador/carreras?error=duplicado";
        }
    }

    // 3. Mostrar formulario de edición
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("carrera", carreraService.obtenerPorId(id));
        return "coordinador/carreras-editar";
    }

    // 4. actualización de carrera existente
    @PostMapping("/actualizar")
    public String actualizar(@RequestParam("idCarrera") Integer id,
                             @RequestParam("nombre") String nombre,
                             @RequestParam("clave") String clave) {
        try {
            Carrera carrera = carreraService.obtenerPorId(id);
            carrera.setNombreCarrera(nombre);
            carrera.setClaveOficial(clave);
            carreraService.guardar(carrera);
            return "redirect:/coordinador/carreras?exito=actualizado";
        } catch (DataIntegrityViolationException e) {
            return "redirect:/coordinador/carreras/editar/" + id + "?error=duplicado";
        }
    }

    // 5. Eliminar carrera (con protección de integridad referencial)
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable("id") Integer id) {
        try {
            carreraService.eliminar(id);
            return "redirect:/coordinador/carreras?exito=eliminado";
        } catch (Exception e) {
            return "redirect:/coordinador/carreras?error=vinculado";
        }
    }

    // 6. Ver detalle de la carrera y sus alumnos inscritos
    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable("id") Integer id, Model model) {
        Carrera carrera = carreraService.obtenerPorId(id);
        model.addAttribute("carrera", carrera);        
        model.addAttribute("estudiantes", estudianteService.listarEstudiantes(null, id));
        
        return "coordinador/carreras-detalle";
    }
}