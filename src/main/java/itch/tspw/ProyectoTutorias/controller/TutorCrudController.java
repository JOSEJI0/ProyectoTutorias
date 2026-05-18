package itch.tspw.ProyectoTutorias.controller;

import itch.tspw.ProyectoTutorias.model.Tutor;
import itch.tspw.ProyectoTutorias.model.Usuario;
import itch.tspw.ProyectoTutorias.service.GrupoTutoriaService;
import itch.tspw.ProyectoTutorias.service.TutorService;
import itch.tspw.ProyectoTutorias.service.UploadFileService;
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
    public String guardarTutor(@RequestParam("nombre") String nombre,
                               @RequestParam("apellidos") String apellidos,
                               @RequestParam("correo") String correo,
                               @RequestParam("rfc") String rfc,
                               @RequestParam(value = "foto", required = false) MultipartFile foto) { 
        try {
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setNombre(nombre.trim());
            nuevoUsuario.setApellidos(apellidos.trim());
            nuevoUsuario.setCorreoInstitucional(correo.trim());
            nuevoUsuario.setActivo(true);

            if (foto != null && !foto.isEmpty()) {
                String nombreFoto = uploadFileService.guardarImagen(foto);
                nuevoUsuario.setFotoPerfil(nombreFoto);
            } else {
                nuevoUsuario.setFotoPerfil("default.png"); 
            }

            Tutor nuevoTutor = new Tutor();
            nuevoTutor.setRfcEmpleado(rfc.trim());
            nuevoTutor.setUsuario(nuevoUsuario);

            tutorService.guardarTutor(nuevoTutor);
            return "redirect:/coordinador/tutores?exito=guardado";
        } catch (Exception e) {
            return "redirect:/coordinador/tutores?error=duplicado";
        }
    }

    @PostMapping("/actualizar")
    public String actualizarTutor(@RequestParam("idTutor") Integer idTutor,
                                  @RequestParam("rfc") String rfc,
                                  @RequestParam("nombre") String nombre,
                                  @RequestParam("apellidos") String apellidos,
                                  @RequestParam("correo") String correo,
                                  @RequestParam(value = "foto", required = false) MultipartFile foto) {
        try {
            Tutor tutorExistente = tutorService.obtenerPorId(idTutor);
            tutorExistente.setRfcEmpleado(rfc.trim());
            
            Usuario usuario = tutorExistente.getUsuario();
            usuario.setNombre(nombre.trim());
            usuario.setApellidos(apellidos.trim());
            usuario.setCorreoInstitucional(correo.trim());

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