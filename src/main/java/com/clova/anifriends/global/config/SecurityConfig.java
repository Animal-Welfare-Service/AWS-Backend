package com.clova.anifriends.global.config;

import com.clova.anifriends.domain.auth.authentication.JwtAuthenticationProvider;
import com.clova.anifriends.domain.common.CustomPasswordEncoder;
import com.clova.anifriends.global.security.passwordencoder.BCryptCustomPasswordEncoder;
import com.clova.anifriends.global.web.filter.JwtAuthenticationFilter;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ROLE_SHELTER = "SHELTER";
    private static final String ROLE_VOLUNTEER = "VOLUNTEER";

    private final String frontServer;

    public SecurityConfig(@Value("${front.server}") String frontServer) {
        this.frontServer = frontServer;
    }

    @Bean
    public CustomPasswordEncoder customPasswordEncoder() {
        return new BCryptCustomPasswordEncoder(new BCryptPasswordEncoder());
    }

    @Bean
    public SecurityFilterChain filterChain(
        HttpSecurity http,
        JwtAuthenticationProvider jwtAuthenticationProvider) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .rememberMe(AbstractHttpConfigurer::disable)
            .anonymous(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterAfter(new JwtAuthenticationFilter(jwtAuthenticationProvider),
                SecurityContextHolderFilter.class)
            .headers(header -> header.frameOptions(
                FrameOptionsConfig::disable))
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(
                    List.of("http://localhost:5173", "http://localhost:5174", frontServer));
                config.setAllowedMethods(
                    List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                config.setAllowCredentials(true);
                config.setAllowedHeaders(List.of("*"));
                config.setMaxAge(3600L);
                return config;
            }))
            .authorizeHttpRequests(request ->
                request
                    .requestMatchers(HttpMethod.GET, "/api/shelters/me/**").hasRole(ROLE_SHELTER)
                    .requestMatchers(HttpMethod.GET, "/api/shelters/*/profile/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/shelters/*/reviews").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/volunteers/*/recruitments/completed")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/shelters/*/recruitments").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/volunteers/*/reviews").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/volunteers/email").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/shelters/email").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/volunteers").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/shelters").permitAll()
                    .requestMatchers("/api/shelters/**").hasRole(ROLE_SHELTER)
                    .requestMatchers("/api/volunteers/**").hasRole(ROLE_VOLUNTEER)
                    .requestMatchers("/**").permitAll());
        return http.build();
    }
}
