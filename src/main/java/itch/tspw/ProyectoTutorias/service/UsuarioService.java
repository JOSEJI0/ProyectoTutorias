package itch.tspw.ProyectoTutorias.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import itch.tspw.ProyectoTutorias.model.Usuario;
import itch.tspw.ProyectoTutorias.repository.UsuarioRepository;

import java.util.Optional;
import java.util.Objects;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Usuario autenticarUsuario(String correo, String passwordPlanText) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreoInstitucional(correo);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (Objects.equals(usuario.getPasswordHash(), passwordPlanText)
                    && Boolean.TRUE.equals(usuario.getActivo())) {
                return usuario;
            }
        }
        return null; 
    }
}