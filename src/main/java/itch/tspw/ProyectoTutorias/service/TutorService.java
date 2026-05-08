package itch.tspw.ProyectoTutorias.service;

import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TutorService {

    private final TutorRepository tutorRepository;
    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;

    public TutorService(TutorRepository tutorRepository, 
                        UsuarioRepository usuarioRepository, 
                        PerfilRepository perfilRepository, 
                        PasswordEncoder passwordEncoder) {
        this.tutorRepository = tutorRepository;
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Tutor obtenerPorId(Integer idTutor) {
        return tutorRepository.findById(idTutor)
                .orElseThrow(() -> new RuntimeException("Tutor no encontrado con ID: " + idTutor));
    }

    public List<Tutor> listarTodos() {
        return tutorRepository.findAll().stream()
                .filter(t -> t.getUsuario() != null && Boolean.TRUE.equals(t.getUsuario().getActivo()))
                .collect(Collectors.toList());
    }

    public void guardarTutor(Tutor tutor) {
        Usuario usuario = tutor.getUsuario();
        if (usuario.getIdUsuario() == null) {
            configurarNuevoUsuario(usuario, tutor.getRfcEmpleado());
        }
        tutor.setUsuario(usuarioRepository.save(usuario));
        tutorRepository.save(tutor);
    }

    private void configurarNuevoUsuario(Usuario usuario, String rfc) {
        usuario.setPasswordHash(passwordEncoder.encode(rfc));
        usuario.setActivo(true);
        Perfil perfilTutor = perfilRepository.findByNombre("ROLE_TUTOR")
                .orElseThrow(() -> new RuntimeException("Error crítico: El perfil ROLE_TUTOR no existe en la base de datos"));

        if (usuario.getPerfiles() == null) {
            usuario.setPerfiles(new HashSet<>());
        }
        usuario.getPerfiles().add(perfilTutor);
    }

    public void eliminarTutor(Integer idTutor) {
        Tutor tutor = obtenerPorId(idTutor);
        Usuario usuario = tutor.getUsuario();        
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }
}