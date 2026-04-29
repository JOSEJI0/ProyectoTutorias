package itch.tspw.ProyectoTutorias.configuration;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;

@Configuration
public class DataWebSecurity {

    @Bean
    public UserDetailsManager userDetailsManager(DataSource dataSource) {
        JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager(dataSource);

        jdbcUserDetailsManager.setUsersByUsernameQuery(
            "select correo_institucional, password_hash, activo from usuarios where correo_institucional = ?"
        );

        jdbcUserDetailsManager.setAuthoritiesByUsernameQuery(
            "select u.correo_institucional, p.nombre " +
            "from usuarios u " +
            "join usuario_perfil up on u.id_usuario = up.id_usuario " +
            "join perfiles p on up.id_perfil = p.id_perfil " +
            "where u.correo_institucional = ?"
        );

        return jdbcUserDetailsManager;
    }
}