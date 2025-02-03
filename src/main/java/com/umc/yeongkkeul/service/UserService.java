package com.umc.yeongkkeul.service;

import com.umc.yeongkkeul.apiPayload.code.status.ErrorStatus;
import com.umc.yeongkkeul.apiPayload.exception.GeneralException;
import com.umc.yeongkkeul.domain.Term;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.domain.mapping.UserTerms;
import com.umc.yeongkkeul.repository.TermRepository;
import com.umc.yeongkkeul.repository.UserRepository;
import com.umc.yeongkkeul.repository.UserTermsRepository;
import com.umc.yeongkkeul.web.dto.UserRequestDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Data
@RequiredArgsConstructor
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserTermsRepository userTermsRepository;
    @Autowired
    private TermRepository termRepository;

    public void saveUserInfo(String email, UserRequestDto.userInfoDto userInfoDto){

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));


        user.setNickname(userInfoDto.getNickName());
        user.setGender(userInfoDto.getGender());
        user.setAgeGroup(userInfoDto.getAgeGroup());
        user.setJob(userInfoDto.getJob());



        userRepository.save(user);
    }

    public boolean findReferralCode(String email, UserRequestDto.ReferralCodeRequestDto referralCodeRequestDto){

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));

        if(referralCodeRequestDto.getReferralCode() == null || referralCodeRequestDto.getReferralCode().isBlank()){
            return false;
        }
        else if(referralCodeRequestDto.getReferralCode()!=null) {
            User findRecommendUser = userRepository.findByReferralCode(referralCodeRequestDto.getReferralCode())
                    .orElseThrow(() -> new GeneralException(ErrorStatus._REFERRALCODE_NOT_FOUND));

            findRecommendUser.setRewardBalance(findRecommendUser.getRewardBalance() + 30);
            user.setRewardBalance(user.getRewardBalance()+30);

            userRepository.save(user);
            userRepository.save(findRecommendUser);

            return true;
        }

        return false;
    }



    public void saveUserTerms(String email, UserRequestDto.TermDTO termDto){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));


        List<UserTerms> userTermsList = new ArrayList<>();


        // Term1
        Term term1 = termRepository.findTermById(1L);

        userTermsList.add(UserTerms.builder()
                .user(user)
                .term(term1)
                .isAgreed(true)
                .agreedAt(LocalDateTime.now())
                .build());

        // Term2
        Term term2 = termRepository.findTermById(2L);

        userTermsList.add(UserTerms.builder()
                .user(user)
                .term(term2)
                .isAgreed(true)
                .agreedAt(LocalDateTime.now())
                .build());

        // Term3
        Term term3 = termRepository.findTermById(3L);

        userTermsList.add(UserTerms.builder()
                .user(user)
                .term(term3)
                .isAgreed(true)
                .agreedAt(LocalDateTime.now())
                .build());

        // Term4 (선택적)
        Term term4 = termRepository.findTermById(4L);

        userTermsList.add(UserTerms.builder()
                .user(user)
                .term(term4)
                .isAgreed(termDto.getTerm4())
                .agreedAt(LocalDateTime.now())
                .build());





        userTermsRepository.saveAll(userTermsList);




    }

    public void logout(String token, String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));

        String oauthType = user.getOauthType();

        if(oauthType=="KAKAO"){
            kakaoLogout(token);
        }else if(oauthType=="GOOGLE"){
            googleLogout(token);
        }

    }

    public void kakaoLogout(String accessToken) {
        String reqUrl = "https://kapi.kakao.com/v1/user/logout";
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(reqUrl).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

            int responseCode = conn.getResponseCode();
            log.info("[KakaoApi.kakaoLogout] responseCode : {}", responseCode);

            if (responseCode != 200) {
                log.warn("Kakao 로그아웃 실패");
            }
        } catch (Exception e) {
            log.error("Kakao 로그아웃 중 오류 발생", e);
        }
    }

    public void googleLogout(String idToken) {
        String revokeUrl = "https://accounts.google.com/o/oauth2/revoke?token=" + idToken;
        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getForObject(revokeUrl, String.class);
            log.info("Google 토큰 무효화 성공");
        } catch (Exception e) {
            log.error("Google 로그아웃 실패", e);
        }
    }
}
