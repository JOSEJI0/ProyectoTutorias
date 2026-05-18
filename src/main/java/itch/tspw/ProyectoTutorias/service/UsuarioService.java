package itch.tspw.ProyectoTutorias.service;

import itch.tspw.ProyectoTutorias.model.Usuario;
import itch.tspw.ProyectoTutorias.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Usuario autenticarUsuario(String correo, String passwordPlainText) {
        return usuarioRepository.findByCorreoInstitucional(correo)
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .filter(u -> passwordEncoder.matches(passwordPlainText, u.getPasswordHash()))
                .orElse(null);
    }
    
    @Transactional
    public void registrarUsuario(Usuario usuario) {
        String passwordSegura = passwordEncoder.encode(usuario.getPasswordHash());
        usuario.setPasswordHash(passwordSegura);
        usuarioRepository.save(usuario);
    }
    
    @Transactional
    public boolean cambiarPassword(String correo, String passwordActual, String nuevaPassword) {
        return usuarioRepository.findByCorreoInstitucional(correo)
                .map(usuario -> {
                    String storedHash = usuario.getPasswordHash();
                    boolean coincide = false;

                    try {
                        coincide = passwordEncoder.matches(passwordActual, storedHash);
                    } catch (Exception e) {
                    }
                    
                    if (!coincide) {
                        coincide = storedHash.equals(passwordActual);
                    }

                    if (coincide) {
                        usuario.setPasswordHash(passwordEncoder.encode(nuevaPassword));
                        usuarioRepository.save(usuario);
                        return true;
                    }
                    
                    return false;
                }).orElse(false);
    }
}