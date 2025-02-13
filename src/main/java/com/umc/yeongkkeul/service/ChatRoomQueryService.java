package com.umc.yeongkkeul.service;

import com.umc.yeongkkeul.apiPayload.code.status.ErrorStatus;
import com.umc.yeongkkeul.apiPayload.exception.handler.ChatRoomHandler;
import com.umc.yeongkkeul.domain.ChatRoom;
import com.umc.yeongkkeul.domain.Expense;
import com.umc.yeongkkeul.domain.enums.AgeGroup;
import com.umc.yeongkkeul.domain.enums.Job;
import com.umc.yeongkkeul.repository.ChatRoomMembershipRepository;
import com.umc.yeongkkeul.repository.ChatRoomRepository;
import com.umc.yeongkkeul.repository.ExpenseRepository;
import com.umc.yeongkkeul.web.dto.BannerResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomQueryService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMembershipRepository chatRoomMembershipRepository;
    private final ExpenseRepository expenseRepository;

    // TODO : 배너 api 배치를 통해 저장된 값 보여주도록 수 (배너에서는 필터 없으면 null로 / 채팅방 조회에서는 필터 없으면 전체 백분위로)
    public BannerResponseDto getChatRoomBanner(Long chatRoomId) {

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatRoomHandler(ErrorStatus._CHATROOM_NOT_FOUND));

        // 해당 채팅방의 유저 리스트
        List<Long> userIdList = chatRoomMembershipRepository.findUserIdByChatroomId(chatRoomId);

        int achievingCount = 0; // 하루 목표 달성한 유저 수
        int sumAmount = 0; // 지출 통계를 위한 합계
        for (Long userId : userIdList) {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            List<Expense> expenseList = expenseRepository.findYesterdayExpenseByUserId(userId, yesterday);

            int userAmount = 0; // 유저의 하루 지출 총액
            for (Expense expense : expenseList) {
                userAmount += expense.getAmount();
            }

            // 하루 목표 달성한 유저 수 count (유저의 하루 지출 총액 <= 채팅방의 하루 목표 지출 금액)
            if (userAmount <= chatRoom.getDailySpendingGoalFilter()) {
                achievingCount++;
            }
            sumAmount += userAmount;
        }

        // 채팅방 총 참여자 수
        int chatRoomUserCount = chatRoomMembershipRepository.countByChatroomId(chatRoomId);

        // 지출 평균
        int avgAmount = sumAmount / chatRoomUserCount;

        // age 필터
        AgeGroup ageFilter = chatRoom.getAgeGroupFilter();
        String age = (ageFilter != null) ? ageFilter.getAgeGroup() : null;

        // job 필터
        Job jobFilter = chatRoom.getJobFilter();
        String job = (jobFilter != null) ? jobFilter.getJob() : null;

        // job, age 기준으로 필터링
        List<ChatRoom> chatRoomList = Collections.emptyList();;
        if (ageFilter != null && !"UNDECIDED".equals(ageFilter) && jobFilter != null && !"UNDECIDED".equals(jobFilter)) { // age, job
            chatRoomList = chatRoomRepository.findAllByAgeGroupFilterAndJobFilterOrderByTotalScoreDesc(ageFilter, jobFilter);
        } else if (ageFilter != null && !"UNDECIDED".equals(ageFilter) && (jobFilter == null || "UNDECIDED".equals(jobFilter))) { // age
            chatRoomList = chatRoomRepository.findAllByAgeGroupFilterOrderByTotalScoreDesc(ageFilter);
        } else if (jobFilter != null && !"UNDECIDED".equals(jobFilter) && (ageFilter == null || "UNDECIDED".equals(ageFilter))) { // job
            chatRoomList = chatRoomRepository.findAllByJobFilterOrderByTotalScoreDesc(jobFilter);
        }

        // null 아닌 방만 남기기
        List<ChatRoom> chatRooms = chatRoomList.stream()
                .filter(room -> room.getTotalScore() != null)
                .toList();

        // 채팅방 백분위 계산 (chatRoom totalScore Batch 돌린 값)
        Double topRate = null; // age, job 없을 때, 5명 이하일 때 null 반환 위함
        if (chatRooms.size() >= 5) { // 5명 이상인 방만 계산
            int ranking = 1;
            for (int i = 0; i < chatRooms.size(); i++) {
                if (chatRooms.get(i).getId().equals(chatRoom.getId())) {
                    ranking = i + 1;
                    break;
                }
            }
            topRate = ((double) ranking / chatRooms.size()) * 100.0;
        }

        // 배너 기준 날짜 (공지하는 날짜 - 오늘)
        String createdAt = LocalDate.now().format(DateTimeFormatter.ofPattern("MM.dd"));

        return BannerResponseDto.builder()
                .achievingCount(achievingCount)
                .chatRoomUserCount(chatRoomUserCount)
                .avgAmount(avgAmount)
                .age(age)
                .job(job)
                .topRate(topRate)
                .createdAt(createdAt)
                .build();
    }
}
