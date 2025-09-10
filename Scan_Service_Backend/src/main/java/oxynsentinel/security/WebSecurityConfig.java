package oxynsentinel.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class WebSecurityConfig {

    private final KeycloakTokenFilter keycloakTokenFilter;

    public WebSecurityConfig(KeycloakTokenFilter keycloakTokenFilter) {
        this.keycloakTokenFilter = keycloakTokenFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/oxynsentinel/websocket-test.html", "/oxynsentinel/ws/**").permitAll()
                        .requestMatchers("/oxynsentinel/**.js", "/oxynsentinel/**.css").permitAll()
                        .requestMatchers("/api/**").hasRole("ADMIN")
                        // ✅ Restrict by role
                        .anyRequest().permitAll()
                );

        http.addFilterBefore(keycloakTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
