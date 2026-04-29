package itch.tspw.ProyectoTutorias.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import itch.tspw.ProyectoTutorias.model.Estudiante;
import itch.tspw.ProyectoTutorias.model.Perfil;
import itch.tspw.ProyectoTutorias.model.Usuario;
import itch.tspw.ProyectoTutorias.repository.EstudianteRepository;
import itch.tspw.ProyectoTutorias.repository.PerfilRepository;
import itch.tspw.ProyectoTutorias.repository.UsuarioRepository;

import java.util.List;
import java.util.stream.Collectors;

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

    public List<Estudiante> listarTodos() {
        return estudianteRepository.findAll().stream()
                .filter(est -> est.getUsuario() != null && est.getUsuario().getActivo())
                .collect(Collectors.toList());
    }

    public Estudiante obtenerPorId(Integer id) {
        return estudianteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
    }

    @Transactional
    public void guardarEstudiante(Estudiante estudiante) {
        Usuario usuario = estudiante.getUsuario();

        if (usuario.getIdUsuario() == null) {
            String hash = passwordEncoder.encode(usuario.getPasswordHash());
            usuario.setPasswordHash(hash);
            Perfil perfilEstudiante = perfilRepository.findByNombre("ROLE_ESTUDIANTE")
                    .orElseThrow(() -> new RuntimeException("El perfil ROLE_ESTUDIANTE no existe en la BD"));
            usuario.agregarPerfil(perfilEstudiante);
        }

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        estudiante.setUsuario(usuarioGuardado);
        estudianteRepository.save(estudiante);
    }

    @Transactional
    public void eliminarEstudiante(Integer id) {
        Estudiante est = obtenerPorId(id);
        Usuario usuario = est.getUsuario();
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }
}