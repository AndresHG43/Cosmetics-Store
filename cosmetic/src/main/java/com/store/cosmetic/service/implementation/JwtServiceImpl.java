package com.store.cosmetic.service.implementation;

import com.store.cosmetic.config.DateTimeConfig;
import com.store.cosmetic.service.contract.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.private-key-path}")
    private Resource privateKeyResource;

    @Value("${jwt.public-key-path}")
    private Resource publicKeyResource;

    @Value("${jwt.expiration:3600000}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration:86400000}")
    private long refreshExpiration;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    private static final String USER_ID = "userId";

    @PostConstruct
    public void init() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        this.privateKey = loadPrivateKey();
        this.publicKey = loadPublicKey();
    }

    private PrivateKey loadPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String pem = readPem(privateKeyResource);
        pem = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(pem);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private PublicKey loadPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String pem = readPem(publicKeyResource);
        pem = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(pem);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private String readPem(Resource resource) throws IOException {
        try (InputStream is = resource.getInputStream()) {
            return new String(is.readAllBytes());
        }
    }

    @Override
    public String generateToken(String username, Long userId, List<String> roles) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("type", "access");
        if (userId != null) {
            extraClaims.put(USER_ID, userId);
        }
        if (roles != null) {
            extraClaims.put("roles", roles);
        }
        return generateToken(extraClaims, username);
    }

    @Override
    public String generateToken(Map<String, Object> extraClaims, String username) {
        long currentTime = System.currentTimeMillis();
        return Jwts.builder()
                .header().add("typ", "JWT").and()
                .claims(extraClaims)
                .subject(username)
                .issuedAt(new Date(currentTime))
                .expiration(new Date(currentTime + jwtExpiration))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    @Override
    public String generateRefreshToken(String username, Long userId) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("type", "refresh");
        if (userId != null) {
            extraClaims.put(USER_ID, userId);
        }
        long currentTime = System.currentTimeMillis();
        return Jwts.builder()
                .header().add("typ", "JWT").and()
                .claims(extraClaims)
                .subject(username)
                .issuedAt(new Date(currentTime))
                .expiration(new Date(currentTime + refreshExpiration))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        Object userIdObj = claims.get(USER_ID);
        if (userIdObj instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    @Override
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }
        return List.of();
    }

    @Override
    public String extractTokenType(String token) {
        Claims claims = extractAllClaims(token);
        Object type = claims.get("type");
        return type != null ? type.toString() : null;
    }

    @Override
    public LocalDateTime extractExpiration(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.toInstant()
                .atZone(DateTimeConfig.APP_ZONE)
                .toLocalDateTime();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception _) {
            return false;
        }
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception _) {
            return false;
        }
    }

    @Override
    public boolean isRefreshToken(String token) {
        try {
            return "refresh".equalsIgnoreCase(extractTokenType(token)) && !isTokenExpired(token);
        } catch (Exception _) {
            return false;
        }
    }

    @Override
    public boolean isAccessToken(String token) {
        try {
            String type = extractTokenType(token);
            return (type == null || "access".equalsIgnoreCase(type)) && !isTokenExpired(token);
        } catch (Exception _) {
            return false;
        }
    }

    @Override
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).isBefore(LocalDateTime.now(DateTimeConfig.APP_ZONE));
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    public void setJwtExpiration(long jwtExpiration) {
        this.jwtExpiration = jwtExpiration;
    }

    public void setRefreshExpiration(long refreshExpiration) {
        this.refreshExpiration = refreshExpiration;
    }
}
