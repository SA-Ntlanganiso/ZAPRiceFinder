package sa_ntlanganiso.SmartPrices_SA.security;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import sa_ntlanganiso.SmartPrices_SA.model.Customer;
import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {
    // 24 hours in milliseconds
    private static final long VALIDITY_IN_MILLISECONDS = 86400000L;
    
    // Pre-generated secure key for HS512 (512-bit)
    private static final String BASE64_ENCODED_SECRET =  "t4vV3p7yA9D0gF5jH8kL2oP5uM1qW4eR7tY9iU3oP5aZ2xS6dF9gH4jK7lP3oI9uY2tR5eW8qA1sD4fG7hJ9kL2";
    
    private final SecretKey secretKey;
    private final long validityInMilliseconds;

    public JwtUtil() {
        // Decode the base64 encoded key
        byte[] decodedKey = Base64.getDecoder().decode(BASE64_ENCODED_SECRET);
        this.secretKey = Keys.hmacShaKeyFor(decodedKey);
        this.validityInMilliseconds = VALIDITY_IN_MILLISECONDS;
    }

    public String generateToken(Customer customer) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setSubject(customer.getEmail())
                .claim("userId", customer.getId().toString())
                .claim("name", customer.getName())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String getUserIdFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("userId", String.class);
    }
}