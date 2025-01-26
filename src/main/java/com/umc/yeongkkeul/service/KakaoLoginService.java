package com.umc.yeongkkeul.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nimbusds.jose.shaded.gson.JsonElement;
import com.nimbusds.jose.shaded.gson.JsonObject;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.domain.enums.AgeGroup;
import com.umc.yeongkkeul.domain.enums.Job;
import com.umc.yeongkkeul.domain.enums.UserRole;
import com.umc.yeongkkeul.repository.UserTermsRepository;
import com.umc.yeongkkeul.web.dto.KakaoTokenResponseDto;
import com.umc.yeongkkeul.repository.UserRepository;
import com.umc.yeongkkeul.web.dto.SocialInfoResponseDto;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import com.nimbusds.jose.shaded.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HashMap;

@Slf4j
@Data
@NoArgsConstructor
@Service
public class KakaoLoginService {

    @Value("${kakao.client-id}")
    private String KAKAO_CLIENT_ID;
    @Value("${kakao.redirect-url}")
    private String KAKAO_REDIRECT_URL;
    @Value("${kakao.client-secret}")
    private String KAKAO_CLIENT_SECRET;

    private final static String KAKAO_AUTH_URI = "https://kauth.kakao.com";
    private final static String KAKAO_API = "https://kapi.kakao.com";


    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserTermsRepository userTermsRepository;

    @Transactional
    public String getKakaoAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // Http Response Body 객체 생성
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code"); //카카오 공식문서 기준 authorization_code 로 고정
        params.add("client_id", KAKAO_CLIENT_ID); // 카카오 Dev 앱 REST API 키
        params.add("redirect_uri", KAKAO_REDIRECT_URL); // 카카오 Dev redirect uri
        params.add("code", code); // 프론트에서 인가 코드 요청시 받은 인가 코드값
        params.add("client_secret",KAKAO_CLIENT_SECRET); //client_secret 발급

        // 헤더와 바디 합치기 위해 Http Entity 객체 생성
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params, headers);

        // 카카오로부터 Access token 받아오기
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> accessTokenResponse = rt.exchange(
                "https://kauth.kakao.com/oauth/token", // "https://kauth.kakao.com/oauth/token"
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );

        // JSON Parsing (-> KakaoTokenResponseDto)
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        KakaoTokenResponseDto kakaoTokenDto = null;
        try {
            kakaoTokenDto = objectMapper.readValue(accessTokenResponse.getBody(), KakaoTokenResponseDto.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        String AccessToken = kakaoTokenDto.getAccessToken();

        // 토큰 값 출력
        if (kakaoTokenDto != null) {
            log.info("Received Access Token: {}", AccessToken);
        }

        return AccessToken;
    }


    public HashMap<String, Object> getUserInfo(String accessToken) {
        HashMap<String, Object> userInfo = new HashMap<>();
        String reqUrl = "https://kapi.kakao.com/v2/user/me";
        try{
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

            int responseCode = conn.getResponseCode();
            log.info("[KakaoApi.getUserInfo] responseCode : {}",  responseCode);

            BufferedReader br;
            if (responseCode >= 200 && responseCode <= 300) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            String line = "";
            StringBuilder responseSb = new StringBuilder();
            while((line = br.readLine()) != null){
                responseSb.append(line);
            }
            String result = responseSb.toString();
            log.info("responseBody = {}", result);

            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(result);

            JsonObject properties = element.getAsJsonObject().get("properties").getAsJsonObject();
            JsonObject kakaoAccount = element.getAsJsonObject().get("kakao_account").getAsJsonObject();


            String nickname = new String(properties.getAsJsonObject().get("nickname").getAsString().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            String email = new String(kakaoAccount.getAsJsonObject().get("email").getAsString().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);

            userInfo.put("nickname", nickname);
            userInfo.put("email", email);

            br.close();

        }catch (Exception e){
            e.printStackTrace();
        }
        return userInfo;
    }

    //카카오 로그인 성공 기본 값 저장
    public SocialInfoResponseDto.KakaoInfoDTO loginUserInfo(String jwtToken, String refreshToken, String email, String name){

        boolean isExistUser = userRepository.existsByEmail(email);
        boolean isExistTerms = userTermsRepository.existsByUser_EmailAndUser_OauthType(email,"KAKAO");

        String redirectUrl = isExistTerms ? "/api/home" : "/api/auth/user-info";

        //초기 사용자 값 저장(kakao 정보)
        if(!isExistUser){
            User user = User.builder()
                    .nickname(name)
                    .email(email)
                    .job(Job.UNDECIDED)
                    .userRole(UserRole.USER)
                    .oauthType("KAKAO")
                    .referralCode(generateRandomCode(6))
                    .oauthKey(refreshToken)
                    .gender("UNDECIDED")

                    .ageGroup(AgeGroup.UNDECIDED)
                    .rewardBalance(0)
                    .build();

            userRepository.save(user);
        }

        return SocialInfoResponseDto.KakaoInfoDTO.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .email(email)
                .redirectUrl(redirectUrl)
                .build();
    }

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
