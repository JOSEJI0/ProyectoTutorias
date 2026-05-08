package itch.tspw.ProyectoTutorias.controller;

import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.service.*;
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

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("carreras", carreraService.listarTodas());
        return "coordinador/carreras-lista";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Carrera carrera) {
        try {
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
    public String actualizar(@ModelAttribute Carrera carrera) {
        try {
            carreraService.guardar(carrera);
            return "redirect:/coordinador/carreras?exito=actualizado";
        } catch (DataIntegrityViolationException e) {
            return "redirect:/coordinador/carreras/editar/" + carrera.getIdCarrera() + "?error=duplicado";
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