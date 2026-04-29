package itch.tspw.ProyectoTutorias.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import itch.tspw.ProyectoTutorias.service.EvidenciaService;
import itch.tspw.ProyectoTutorias.service.GrupoTutoriaService;
import itch.tspw.ProyectoTutorias.service.SesionService;

import java.time.LocalDate;

@Controller
@RequestMapping("/coordinador")
public class CoordinadorController {

    @Autowired
    private EvidenciaService evidenciaService;

    @Autowired
    private GrupoTutoriaService grupoTutoriaService;

    @Autowired
    private SesionService sesionService;

    @GetMapping("/panel")
    public String mostrarDashboardCoordinador(Model model) {
        model.addAttribute("evidenciasPendientes", evidenciaService.obtenerEvidenciasPendientes());
        model.addAttribute("totalTutores", 15);
        model.addAttribute("alertasAcademicas", 4);
        return "coordinador/dashboard-coordinador";
    }

    @GetMapping("/busquedas")
    public String mostrarCentroDeBusquedas() {
        return "coordinador/busquedas";
    }

    @GetMapping("/buscar-tutores")
    public String buscarTutoresPorSemestre(@RequestParam("idPeriodo") Integer idPeriodo, Model model) {
        model.addAttribute("resultados", grupoTutoriaService.buscarTutoriasPorPeriodo(idPeriodo));
        model.addAttribute("periodoId", idPeriodo);
        return "coordinador/resultados-tutores";
    }

    @GetMapping("/buscar-historial-alumno")
    public String buscarHistorialAlumno(@RequestParam("idEstudiante") Integer idEstudiante, Model model) {
        model.addAttribute("resultados", grupoTutoriaService.buscarHistorialTutoriasDeEstudiante(idEstudiante));
        model.addAttribute("estudianteId", idEstudiante);
        return "coordinador/resultados-historial";
    }

    @GetMapping("/buscar-actividades")
    public String buscarActividadesPorFecha(@RequestParam("fecha") LocalDate fecha, Model model) {
        model.addAttribute("resultados", sesionService.buscarActividadesPorFecha(fecha));
        model.addAttribute("fechaBusqueda", fecha);
        return "coordinador/resultados-actividades";
    }

    @PostMapping("/validar-evidencia")
    public String validarEvidencia(@RequestParam("idEvidencia") Integer idEvidencia,
                                   @RequestParam("estatus") String estatus,
                                   @RequestParam(value = "notas", required = false) String notas) {
        evidenciaService.validarEvidencia(idEvidencia, estatus, notas);
        return "redirect:/coordinador/panel?exito=evidencia_validada";
    }
}