package com.godown.storage.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Map the signed role claim to Spring authorities such as ROLE_CUSTOMER.
        var authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("role");
        authorities.setAuthorityPrefix("ROLE_");

        var authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(authorities);

        return http
                // Clients authenticate with a Bearer header on each protected request.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint()))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/me").hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/storage-units").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/storage-units/available")
                        .hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/bookings").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/api/bookings").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.PATCH, "/api/bookings/*/close").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/api/bookings/*/items").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/api/bookings/*/items").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/api/bookings/*/invoices").hasRole("ADMIN")
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/bookings/*/invoices",
                                "/api/bookings/*/invoices/*"
                        ).hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.PATCH, "/api/bookings/*/invoices/*/pay")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/bookings/*/schedule")
                        .hasRole("CUSTOMER")
                        .anyRequest().denyAll())
                .oauth2ResourceServer(server -> server
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(authenticationConverter)))
                .build();
    }

    @Bean
    JwtDecoder jwtDecoder(@Value("${jwt.secret}") String secret) {
        // Use the same Base64-decoded signing key and HS256 algorithm as LoginService.
        byte[] key = Base64.getDecoder().decode(secret);
        return NimbusJwtDecoder.withSecretKey(new SecretKeySpec(key, "HmacSHA256"))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}