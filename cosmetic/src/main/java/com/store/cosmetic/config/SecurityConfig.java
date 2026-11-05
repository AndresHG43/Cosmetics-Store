package com.store.cosmetic.config;

import com.store.cosmetic.exception.UnauthorizedException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;

@Configuration
@EnableWebSecurity //@EnableWebSecurity(debug = true): Enable debug mode for development
@EnableMethodSecurity //Allows use @PreAuthorize, @PostAuthorize in methods
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @SuppressWarnings("java:S4502") // CSRF is disabled intentionally: the API is 100% stateless, session creation is disabled, and authentication relies exclusively on Bearer JWTs transmitted in the Authorization header. No ambient credentials or session cookies are used.
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws UnauthorizedException {
        try {
            http
                    .csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .exceptionHandling(exceptions -> exceptions
                            .authenticationEntryPoint((request, response, authException) -> {
                                response.setStatus(401);
                                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                response.getWriter().write(
                                        "{\"success\":false,\"message\":\"Unauthorized: " + authException.getMessage() + "\",\"timestamp\":\"" + LocalDateTime.now(DateTimeConfig.APP_ZONE) + "\"}"
                                );
                            })
                            .accessDeniedHandler((request, response, accessDeniedException) -> {
                                response.setStatus(403);
                                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                response.getWriter().write(
                                        "{\"success\":false,\"message\":\"Access denied: " + accessDeniedException.getMessage() + "\",\"timestamp\":\"" + LocalDateTime.now(DateTimeConfig.APP_ZONE) + "\"}"
                                );
                            })
                    )
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(ApiConfig.API_BASE_PATH + "/auth/**").permitAll()
                            .requestMatchers("/error").permitAll()
                            .requestMatchers(
                                    ApiConfig.API_BASE_PATH + "/users/**",
                                    ApiConfig.API_BASE_PATH + "/roles/**",
                                    ApiConfig.API_BASE_PATH + "/users-roles/**",
                                    ApiConfig.API_BASE_PATH + "/analytics/**"
                            ).hasAnyRole("ROOT", "ADMIN")
                            .requestMatchers(ApiConfig.API_BASE_PATH + "/**").authenticated()
                            .anyRequest().permitAll()
                    )
                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

            return http.build();
        } catch (Exception _) {
            throw new UnauthorizedException("Error in security configuration");
        }

    }
}
