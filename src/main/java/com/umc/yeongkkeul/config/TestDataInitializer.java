package com.umc.yeongkkeul.config;

import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.domain.enums.AgeGroup;
import com.umc.yeongkkeul.domain.enums.Job;
import com.umc.yeongkkeul.domain.enums.UserRole;
import com.umc.yeongkkeul.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        // 이미 존재하는지 체크한 뒤 생성 (이메일 등 특정 필드를 기준으로 체크)
        String testUserEmail = "testuser@example.com";
        boolean exists = userRepository.existsByEmail(testUserEmail);

        if (!exists) {
            User testUser = User.builder()
                    .oauthType("KAKAO")
                    .oauthKey("test_oauth_key")
                    .nickname("테스트유저")
                    .userRole(UserRole.USER)
                    .gender("FEMALE")
                    .ageGroup(AgeGroup.TWENTIES)
                    .job(Job.STUDENT)
                    .email(testUserEmail)
                    .referralCode(null)
                    .rewardBalance(0)
                    .status(true)
                    .inactiveDate(null)
                    .dayTargetExpenditure(null)
                    .notificationAgreed(true)
                    .build();

            userRepository.save(testUser);
            System.out.println("[TestDataInitializer] 테스트 유저가 생성되었습니다: " + testUserEmail);
        } else {
            System.out.println("[TestDataInitializer] 이미 테스트 유저가 존재합니다: " + testUserEmail);
        }
    }
}