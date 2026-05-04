package itch.tspw.ProyectoTutorias.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import itch.tspw.ProyectoTutorias.model.Carrera;
import itch.tspw.ProyectoTutorias.service.CarreraService;
import itch.tspw.ProyectoTutorias.service.EstudianteService;

@Controller
@RequestMapping("/coordinador/carreras")
public class CarreraCrudController {

    @Autowired
    private CarreraService carreraService;

    @Autowired
    private EstudianteService estudianteService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("carreras", carreraService.listarTodas());
        return "coordinador/carreras-lista";
    }

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

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("carrera", carreraService.obtenerPorId(id));
        return "coordinador/carreras-editar";
    }

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

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable("id") Integer id) {
        try {
            carreraService.eliminar(id);
            return "redirect:/coordinador/carreras?exito=eliminado";
        } catch (Exception e) {
            return "redirect:/coordinador/carreras?error=vinculado";
        }
    }

    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable("id") Integer id, Model model) {
        Carrera carrera = carreraService.obtenerPorId(id);
        model.addAttribute("carrera", carrera);
        
        model.addAttribute("estudiantes", estudianteService.listarEstudiantes(null, id));
        
        return "coordinador/carreras-detalle";
    }
}