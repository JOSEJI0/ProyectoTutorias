package itch.tspw.ProyectoTutorias.service;

import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TutorService {

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Tutor obtenerPorId(Integer idTutor) {
        return tutorRepository.findById(idTutor)
                .orElseThrow(() -> new RuntimeException("Tutor no encontrado"));
    }

    public List<Tutor> listarTodos() {
        return tutorRepository.findAll().stream()
                .filter(tutor -> tutor.getUsuario() != null && tutor.getUsuario().getActivo()) 
                .collect(Collectors.toList());
    }

    @Transactional
    public void guardarTutor(Tutor tutor) {
        Usuario usuario = tutor.getUsuario();
        
        if (usuario.getIdUsuario() == null) {
            String hash = passwordEncoder.encode(tutor.getRfcEmpleado());
            usuario.setPasswordHash(hash);
            
            Perfil perfilTutor = perfilRepository.findByNombre("ROLE_TUTOR")
                    .orElseThrow(() -> new RuntimeException("El perfil ROLE_TUTOR no existe en la BD"));
            
            if(usuario.getPerfiles() == null) {
                usuario.setPerfiles(new java.util.HashSet<>());
            }
            usuario.getPerfiles().add(perfilTutor);
            usuario.setActivo(true);
        }

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        tutor.setUsuario(usuarioGuardado);
        tutorRepository.save(tutor);
    }

    @Transactional
    public void eliminarTutor(Integer idTutor) {
        Tutor tutor = tutorRepository.findById(idTutor)
                .orElseThrow(() -> new RuntimeException("Tutor no encontrado"));
        Usuario usuario = tutor.getUsuario();
        usuario.setActivo(false); 
        usuarioRepository.save(usuario);
    }
}