package com.example.gym.config;

import com.example.gym.service.GymUserDetailsService;
import com.example.gym.web.JwtLogoutHandler;
import com.example.gym.web.RestAccessDeniedHandler;
import com.example.gym.web.RestAuthenticationEntryPoint;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationProvider authenticationProvider,
            JwtDecoder jwtDecoder,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            CorsConfigurationSource corsConfigurationSource,
            JwtLogoutHandler jwtLogoutHandler,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler
    ) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource
                        )
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authenticationProvider(
                        authenticationProvider
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/login",
                                "/api/trainees/registration",
                                "/api/trainers/registration"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/actuator/health"
                        ).permitAll()

                        .requestMatchers(
                                "/api/trainees/**"
                        ).hasRole("TRAINEE")

                        .requestMatchers(
                                "/api/trainers/**"
                        ).hasRole("TRAINER")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/trainee-trainings"
                        ).hasRole("TRAINEE")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/trainer-trainings"
                        ).hasRole("TRAINER")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/trainings"
                        ).hasRole("TRAINER")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/training-types"
                        ).hasAnyRole(
                                "TRAINEE",
                                "TRAINER"
                        )

                        .anyRequest().authenticated()
                )

                .exceptionHandling(exceptions ->
                        exceptions
                                .authenticationEntryPoint(
                                        authenticationEntryPoint
                                )
                                .accessDeniedHandler(
                                        accessDeniedHandler
                                )
                )

                .oauth2ResourceServer(resourceServer ->
                        resourceServer
                                .authenticationEntryPoint(
                                        authenticationEntryPoint
                                )
                                .accessDeniedHandler(
                                        accessDeniedHandler
                                )
                                .jwt(jwt ->
                                        jwt
                                                .decoder(jwtDecoder)
                                                .jwtAuthenticationConverter(
                                                        jwtAuthenticationConverter
                                                )
                                )
                )

                .logout(logout ->
                        logout
                                .logoutUrl("/api/logout")
                                .addLogoutHandler(
                                        jwtLogoutHandler
                                )
                                .logoutSuccessHandler(
                                        (
                                                request,
                                                response,
                                                authentication
                                        ) -> {
                                            if (response.getStatus()
                                                    == HttpServletResponse
                                                    .SC_OK) {
                                                response.setStatus(
                                                        HttpServletResponse
                                                                .SC_NO_CONTENT
                                                );
                                            }
                                        }
                                )
                )

                .formLogin(
                        AbstractHttpConfigurer::disable
                )

                .httpBasic(
                        AbstractHttpConfigurer::disable
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            GymUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(
                        userDetailsService
                );

        provider.setPasswordEncoder(
                passwordEncoder
        );

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration
                .getAuthenticationManager();
    }

    @Bean
    public BearerTokenResolver bearerTokenResolver() {
        return new DefaultBearerTokenResolver();
    }
}