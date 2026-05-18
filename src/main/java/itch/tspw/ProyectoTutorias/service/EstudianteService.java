package itch.tspw.ProyectoTutorias.service;

import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;
    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;

    public EstudianteService(EstudianteRepository estudianteRepository,
                             UsuarioRepository usuarioRepository,
                             PerfilRepository perfilRepository,
                             PasswordEncoder passwordEncoder) {
        this.estudianteRepository = estudianteRepository;
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Estudiante> listarEstudiantes(Integer semestre, Integer idCarrera) {
        if (semestre != null && idCarrera != null) {
            return estudianteRepository.findBySemestreActualAndCarrera_IdCarreraAndActivoTrue(semestre, idCarrera);
        } else if (semestre != null) {
            return estudianteRepository.findBySemestreActualAndActivoTrue(semestre);
        } else if (idCarrera != null) {
            return estudianteRepository.findByCarrera_IdCarreraAndActivoTrue(idCarrera);
        }
        return estudianteRepository.findByActivoTrue();
    }

    @Transactional(readOnly = true)
    public Estudiante obtenerPorId(Integer id) {
        return estudianteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado con ID: " + id));
    }
    
    @Transactional(readOnly = true)
    public Estudiante buscarPorNumeroControl(String numeroControl) {
        return estudianteRepository.findByNumeroControl(numeroControl).orElse(null);
    }

    @Transactional
    public void guardarEstudiante(Estudiante estudiante) {
        Usuario usuario = estudiante.getUsuario();

        if (usuario.getIdUsuario() == null) {
            configurarNuevoUsuario(usuario, estudiante.getNumeroControl());
        }

        estudiante.setUsuario(usuarioRepository.save(usuario));
        estudiante.setActivo(true);
        estudianteRepository.save(estudiante);
    }

    private void configurarNuevoUsuario(Usuario usuario, String numeroControl) {
        usuario.setPasswordHash(passwordEncoder.encode(numeroControl));
        usuario.setActivo(true);
        
        Perfil perfil = perfilRepository.findByNombre("ROLE_ESTUDIANTE")
                .orElseThrow(() -> new RuntimeException("Error: ROLE_ESTUDIANTE no configurado en BD"));

        if (usuario.getPerfiles() == null) {
            usuario.setPerfiles(new HashSet<>());
        }
        usuario.getPerfiles().add(perfil);
    }

    @Transactional
    public void eliminarEstudianteLogico(Integer idEstudiante) {
        Estudiante estudiante = obtenerPorId(idEstudiante);
        
        estudiante.setGrupo(null); 
        estudiante.setActivo(false);
        
        if (estudiante.getUsuario() != null) {
            estudiante.getUsuario().setActivo(false);
            usuarioRepository.save(estudiante.getUsuario());
        }
        
        estudianteRepository.save(estudiante);
    }

    @Transactional(readOnly = true)
    public List<Estudiante> listarSinGrupo() {
        return estudianteRepository.findByGrupoIsNullAndActivoTrue();
    }
}