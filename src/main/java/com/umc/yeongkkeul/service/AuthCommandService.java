package com.umc.yeongkkeul.service;

import com.umc.yeongkkeul.apiPayload.code.status.ErrorStatus;
import com.umc.yeongkkeul.apiPayload.exception.GeneralException;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.repository.UserRepository;
import com.umc.yeongkkeul.security.TokenProvider;
import com.umc.yeongkkeul.web.dto.TokenDto;
import com.umc.yeongkkeul.web.dto.TokenRequestDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Data
@RequiredArgsConstructor
public class AuthCommandService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenProvider tokenProvider;


    public TokenDto reissue(TokenRequestDto tokenRequestDto) {
        // Refresh Token 검증
        if (!tokenProvider.validateToken(tokenRequestDto.getRefreshToken())) {
            throw new GeneralException(ErrorStatus._INVALID_REFRESH_TOKEN);
        }

        // Access Token 에서 user 정보 가져오기
        String email = tokenProvider.getEmailFromToken(tokenRequestDto.getAccessToken());

        // 저장소의 정보를 기반으로 RefreshToken 값 가져옴
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus._UNAUTHORIZED));

        // Refresh Token 일치하는지 검사
        if (!user.getOauthKey().equals(tokenRequestDto.getRefreshToken())) {
            throw new GeneralException(ErrorStatus._INVALID_REFRESH_TOKEN);
        }

        // 새로운 AccessToken 생성
        TokenDto tokenDto;
        if (tokenProvider.refreshTokenPeriodCheck(user.getOauthKey())) {
            // RefreshToken의 유효기간이 3일 이하면, Access,Refresh Token 모두 재발급
            tokenDto = tokenProvider.genrateToken(email);

            user.setOauthKey(tokenDto.getRefreshToken()); // 새로운 RefreshToken 저장
            userRepository.save(user);
        } else {
            // Refresh Token의 유효기간이 3일 이상이면, AccessToken만 재발급
            tokenDto = tokenProvider.createAccessToken(email);
        }

        // 토큰 발급
        return tokenDto;
    }


}
