package com.godown.storage.service;

import com.godown.storage.dto.LoginRequest;
import com.godown.storage.dto.LoginResponse;
import com.godown.storage.entity.User;
import com.godown.storage.repository.UserRepository;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;

@Service
public class LoginService {

    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final JwtEncoder jwtEncoder;
    private final long expirationMs;

    public LoginService(
            UserRepository users,
            PasswordEncoder passwords,
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs) {
        this.users = users;
        this.passwords = passwords;
        this.expirationMs = expirationMs;

        // PowerShell stores the random signing bytes as Base64 in JWT_SECRET.
        byte[] key = Base64.getDecoder().decode(secret);
        if (key.length < 32) {
            throw new IllegalArgumentException("JWT_SECRET must decode to at least 32 bytes");
        }
        this.jwtEncoder = new NimbusJwtEncoder(
                new ImmutableSecret<>(new SecretKeySpec(key, "HmacSHA256")));
    }

    public LoginResponse login(LoginRequest request) {
        String email = request.email().toLowerCase(Locale.ROOT);
        User user = users.findByEmail(email).orElseThrow(this::invalidCredentials);

        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72
                || !passwords.matches(request.password(), user.getPassword())) {
            throw invalidCredentials();
        }

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                // Use the database ID as the subject so it stays stable if the email changes.
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plusMillis(expirationMs))
                .claim("role", user.getRole().name())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new LoginResponse(token, "Bearer");
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }
}