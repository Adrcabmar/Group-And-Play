package com.groupandplay.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Desactiva CSRF para facilitar pruebas
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/api/users/register").permitAll() // Permite el acceso a estas rutas
                .anyRequest().authenticated()
            )
            .formLogin(login -> login.disable()) // Desactiva el formulario de login por defecto
            .httpBasic(httpBasic -> httpBasic.disable()); // Desactiva la autenticación HTTP básica
        
        return http.build();
    }
}
