package poc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for simplicity (API & H2 console)
            .csrf(csrf -> csrf.disable())
            // Allow H2 console to be displayed in an iframe
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            // Permit access to H2 console and all API endpoints without authentication
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/api/**").permitAll()
                .anyRequest().permitAll()
            );
            // Use HTTP Basic authentication for any other endpoints
            // .httpBasic(Customizer.withDefaults()); // Disabled to avoid browser auth pop‑up
        return http.build();
    }
}
