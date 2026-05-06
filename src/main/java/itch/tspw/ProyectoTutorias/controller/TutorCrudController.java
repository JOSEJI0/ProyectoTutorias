package itch.tspw.ProyectoTutorias.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.service.*;

@Controller
@RequestMapping("/coordinador/tutores")
public class TutorCrudController {

    @Autowired
    private TutorService tutorService;
    
    @Autowired
    private UploadFileService uploadFileService;

    @Autowired
    private GrupoTutoriaService grupoTutoriaService;

    @GetMapping
    public String listarTutores(Model model) {
        model.addAttribute("tutores", tutorService.listarTodos());
        return "coordinador/tutores-lista";
    }

    @PostMapping("/guardar")
    public String guardarTutor(@RequestParam("nombre") String nombre,
                               @RequestParam("apellidos") String apellidos,
                               @RequestParam("correo") String correo,
                               @RequestParam("rfc") String rfc,
                               @RequestParam(value = "foto", required = false) MultipartFile foto) { 
        try {
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setNombre(nombre);
            nuevoUsuario.setApellidos(apellidos);
            nuevoUsuario.setCorreoInstitucional(correo);
            nuevoUsuario.setPasswordHash("1234");
            nuevoUsuario.setActivo(true);

            if (foto != null && !foto.isEmpty()) {
                String nombreFoto = uploadFileService.guardarImagen(foto);
                nuevoUsuario.setFotoPerfil(nombreFoto);
            } else {
                nuevoUsuario.setFotoPerfil("default.png"); 
            }

            Tutor nuevoTutor = new Tutor();
            nuevoTutor.setRfcEmpleado(rfc);
            nuevoTutor.setUsuario(nuevoUsuario);

            tutorService.guardarTutor(nuevoTutor);
            return "redirect:/coordinador/tutores?exito=guardado";
        } catch (Exception e) {
            return "redirect:/coordinador/tutores?error=duplicado";
        }
    }

    // ACTUALIZACIÓN: Ahora soporta el cambio de foto
    @PostMapping("/actualizar")
    public String actualizarTutor(@RequestParam("idTutor") Integer idTutor,
                                  @RequestParam("rfc") String rfc,
                                  @RequestParam("nombre") String nombre,
                                  @RequestParam("apellidos") String apellidos,
                                  @RequestParam("correo") String correo,
                                  @RequestParam(value = "foto", required = false) MultipartFile foto) {
        try {
            Tutor tutorExistente = tutorService.obtenerPorId(idTutor);
            tutorExistente.setRfcEmpleado(rfc);
            
            Usuario usuario = tutorExistente.getUsuario();
            usuario.setNombre(nombre);
            usuario.setApellidos(apellidos);
            usuario.setCorreoInstitucional(correo);

            // Si se seleccionó una nueva foto, la procesamos
            if (foto != null && !foto.isEmpty()) {
                String nombreFoto = uploadFileService.guardarImagen(foto);
                usuario.setFotoPerfil(nombreFoto);
            }
            
            tutorService.guardarTutor(tutorExistente);
            return "redirect:/coordinador/tutores?exito=actualizado";
            
        } catch (Exception e) {
            return "redirect:/coordinador/tutores/editar/" + idTutor + "?error=duplicado";
        }
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarTutor(@PathVariable("id") Integer id) {
        tutorService.eliminarTutor(id);
        return "redirect:/coordinador/tutores?exito=eliminado";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("tutor", tutorService.obtenerPorId(id));
        return "coordinador/tutores-editar";
    }

    @GetMapping("/detalle/{id}")
    public String verDetalleTutor(@PathVariable("id") Integer id, Model model) {
        Tutor tutor = tutorService.obtenerPorId(id);
        model.addAttribute("tutor", tutor);
        model.addAttribute("grupos", grupoTutoriaService.obtenerGruposActivosPorTutor(id));
        return "coordinador/tutores-detalle";
    }
}