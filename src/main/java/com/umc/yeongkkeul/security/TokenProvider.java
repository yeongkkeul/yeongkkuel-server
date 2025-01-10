package com.umc.yeongkkeul.security;

import com.umc.yeongkkeul.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Slf4j
@Service
public class TokenProvider { // 현재는 userId 기반 jwt 토큰 발행 -> 추후 카카오, 구글 기반 OAuth로 변경시 수정해야함
    @Value("${external.jwt.secret}")
    private String SECRET_KEY;

    public String create(User user){
        Date expiryDate=Date.from(Instant.now().plus(1, ChronoUnit.DAYS));
        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS512,SECRET_KEY)
                .setSubject(user.getId().toString()) // String으로 변환함!
                .setIssuer("yeongkkeul app")
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .compact();
    }

    public String validateAndGetUserId(String token){
        Claims claims= Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}