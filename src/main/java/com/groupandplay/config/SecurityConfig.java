package com.groupandplay.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.groupandplay.config.jwt.services.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsSer;

    @Autowired
    private JWTAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults()) // Habilita CORS
                .authorizeHttpRequests(auth -> auth
                        // Permitir sin autenticación (rutas públicas)
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/users/auth/register",
                                "/api/users/auth/login",
                                "/ws/**",
                                "/api/users/auth/current-user",
                                "/resources/**", "/static/**", "/images/**", "/css/**", "/js/**")
                        .permitAll()

                        // URIS DE ADMIN
                        .requestMatchers(
                                "/api/users/admin/**",
                                "api/groups/admin/**",
                                "/api/games/admin/**")
                        .hasAuthority("ADMIN")

                        // URIS DE USER
                        .requestMatchers(
                                "/api/users/friends/**",
                                "/api/invitations/**",
                                "/api/users/public/**")
                        .hasAnyAuthority("USER", "ADMIN")

                        .requestMatchers(
                                "/api/users/{id:[0-9]+}/**")
                        .hasAnyAuthority("USER", "ADMIN")

                        .requestMatchers(
                                "/api/games/all",
                                "/api/games/find/{gameName}",
                                "/api/groups/my-groups",
                                "/api/groups/open",
                                "/api/groups/**")
                        .hasAnyAuthority("USER", "ADMIN")

                        // URIS DE CHAT
                        .requestMatchers(
                                "/api/chat/**",
                                "/app/**",
                                "/topic/**")
                        .hasAnyAuthority("USER", "ADMIN")

                        // Todo lo demás requiere autenticación
                        .anyRequest().authenticated())
                .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsSer);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}