package itch.tspw.ProyectoTutorias.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import itch.tspw.ProyectoTutorias.model.Perfil;
import itch.tspw.ProyectoTutorias.model.Usuario;
import itch.tspw.ProyectoTutorias.repository.UsuarioRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String correo = (authentication.getPrincipal() == null) ? "" : authentication.getName();
        String password = (authentication.getCredentials() == null) ? "" : authentication.getCredentials().toString();

        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreoInstitucional(correo);
        
        if (usuarioOpt.isEmpty()) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        Usuario usuario = usuarioOpt.get();

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new BadCredentialsException("Usuario inactivo");
        }

        String storedHash = usuario.getPasswordHash();
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        
        boolean matches = false;
        try {
            matches = encoder.matches(password, storedHash);
        } catch (Exception e) {}
        
        if (!matches) {
            matches = storedHash.equals(password);
        }

        if (!matches) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        Set<Perfil> perfiles = usuario.getPerfiles();
        
        if (perfiles != null) {
            for (Perfil perfil : perfiles) {
                authorities.add(new SimpleGrantedAuthority(perfil.getNombre()));
            }
        }

        if (authorities.isEmpty()) {
            throw new BadCredentialsException("El usuario no tiene ningún rol asignado");
        }

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(correo, null, authorities);
        token.setDetails(usuario);
        return token;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}