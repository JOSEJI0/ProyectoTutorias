package itch.tspw.ProyectoTutorias.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Filtro de Rutas y Permisos
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, 
            CustomAuthenticationProvider customAuthenticationProvider,
            CustomSuccessHandler customSuccessHandler) throws Exception {
        
        http.authenticationProvider(customAuthenticationProvider);

        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            )
            .authorizeHttpRequests(configurer ->
                configurer
                        .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        .requestMatchers("/coordinador/**").hasAuthority("ROLE_COORDINADOR")
                        .requestMatchers("/tutor/**").hasAuthority("ROLE_TUTOR")
                        .requestMatchers("/estudiante/**").hasAuthority("ROLE_ESTUDIANTE")
                        .anyRequest().authenticated()
            ) 
            .formLogin(form ->
                form
                    .loginPage("/login")
                    .loginProcessingUrl("/authenticateTheUser")
                    .usernameParameter("correo")
                    .passwordParameter("password")
                    .successHandler(customSuccessHandler) 
                    .permitAll()
            )
            .logout(logout -> logout.permitAll());

        return http.build();
    }
}