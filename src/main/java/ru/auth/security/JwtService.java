package ru.auth.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token.expiration-ms}")
    private long accessTtl;

    @Value("${jwt.refresh-token.expiration-ms}")
    private long refreshTtl;

    public String generateToken(UserDetails userDetails) {
        return buildToken(userDetails, accessTtl, "access");
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(userDetails, refreshTtl, "refresh");
    }

    private String buildToken(UserDetails ud, long ttl, String type) {
        Instant now = Instant.now();
        Algorithm alg = Algorithm.HMAC256(secret);
        return JWT.create()
                .withSubject(ud.getUsername())
                .withClaim("roles", ud.getAuthorities().stream().map(Object::toString).toList())
                .withClaim("type", type)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusMillis(ttl)))
                .sign(alg);
    }

    private DecodedJWT decode(String token) {
        return JWT.require(Algorithm.HMAC256(secret)).build().verify(token);
    }

    public String extractUsername(String token) {
        return decode(token).getSubject();
    }

    public String extractTokenType(String token) {
        return decode(token).getClaim("type").asString();
    }

    public boolean isTokenValid(String token) {
        try {
            return decode(token).getExpiresAt().after(new Date());
        } catch (Exception ex) {
            return false;
        }
    }

    public Instant extractExpiration(String token) {
        return decode(token).getExpiresAt().toInstant();
    }
}
