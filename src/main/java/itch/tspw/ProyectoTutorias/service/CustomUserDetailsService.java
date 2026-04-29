package itch.tspw.ProyectoTutorias.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import itch.tspw.ProyectoTutorias.model.Usuario;
import itch.tspw.ProyectoTutorias.repository.UsuarioRepository;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        // 1. Buscamos el usuario en nuestra tabla 'usuarios'
        Usuario usuario = usuarioRepository.findByCorreoInstitucional(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con correo: " + correo));

        // 2. Extraemos sus perfiles desde la tabla intermedia 'usuario_perfil'
        Collection<GrantedAuthority> authorities = usuario.getPerfiles().stream()
                .map(perfil -> new SimpleGrantedAuthority(perfil.getNombre()))
                .collect(Collectors.toList());

        // 3. Le entregamos a Spring Security los datos oficiales
        return new User(usuario.getCorreoInstitucional(), usuario.getPasswordHash(), authorities);
    }
}