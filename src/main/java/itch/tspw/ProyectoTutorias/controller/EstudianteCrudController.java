package itch.tspw.ProyectoTutorias.controller;

import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/coordinador/estudiantes")
public class EstudianteCrudController {

    private final EstudianteService estudianteService;
    private final CarreraService carreraService;
    private final UploadFileService uploadFileService;

    public EstudianteCrudController(EstudianteService estudianteService, 
                                    CarreraService carreraService, 
                                    UploadFileService uploadFileService) {
        this.estudianteService = estudianteService;
        this.carreraService = carreraService;
        this.uploadFileService = uploadFileService;
    }

    @GetMapping
    public String cargarListaEstudiantes(@RequestParam(required = false) Integer semestre,
                                         @RequestParam(required = false) Integer idCarrera,
                                         Model model) {
        model.addAttribute("estudiantes", estudianteService.listarEstudiantes(semestre, idCarrera));
        model.addAttribute("carreras", carreraService.listarTodas()); 
        model.addAttribute("semestreFiltro", semestre);
        model.addAttribute("carreraFiltro", idCarrera);
        return "coordinador/estudiantes-lista";
    }

    @PostMapping("/guardar")
    public String almacenarEstudiante(@RequestParam("numControl") String numeroControl,
                                      @RequestParam("nombre") String nombre,
                                      @RequestParam("apellidos") String apellidos,
                                      @RequestParam("correo") String correo,
                                      @RequestParam("semestre") Integer semestre,
                                      @RequestParam("idCarrera") Integer idCarrera,
                                      @RequestParam(value = "foto", required = false) MultipartFile foto) {
        try {
            Estudiante existente = estudianteService.buscarPorNumeroControl(numeroControl);

            if (existente != null && existente.getUsuario().getActivo()) {
                return "redirect:/coordinador/estudiantes?error=duplicado";
            }
            
            if (existente != null && !existente.getUsuario().getActivo()) {
                return "redirect:/coordinador/estudiantes?reactivar=" + existente.getIdEstudiante();
            }

            Estudiante estudiante = new Estudiante();
            aplicarDatosFormulario(estudiante, numeroControl, nombre, apellidos, correo, semestre, idCarrera);
            estudiante.getUsuario().setActivo(true);
            
            if (foto != null && !foto.isEmpty()) {
                String nombreFoto = uploadFileService.guardarImagen(foto);
                estudiante.getUsuario().setFotoPerfil(nombreFoto);
            } else {
                estudiante.getUsuario().setFotoPerfil("default.png");
            }
            
            estudianteService.guardarEstudiante(estudiante);
            return "redirect:/coordinador/estudiantes?exito=guardado";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/coordinador/estudiantes?error=duplicado";
        }
    }

    // 3. REACTIVAR ALUMNO
    @GetMapping("/reactivar/{id}")
    public String reactivarEstudiante(@PathVariable Integer id) {
        try {
            Estudiante estudiante = estudianteService.obtenerPorId(id);
            if (estudiante != null) {
                estudiante.getUsuario().setActivo(true);
                estudianteService.guardarEstudiante(estudiante);
            }
            return "redirect:/coordinador/estudiantes?exito=reactivado";
        } catch (Exception e) {
            return "redirect:/coordinador/estudiantes?error=no_reactivado";
        }
    }

    @PostMapping("/actualizar")
    public String guardarCambiosEstudiante(@RequestParam("idEstudiante") Integer idEstudiante,
                                           @RequestParam("numControl") String numeroControl,
                                           @RequestParam("nombre") String nombre,
                                           @RequestParam("apellidos") String apellidos,
                                           @RequestParam("correo") String correo,
                                           @RequestParam("semestre") Integer semestre,
                                           @RequestParam("idCarrera") Integer idCarrera,
                                           @RequestParam(value = "foto", required = false) MultipartFile foto) {
        try {
            Estudiante estudiante = estudianteService.obtenerPorId(idEstudiante);
            aplicarDatosFormulario(estudiante, numeroControl, nombre, apellidos, correo, semestre, idCarrera);
            
            if (foto != null && !foto.isEmpty()) {
                String nombreFoto = uploadFileService.guardarImagen(foto);
                estudiante.getUsuario().setFotoPerfil(nombreFoto);
            }
            
            estudianteService.guardarEstudiante(estudiante);
            return "redirect:/coordinador/estudiantes?exito=actualizado";
        } catch (Exception e) {
            return "redirect:/coordinador/estudiantes/editar/" + idEstudiante + "?error=duplicado";
        }
    }


    private void aplicarDatosFormulario(Estudiante estudiante, String num, String nom, String ape, String mail, Integer sem, Integer idCar) {
        if (estudiante.getUsuario() == null) {
            estudiante.setUsuario(new Usuario());
        }
        estudiante.setNumeroControl(num.trim());
        estudiante.setSemestreActual(sem);
        estudiante.setCarrera(carreraService.obtenerPorId(idCar));
        
        Usuario usuario = estudiante.getUsuario();
        usuario.setNombre(nom.trim());
        usuario.setApellidos(ape.trim());
        usuario.setCorreoInstitucional(mail.trim());
    }

    @GetMapping("/editar/{id}")
    public String prepararFormularioModificacion(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("estudiante", estudianteService.obtenerPorId(id));
        model.addAttribute("carreras", carreraService.listarTodas()); 
        return "coordinador/estudiantes-editar";
    }

    @GetMapping("/eliminar/{id}")
    public String removerEstudiante(@PathVariable("id") Integer id) {
        try {
            estudianteService.eliminarEstudianteLogico(id);
            return "redirect:/coordinador/estudiantes?exito=eliminado";
        } catch (Exception e) {
            return "redirect:/coordinador/estudiantes?error=no_eliminado";
        }
    }
    
    @GetMapping("/detalle/{id}")
    public String verDetalleEstudiante(@PathVariable("id") Integer id, Model model) {
        Estudiante estudiante = estudianteService.obtenerPorId(id);
        model.addAttribute("estudiante", estudiante);
        return "coordinador/estudiantes-detalle";
    }
}