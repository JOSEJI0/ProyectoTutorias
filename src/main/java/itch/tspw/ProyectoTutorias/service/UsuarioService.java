package itch.tspw.ProyectoTutorias.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import itch.tspw.ProyectoTutorias.model.Usuario;
import itch.tspw.ProyectoTutorias.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario autenticarUsuario(String correo, String passwordPlainText) {
        return usuarioRepository.findByCorreoInstitucional(correo)
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .filter(u -> passwordEncoder.matches(passwordPlainText, u.getPasswordHash()))
                .orElse(null);
    }
    
    @Transactional
    public void registrarUsuario(Usuario usuario) {
        usuario.setPasswordHash(passwordEncoder.encode(usuario.getPasswordHash()));
        usuarioRepository.save(usuario);
    }
    
    @Transactional
    public boolean cambiarPassword(String correo, String passwordActual, String nuevaPassword) {
        return usuarioRepository.findByCorreoInstitucional(correo)
                .map(usuario -> {
                    if (passwordEncoder.matches(passwordActual, usuario.getPasswordHash())) {
                        usuario.setPasswordHash(passwordEncoder.encode(nuevaPassword));
                        usuarioRepository.save(usuario);
                        return true;
                    }
                    return false;
                }).orElse(false);
    }
}