package com.umc.yeongkkeul.security;

import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.web.dto.TokenDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
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

    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24; // AccessToken의 유효시간 (24시간)
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7;  // RefreshToken의 유효시간 (7일)
    private static final long THREE_DAYS = 1000 * 60 * 60 * 24 * 3;

    @Value("${external.jwt.secret}")
    private String SECRET_KEY;

    public TokenDto createAccessToken(String email){
        Date expiryDate= new Date(new Date().getTime() + ACCESS_TOKEN_EXPIRE_TIME);
        String accessToken = Jwts.builder()
                .signWith(SignatureAlgorithm.HS512,SECRET_KEY)
                .setSubject(email)
                .setIssuer("yeongkkeul app")
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .compact();

        return TokenDto.builder()
                .grantType("auth")
                .accessToken(accessToken)
                .accessTokenExpiresIn(expiryDate.getTime())
                .refreshToken(null)
                .build();
    }



    public TokenDto genrateToken(String email){

        Date accessExpiryDate = new Date(new Date().getTime() + ACCESS_TOKEN_EXPIRE_TIME);
        String accessToken = Jwts.builder()
                .signWith(SignatureAlgorithm.HS512,SECRET_KEY)
                .setSubject(email)
                .setIssuer("yeongkkeul app")
                .setIssuedAt(new Date())
                .setExpiration(accessExpiryDate)
                .compact();

        Date refreshExpiryDate = new Date(new Date().getTime() + REFRESH_TOKEN_EXPIRE_TIME);

        String refreshToken = Jwts.builder()
                .signWith(SignatureAlgorithm.HS512,SECRET_KEY)
                .setSubject(email)
                .setIssuer("yeongkkeul app")
                .setIssuedAt(new Date())
                .setExpiration(refreshExpiryDate)
                .compact();

        return TokenDto.builder()
                .grantType("auth")
                .accessToken(accessToken)
                .accessTokenExpiresIn(accessExpiryDate.getTime())
                .refreshToken(refreshToken)
                .build();

    }

    public String getEmailFromToken(String token){
        Claims claims= Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean refreshTokenPeriodCheck(String token){
        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);

        long now = (new Date()).getTime();
        long refreshExpiredTime = claimsJws.getBody().getExpiration().getTime();

        // 유효기간 3일 이내면 true
        return (refreshExpiredTime - now <= THREE_DAYS);
    }




}