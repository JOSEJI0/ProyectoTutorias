package itch.tspw.ProyectoTutorias.service;


import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EstudianteService {

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    public Estudiante obtenerPorId(Integer id) {
        return estudianteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
    }

    @Transactional
    public void guardarEstudiante(Estudiante estudiante) {
        Usuario usuario = estudiante.getUsuario();

        if (usuario.getIdUsuario() == null) {
            String hash = passwordEncoder.encode(estudiante.getNumeroControl());
            usuario.setPasswordHash(hash);
            
            Perfil perfilEstudiante = perfilRepository.findByNombre("ROLE_ESTUDIANTE")
                    .orElseThrow(() -> new RuntimeException("El perfil ROLE_ESTUDIANTE no existe en la BD"));
            
            if(usuario.getPerfiles() == null) {
                usuario.setPerfiles(new java.util.HashSet<>());
            }
            usuario.getPerfiles().add(perfilEstudiante);
            usuario.setActivo(true);
        }

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        estudiante.setUsuario(usuarioGuardado);
        estudiante.setActivo(true);
        estudianteRepository.save(estudiante);
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

    public List<Estudiante> listarSinGrupo() {
        return estudianteRepository.findByGrupoIsNullAndActivoTrue();
    }
}