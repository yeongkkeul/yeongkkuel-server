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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Data
@RequiredArgsConstructor
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

        User findRecommendUser = userRepository.findByReferralCode(userInfoDto.getReferralCode())
                        .orElseThrow(()-> new GeneralException(ErrorStatus._REFERRALCODE_NOT_FOUND));

        user.setNickname(userInfoDto.getNickName());
        user.setGender(userInfoDto.getGender());
        user.setAgeGroup(userInfoDto.getAgeGroup());
        user.setJob(userInfoDto.getJob());



        userRepository.save(user);
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
}
