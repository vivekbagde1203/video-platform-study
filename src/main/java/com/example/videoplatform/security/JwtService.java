package com.example.videoplatform.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    // ✅ Secure 256-bit (32-byte) secret key for HS256 algorithm
    // You can replace the string with your own generated secret from:
    //    openssl rand -base64 32
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(
            "MySuperSecretKeyForHLSVideos123456789!".getBytes(StandardCharsets.UTF_8)
    );

    // Token validity (10 minutes)
    private static final long EXPIRATION_TIME = 10 * 60 * 1000;

    // 🎟 Generate token for specific path + username
    public String createTokenForPath(String path, String username) {
        return Jwts.builder()
                .claim("path", path)
                .claim("user", username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ Validate token — ensures path, user, and expiry are correct
    public boolean validateToken(String token, String path, String username) {
        try {
            Claims claims = extractAllClaims(token);
            String tokenPath = claims.get("path", String.class);
            String tokenUser = claims.get("user", String.class);

            return tokenPath.equals(path)
                    && tokenUser.equals(username)
                    && !isTokenExpired(token);
        } catch (Exception e) {
            System.err.println("❌ JWT validation failed: " + e.getMessage());
            return false;
        }
    }

    // 🔍 Check expiry
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 🧩 Parse all claims safely
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

