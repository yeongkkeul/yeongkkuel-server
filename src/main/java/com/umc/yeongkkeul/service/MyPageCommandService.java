package com.umc.yeongkkeul.service;

import com.umc.yeongkkeul.apiPayload.code.status.ErrorStatus;
import com.umc.yeongkkeul.apiPayload.exception.handler.UserHandler;
import com.umc.yeongkkeul.aws.s3.AmazonS3Manager;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.domain.UserExitReason;
import com.umc.yeongkkeul.domain.enums.ExitReason;
import com.umc.yeongkkeul.repository.UserExitReasonRepository;
import com.umc.yeongkkeul.repository.UserRepository;
import com.umc.yeongkkeul.web.dto.MyPageInfoResponseDto;
import com.umc.yeongkkeul.web.dto.MyPageInfoRequestDto;
import com.umc.yeongkkeul.web.dto.UserExitRequestDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class MyPageCommandService {

    private final UserRepository userRepository;
    private final UserExitReasonRepository userExitReasonRepository;
    private final MyPageQueryService myPageQueryService;

    private final AmazonS3Manager amazonS3Manager;

    // TODO: 프로필사진 수정 (string -> 이미지 파일로)
    public MyPageInfoResponseDto updateUserInfo(Long userId, MyPageInfoRequestDto mypageInfoRequestDto, MultipartFile profileImage) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        String profileImageUrl = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            String keyName = amazonS3Manager.generateUserProfileKeyName(user.getId());
            profileImageUrl = amazonS3Manager.uploadFile(keyName, profileImage);
        }

        user.updateProfile(mypageInfoRequestDto.getNickname(), mypageInfoRequestDto.getGender(),mypageInfoRequestDto.getAgeGroup(), mypageInfoRequestDto.getJob(), profileImageUrl);
        userRepository.save(user);

        double weeklyAchievementRate = myPageQueryService.caculateWeeklyAchievementRate(user);
        return MyPageInfoResponseDto.of(user, weeklyAchievementRate);
    }

    // 사용자 관련 데이터 전부 삭제하고 탈퇴 사유 저장
    public void deleteUser(Long userId, UserExitRequestDto userExitRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        ExitReason reason = switch (userExitRequestDto.getReason()) {
            case "사용 빈도가 줄어서" -> ExitReason.FREQUENCY_DECREASED;
            case "대체 서비스를 발견해서" -> ExitReason.ALTERNATIVE_SERVICE_FOUND;
            case "재미가 없어서" -> ExitReason.NOT_FUN;
            case "사용 방법이 어려워서" -> ExitReason.DIFFICULTY;
            case "기타" -> ExitReason.OTHER;
            default -> throw new UserHandler(ErrorStatus.INVALID_EXIT_REASON);
        };

        // 기타 선택 시 detail 필수 입력 받도록 (나머지는 detail null 처리)
        String detail = ExitReason.OTHER.equals(reason) ? userExitRequestDto.getDetail() : null;
        if (ExitReason.OTHER.equals(reason) && (detail == null || detail.isEmpty())) {
            throw new UserHandler(ErrorStatus.DETAIL_REQUIRED_FOR_OTHER_REASON);
        }

        UserExitReason exitReason = UserExitReason.builder()
                .exitReason(reason)
                .detail(detail)
                .build();
        userExitReasonRepository.save(exitReason);

        userRepository.deleteById(user.getId());
    }
}
