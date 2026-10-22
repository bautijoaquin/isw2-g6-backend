package com.isw2.liga.security;

import com.isw2.liga.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/** Genera y valida los tokens JWT (RNF-05). */
@Service
public class JwtService {

    private final SecretKey clave;
    private final Duration validez;

    public JwtService(@Value("${jwt.secret}") String secretoBase64,
                      @Value("${jwt.expiracion-horas}") long horas) {
        this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretoBase64));
        this.validez = Duration.ofHours(horas);
    }

    public String generarToken(Usuario usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim("id", usuario.getId())
                .claim("rol", usuario.getRol().name())
                .claim("nombre", usuario.getNombreCompleto())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(validez)))
                .signWith(clave)
                .compact();
    }

    /** Devuelve los datos del token, o vacío si está vencido, mal firmado o mal formado. */
    public Optional<Claims> validar(String token) {
        try {
            return Optional.of(Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
