package com.umc.yeongkkeul.service;



import com.umc.yeongkkeul.apiPayload.code.status.ErrorStatus;
import com.umc.yeongkkeul.apiPayload.exception.GeneralException;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.domain.enums.AgeGroup;
import com.umc.yeongkkeul.domain.enums.Job;
import com.umc.yeongkkeul.repository.UserRepository;
import com.umc.yeongkkeul.repository.UserTermsRepository;
import com.umc.yeongkkeul.security.TokenProvider;
import com.umc.yeongkkeul.web.dto.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Data
@NoArgsConstructor
@Service
public class GoogleLoginService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserTermsRepository userTermsRepository;
    @Autowired
    private TokenProvider tokenProvider;




    @Transactional
    public SocialInfoResponseDto.GoogleInfoDTO socialLoginGoogle(String idToken) {
        // 1) 구글 ID Token 검증
        GoogleInfoResponseDto googleInfoResponseDto = verifyGoogleToken(idToken);

        Boolean isExistTerms = userTermsRepository.existsByUser_EmailAndUser_OauthType(googleInfoResponseDto.getEmail(),"GOOGLE");

        TokenDto tokenDto = tokenProvider.genrateToken(googleInfoResponseDto.getEmail());
        String accessToken = tokenDto.getAccessToken();
        String refreshToken = tokenDto.getRefreshToken();

        String redirectUrl = isExistTerms ? "/api/home" : "/api/auth/user-info";

        if (googleInfoResponseDto == null) {
            throw new RuntimeException("구글 토큰 검증 실패");
        }

        // 2) DB 조회 or 가입
        Boolean isExistUser = userRepository.existsByOauthTypeAndEmail("GOOGLE", googleInfoResponseDto.getEmail());
        if(!isExistUser) {
            User newUser = User.builder()
                    .oauthType("GOOGLE")
                    .oauthKey(refreshToken)
                    .job(Job.UNDECIDED)
                    .ageGroup(AgeGroup.UNDECIDED)
                    .referralCode(generateRandomCode(6))
                    .email(googleInfoResponseDto.getEmail())
                    .nickname(googleInfoResponseDto.getName())
                    .gender("UNDECIDED")
                    .build();
            userRepository.save(newUser);
        }

        return SocialInfoResponseDto.GoogleInfoDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(googleInfoResponseDto.getEmail())
                .redirectUrl(redirectUrl)
                .build();

    }


    public GoogleInfoResponseDto verifyGoogleToken(String idToken) {
        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;

        try {
            RestTemplate restTemplate = new RestTemplate();
            GoogleTokenResponseDto response =
                    restTemplate.getForObject(url, GoogleTokenResponseDto.class);
            log.info("Google response: {}", response);

            if (response != null && response.getSub() != null) {
                // TODO: aud(클라이언트ID), iss, email_verified 등 추가 검증
                // 프로토타입이라 생략!
                return GoogleInfoResponseDto.builder()
                        .sub(response.getSub())
                        .email(response.getEmail())
                        .name(response.getName())
                        .build();
            }
        } catch (Exception e) {
            log.error("Google Token 검증 실패", e);
        }
        return null;
    }

//    public Map<String, Object> authenticateUser(String idToken) throws GeneralSecurityException, IOException {
//        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance())
//                .setAudience(Collections.singletonList(googleClientId))
//                .build();
//
//        GoogleIdToken googleIdToken = verifier.verify(idToken);
//        if (googleIdToken != null) {
//            GoogleIdToken.Payload payload = googleIdToken.getPayload();
//            String userId = payload.getSubject();
//            String email = payload.getEmail();
//            String name = (String) payload.get("name");
//            String pictureUrl = (String) payload.get("picture");
//            log.info(email);
//
//            TokenDto tokenDto = tokenProvider.genrateToken(email);
//
//            Map<String, Object> userDetails = new HashMap<>();
//            userDetails.put("userId", userId);
//            userDetails.put("email", email);
//            userDetails.put("name", name);
//            userDetails.put("pictureUrl", pictureUrl);
//            userDetails.put("accessToken", tokenDto.getAccessToken());
//            userDetails.put("refreshToken", tokenDto.getRefreshToken());
//
//            return userDetails;
//        } else {
//            throw new IllegalArgumentException("Invalid ID token.");
//        }
//    }




    public static String generateRandomCode(int length) {
        // 가능한 문자 세트: 영문 대소문자 + 숫자
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom(); // 보안적으로 강력한 랜덤 생성기
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            code.append(chars.charAt(index));
        }

        return code.toString();
    }


}
