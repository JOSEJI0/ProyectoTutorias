package itch.tspw.ProyectoTutorias.service;

import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.repository.*;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Autentica usuario por correo y contraseña (retorna null si falla)
    public Usuario autenticarUsuario(String correo, String passwordPlanText) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreoInstitucional(correo);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            // Comparación null-safe y comprobación de activo null-safe
            if (Objects.equals(usuario.getPasswordHash(), passwordPlanText)
                    && Boolean.TRUE.equals(usuario.getActivo())) {
                return usuario;
            }
        }
        return null; 
    }
    
    public void registrarUsuario(Usuario usuario) {
        // Encriptamos la clave antes de guardar
        String passwordSegura = passwordEncoder.encode(usuario.getPasswordHash());
        usuario.setPasswordHash(passwordSegura);
        usuarioRepository.save(usuario);
    }
    
    @Transactional
    public boolean cambiarPassword(String correo, String passwordActual, String nuevaPassword) {
        Usuario usuario = usuarioRepository.findByCorreoInstitucional(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String storedHash = usuario.getPasswordHash();
        
        // 1. Verificamos que la contraseña actual sea correcta (Doble chequeo)
        boolean coincide = false;
        try {
            coincide = passwordEncoder.matches(passwordActual, storedHash);
        } catch (Exception e) {}
        
        if (!coincide) {
            coincide = storedHash.equals(passwordActual);
        }

        // Si no coincide, devolvemos false para mostrar error
        if (!coincide) {
            return false;
        }

        // 2. Si es correcta, encriptamos la nueva y la guardamos
        usuario.setPasswordHash(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
        
        return true;
    }
}