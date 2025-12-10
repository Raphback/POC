package poc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for simplicity (API & H2 console)
            .csrf(csrf -> csrf.disable())
            // Enable CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Allow H2 console to be displayed in an iframe
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            // Permit access to H2 console and all API endpoints without authentication
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/api/**").permitAll()
                .anyRequest().permitAll()
            );
        // Use HTTP Basic authentication for any other endpoints
        // .httpBasic(Customizer.withDefaults()); // Disabled to avoid browser auth pop-up
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Autoriser localhost:4200 (dev local) et frontend:80 (Docker)
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:4200",
            "http://frontend:80",
            "http://127.0.0.1:4200"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
