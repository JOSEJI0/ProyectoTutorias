package itch.tspw.ProyectoTutorias.controller;

import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/coordinador/tutores")
public class TutorCrudController {

    private final TutorService tutorService;
    private final UploadFileService uploadFileService;
    private final GrupoTutoriaService grupoTutoriaService;

    public TutorCrudController(TutorService tutorService, 
                               UploadFileService uploadFileService, 
                               GrupoTutoriaService grupoTutoriaService) {
        this.tutorService = tutorService;
        this.uploadFileService = uploadFileService;
        this.grupoTutoriaService = grupoTutoriaService;
    }

    @GetMapping
    public String listarTutores(Model model) {
        model.addAttribute("tutores", tutorService.listarTodos());
        return "coordinador/tutores-lista";
    }

    @PostMapping("/guardar")
    public String guardarTutor(@ModelAttribute Tutor tutor, 
                               @RequestParam(value = "foto", required = false) MultipartFile foto) { 
        try {
            if (tutor.getUsuario() == null) {
                tutor.setUsuario(new Usuario());
            }

            if (foto != null && !foto.isEmpty()) {
                String nombreFoto = uploadFileService.guardarImagen(foto);
                tutor.getUsuario().setFotoPerfil(nombreFoto);
            } else {
                tutor.getUsuario().setFotoPerfil("default.png"); 
            }

            tutorService.guardarTutor(tutor);
            
            return "redirect:/coordinador/tutores?exito=guardado";
        } catch (Exception e) {
            e.printStackTrace(); 
            return "redirect:/coordinador/tutores?error=duplicado";
        }
    }

    @PostMapping("/actualizar")
    public String actualizarTutor(@ModelAttribute Tutor tutor,
                                  @RequestParam(value = "foto", required = false) MultipartFile foto) {
        try {
            Tutor tutorExistente = tutorService.obtenerPorId(tutor.getIdTutor());
            
            tutorExistente.setRfcEmpleado(tutor.getRfcEmpleado());
            tutorExistente.getUsuario().setNombre(tutor.getUsuario().getNombre());
            tutorExistente.getUsuario().setApellidos(tutor.getUsuario().getApellidos());
            tutorExistente.getUsuario().setCorreoInstitucional(tutor.getUsuario().getCorreoInstitucional());

            if (foto != null && !foto.isEmpty()) {
                tutorExistente.getUsuario().setFotoPerfil(uploadFileService.guardarImagen(foto));
            }
            
            tutorService.guardarTutor(tutorExistente);
            return "redirect:/coordinador/tutores?exito=actualizado";
            
        } catch (Exception e) {
            return "redirect:/coordinador/tutores/editar/" + tutor.getIdTutor() + "?error=duplicado";
        }
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarTutor(@PathVariable Integer id) {
        tutorService.eliminarTutor(id);
        return "redirect:/coordinador/tutores?exito=eliminado";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Integer id, Model model) {
        model.addAttribute("tutor", tutorService.obtenerPorId(id));
        return "coordinador/tutores-editar";
    }

    @GetMapping("/detalle/{id}")
    public String verDetalleTutor(@PathVariable Integer id, Model model) {
        model.addAttribute("tutor", tutorService.obtenerPorId(id));
        model.addAttribute("grupos", grupoTutoriaService.obtenerGruposActivosPorTutor(id));
        return "coordinador/tutores-detalle";
    }
}