import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GenerateTokens {
    private static final String SECRET = "fnusale-campus-secondhand-trading-platform-jwt-secret-key";
    private static final long EXPIRATION = 2 * 60 * 60 * 1000L;

    private static SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public static String generateToken(Long userId, String username, String identityType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("identityType", identityType);
        claims.put("type", "access");
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION);
        
        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSecretKey())
                .compact();
    }

    public static void main(String[] args) {
        int count = args.length > 0 ? Integer.parseInt(args[0]) : 100;
        StringBuilder sb = new StringBuilder();
        sb.append("{\"tokens\": [");
        for (int i = 1; i <= count; i++) {
            String token = generateToken((long) i, "test_user_" + i, "USER");
            if (i > 1) sb.append(",");
            sb.append("\"").append(token).append("\"");
        }
        sb.append("]}");
        System.out.println(sb.toString());
    }
}
