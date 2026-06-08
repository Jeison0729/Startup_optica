package oft.optica.config;

import lombok.RequiredArgsConstructor;
import oft.optica.security.auth.UsuarioDetailsServiceImpl;
import oft.optica.security.jwt.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final UsuarioDetailsServiceImpl userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;
    private final CorsConfig corsConfig;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http.csrf(
                        csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(
                        corsConfig.corsConfigurationSource())).authenticationProvider(authenticationProvider()).authorizeHttpRequests(auth -> auth

                        // ── RUTAS PÚBLICAS ────────────────────────────────────
                        .requestMatchers("/auth/login").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/solicitudes/solicitar").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/solicitudes/restablecer").permitAll()


                        // ── CATÁLOGOS (solo GET, ambos roles) ────────────────
                        .requestMatchers(HttpMethod.GET, "/api/catalogos/**").hasAnyAuthority("ROLE_DEV", "ROLE_ADMIN")

                        // ── SOLICITUDES DE RECUPERACIÓN ──────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/solicitudes").hasAnyAuthority("ROLE_DEV", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/solicitudes/pendientes").hasAnyAuthority("ROLE_DEV", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/solicitudes/*/aprobar").hasAnyAuthority("ROLE_DEV", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/solicitudes/*/reenviar").hasAnyAuthority("ROLE_DEV", "ROLE_ADMIN")

                        // ── USUARIOS ─────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/**").hasAnyAuthority("ROLE_DEV", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/usuarios").hasAuthority("ROLE_DEV")
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/**").hasAuthority("ROLE_DEV")
                        .requestMatchers(HttpMethod.PATCH, "/api/usuarios/*/reactivar").hasAuthority("ROLE_DEV")
                        .requestMatchers(HttpMethod.PATCH, "/api/usuarios/*/eliminar").hasAuthority("ROLE_DEV")
                        .requestMatchers(HttpMethod.PATCH, "/api/usuarios/**").hasAnyAuthority("ROLE_DEV", "ROLE_ADMIN")

                        // ── ROLES ─────────────────────────────────────────────
                        .requestMatchers("/api/roles/**").hasAuthority("ROLE_DEV")

                        // ── USUARIOS-ROLES ────────────────────────────────────
                        .requestMatchers("/api/usuarios-roles/**").hasAuthority("ROLE_DEV")

                        // ── PACIENTES ─────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/pacientes/**").hasAnyAuthority("ROLE_DEV", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/pacientes").hasAnyAuthority("ROLE_DEV", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/pacientes/**").hasAnyAuthority("ROLE_DEV", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/pacientes/**").hasAnyAuthority("ROLE_DEV", "ROLE_ADMIN")

                        // ── CONSULTAS ─────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/consultas/**").hasAnyAuthority("ROLE_DEV", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/consultas").hasAnyAuthority("ROLE_DEV", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/consultas/**").hasAnyAuthority("ROLE_DEV", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/consultas/**").hasAnyAuthority("ROLE_DEV", "ROLE_ADMIN")

                        // ── MEDICIONES ────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/mediciones/**").hasAnyAuthority("ROLE_DEV", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/mediciones").hasAnyAuthority("ROLE_DEV", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/mediciones/**").hasAnyAuthority("ROLE_DEV", "ROLE_ADMIN")

                        // ── ACOMPAÑANTES ──────────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/acompanantes/**").hasAnyAuthority("ROLE_DEV", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/acompanantes").hasAnyAuthority("ROLE_DEV", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/acompanantes/**").hasAnyAuthority("ROLE_DEV", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/acompanantes/**").hasAnyAuthority("ROLE_DEV", "ROLE_ADMIN")

                        // ── ARCHIVOS ADJUNTOS ─────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/adjuntos/**").hasAnyAuthority("ROLE_DEV", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/adjuntos").hasAnyAuthority("ROLE_DEV", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/adjuntos/**").hasAnyAuthority("ROLE_DEV", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/adjuntos/**").hasAnyAuthority("ROLE_DEV", "ROLE_ADMIN")

                        // ── AUDITORÍA ─────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/logs/**").hasAuthority("ROLE_DEV")

                        // ── RESTO ─────────────────────────────────────────────
                        .anyRequest().authenticated())

                .sessionManagement(session
                        -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .addFilterBefore(jwtAuthFilter, org.springframework.security.web.authentication
                        .UsernamePasswordAuthenticationFilter.class).build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
