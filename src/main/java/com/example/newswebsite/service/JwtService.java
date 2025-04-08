
package com.example.newswebsite.service;

import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.text.ParseException;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    private final long ACCESS_TOKEN_EXPIRY = 60 * 60 * 1000; // 1 giờ
    private final long REFRESH_TOKEN_EXPIRY = 7 * 24 * 60 * 60 * 1000; // 7 ngày

    // Tạo access token (dùng HS512 cho token nội bộ)
    public String generateToken(String googleId, String role) {
        return io.jsonwebtoken.Jwts.builder()
                .setSubject(googleId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRY))
                .signWith(io.jsonwebtoken.SignatureAlgorithm.HS512, secret)
                .compact();
    }

    // Tạo refresh token (dùng HS512 cho token nội bộ)
    public String generateRefreshToken(String googleId) {
        return io.jsonwebtoken.Jwts.builder()
                .setSubject(googleId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRY))
                .signWith(io.jsonwebtoken.SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public OidcUser parseGoogleIdToken(String idToken) {
        try {
            // Parse ID Token
            SignedJWT signedJWT = SignedJWT.parse(idToken);

            // Tải JWK từ Google
            JWKSet jwkSet = JWKSet.load(new URL("https://www.googleapis.com/oauth2/v3/certs"));
            RSAKey rsaKey = (RSAKey) jwkSet.getKeyByKeyId(signedJWT.getHeader().getKeyID());
            if (rsaKey == null) {
                throw new IllegalArgumentException(
                        "No matching public key found for kid: " + signedJWT.getHeader().getKeyID());
            }

            // Xác minh chữ ký
            JWSVerifier verifier = new RSASSAVerifier(rsaKey.toRSAPublicKey());
            if (!signedJWT.verify(verifier)) {
                throw new IllegalArgumentException("ID token signature verification failed");
            }

            // Lấy claims từ token
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            // Kiểm tra thời gian hết hạn
            Date expiry = claims.getExpirationTime();
            if (expiry == null || expiry.before(new Date())) {
                throw new IllegalArgumentException("ID token has expired");
            }

            // Tạo attributes cho OidcIdToken
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("sub", claims.getSubject());
            attributes.put("email", claims.getClaim("email"));
            attributes.put("name", claims.getClaim("name"));
            attributes.put("picture", claims.getClaim("picture"));
            attributes.put("iss", claims.getIssuer());
            attributes.put("aud", claims.getAudience());
            attributes.put("iat", claims.getIssueTime().getTime() / 1000);
            attributes.put("exp", claims.getExpirationTime().getTime() / 1000);

            // Tạo OidcIdToken
            OidcIdToken oidcIdToken = new OidcIdToken(
                    idToken, // Chuỗi token gốc
                    Instant.ofEpochSecond((Long) attributes.get("iat")),
                    Instant.ofEpochSecond((Long) attributes.get("exp")),
                    attributes);

            // Trả về OidcUser
            return new DefaultOidcUser(Collections.emptyList(), oidcIdToken);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Failed to parse ID token: " + e.getMessage());
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to process ID token: " + e.getMessage());
        }
    }

    // Xác minh token nội bộ (access token, refresh token)
    public String getGoogleIdFromToken(String token) {
        io.jsonwebtoken.Claims claims = io.jsonwebtoken.Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public String getRoleFromToken(String token) {
        io.jsonwebtoken.Claims claims = io.jsonwebtoken.Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
        return claims.get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            io.jsonwebtoken.Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}