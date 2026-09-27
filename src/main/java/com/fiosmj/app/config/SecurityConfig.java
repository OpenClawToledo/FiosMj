package com.fiosmj.app.config;

import com.fiosmj.app.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Rotas públicas — API
                .requestMatchers(
                    "/api/auth/**",
                    "/api/products/**",
                    "/api/health",
                    "/api/checkout/**",
                    "/api/checkout/webhook",
                    "/api/blog/**",
                    "/api/contact",
                    "/api/stats/**",
                    "/api/presence/**",
                    "/api/newsletter/subscribe",
                    "/api/social/testimonials",
                    "/api/social/**",
                    "/api/newsletter/unsubscribe",
                    "/api/img/**",
                    // Admin — protegido por X-Admin-Secret nos controllers (AdminAuth)
                    "/api/admin/**",
                    "/api/newsletter/admin/**"
                ).permitAll()
                // Recursos estáticos e páginas públicas
                .requestMatchers(
                    "/", "/index.html",
                    "/admin",
                    "/obrigado", "/pendente",
                    "/politica-de-privacidade", "/termos-e-condicoes", "/contacto",
                    "/assets/**", "/img/**", "/uploads/**",
                    "/*.js", "/*.css", "/*.ico", "/*.png", "/*.jpg", "/*.svg",
                    "/sitemap.xml", "/robots.txt", "/favicon.ico"
                ).permitAll()
                // Rotas autenticadas — cliente
                .requestMatchers("/api/cart/**", "/api/orders/**", "/api/customers/**").authenticated()
                // Tudo o resto requer autenticação (fail-safe)
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
