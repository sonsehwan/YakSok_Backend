package com.sehwan.YakSok.auth;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtProvider {
    private final SecretKey key;
    private final long accessTokenValidityMs;

    public JwtProvider(@Value("${jwt.secret}") SecretKey key, @Value("${jwt.acess-token-validity}") long accessTokenValidityMs){
        this.key = key;
        this.accessTokenValidityMs = accessTokenValidityMs;
    }

    public String createAccessToken(Long userId){
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenValidityMs))
                .signWith(key)
                .compact();
    }

    public Long getUserId(String token){
        return Long.valueOf(Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload().getSubject());
    }
}
