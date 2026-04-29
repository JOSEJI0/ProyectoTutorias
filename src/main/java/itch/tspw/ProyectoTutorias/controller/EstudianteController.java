package itch.tspw.ProyectoTutorias.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import itch.tspw.ProyectoTutorias.service.AsistenciaService;

@Controller
@RequestMapping("/estudiante")
public class EstudianteController {

    @Autowired
    private AsistenciaService asistenciaService;

    @GetMapping("/panel")
    public String mostrarDashboardEstudiante(Model model) {
        return "estudiante/dashboard-estudiante";
    }
    
    @GetMapping("/deteccion-necesidades")
    public String mostrarFormularioNecesidades() {
        return "formulario-necesidades";
    }
}