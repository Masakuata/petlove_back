package xatal.sharedz.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

public class TokenUtils {
    private static final String ACCESS_TOKEN_SECRET = System.getenv("TOKEN_SECRET");
    private final static Long TOKEN_LIFETIME_MILLS = 30L * 24 * 60 * 60 * 1000;

    public static String createToken(String username, String email) {
        Date expirationDate = new Date(System.currentTimeMillis() + TOKEN_LIFETIME_MILLS);

        HashMap<String, Object> claims = new HashMap<>();
        claims.put("username", username);

        return Jwts.builder()
                .subject(email)
                .expiration(expirationDate)
                .claims(claims)
                .signWith(Keys.hmacShaKeyFor(ACCESS_TOKEN_SECRET.getBytes()))
                .compact();
    }

    public static UsernamePasswordAuthenticationToken getAuth(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(ACCESS_TOKEN_SECRET.getBytes())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
        return new UsernamePasswordAuthenticationToken(
                claims.getSubject(),
                null,
                Collections.emptyList());
    }

    public static Claims getTokenClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(ACCESS_TOKEN_SECRET.getBytes())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
