package com.umc.yeongkkeul.batch;

import com.umc.yeongkkeul.domain.ChatRoom;
import com.umc.yeongkkeul.domain.enums.AgeGroup;
import com.umc.yeongkkeul.repository.ChatRoomMembershipRepository;
import com.umc.yeongkkeul.repository.ChatRoomRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableBatchProcessing
public class ChatRoomBatchConfig {

    private final JobRepository jobRepository;
    private final ChatRoomMembershipRepository chatRoomMembershipRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final EntityManagerFactory entityManagerFactory;
    private final EntityManager entityManager;
    private final PlatformTransactionManager transactionManager;

    // ChatRoom의 목표 달성 챌린저 인원, 지출 평균, 랭킹 갱신
    @Bean
    public Job updateChatRoomJob() {
        return new JobBuilder("updateChatRoomJob", jobRepository)
                .start(updateChatRoomStep())
                .build();
    }

    @Bean
    public Step updateChatRoomStep() {
        return new StepBuilder("updateChatRoomStep", jobRepository)
                .<ChatRoom, ChatRoom>chunk(10, transactionManager)
                .reader(chatRoomItemReader())
                .processor(chatRoomItemProcessor())
                .writer(chatRoomItemWriter())
                .build();
    }

    @Bean
    public ItemReader<ChatRoom> chatRoomItemReader() {
        return new JpaPagingItemReaderBuilder<ChatRoom>()
                .name("chatRoomItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT c FROM ChatRoom c ORDER BY c.id ASC")
                .pageSize(10)
                .build();
    }

    @Bean
    public ItemProcessor<ChatRoom, ChatRoom> chatRoomItemProcessor() {
        return chatRoom -> {

            // ------------------------------
            // 채팅방 내 목표 달성 인원, 지출 평균 계산
            // ------------------------------

            // 특정 채팅방의 유저 리스트
            List<Long> userIdList = chatRoomMembershipRepository.findUserIdByChatroomId(chatRoom.getId());

            int achievedCount = 0; // 하루 목표 달성한 유저 수
            int sumAmount = 0; // 지출 통계를 위한 합계
            LocalDate yesterday = LocalDate.now().minusDays(1);
            for (Long userId : userIdList) {
                // 어제 지출 총합 계산
                Long totalExpenditureLong = (Long) entityManager.createQuery(
                                "SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.day = :yesterday")
                        .setParameter("userId", userId)
                        .setParameter("yesterday", yesterday)
                        .getSingleResult();

                int totalExpenditure = totalExpenditureLong.intValue();

                // 하루 목표 달성한 유저 수 count (유저의 하루 지출 총액 <= 채팅방의 하루 목표 지출 금액)
                if (totalExpenditure <= chatRoom.getDailySpendingGoalFilter()) {
                    achievedCount++;
                }
                sumAmount += totalExpenditure; // 유저들의 하루 지출
            }

            // 채팅방 총 참여자 수 TODO: 테스트 후 데이터 정리하고 아래 코드로 변경
            int chatRoomUserCount = chatRoomMembershipRepository.countByChatroomId(chatRoom.getId());
//            int chatRoomUserCount = chatRoom.getParticipationCount();

            // 지출 평균 계산
            int averageExpense = sumAmount / chatRoomUserCount;

            chatRoom.setAchievedCount(achievedCount); // 목표 달성 인원 설정
            chatRoom.setAverageExpense(averageExpense); // 지출 평균 설정

            // ------------------------------
            // 채팅방 랭킹 (백분위) 계산
            // ------------------------------

            AgeGroup ageFilter = chatRoom.getAgeGroupFilter(); // age 필터
            com.umc.yeongkkeul.domain.enums.Job jobFilter = chatRoom.getJobFilter(); // job 필터

            // job, age 기준으로 필터링 (totalScore null 아닌 방만 랭킹 집계)
            List<ChatRoom> chatRoomList = new ArrayList<>();
            if (ageFilter != null && !"UNDECIDED".equals(ageFilter) && jobFilter != null && !"UNDECIDED".equals(jobFilter)) { // age, job 대상
                chatRoomList = chatRoomRepository.findAllByAgeGroupFilterAndJobFilterOrderByTotalScoreDesc(ageFilter, jobFilter);
            } else if (ageFilter != null && !"UNDECIDED".equals(ageFilter) && (jobFilter == null || "UNDECIDED".equals(jobFilter))) { // age 대상
                chatRoomList = chatRoomRepository.findAllByAgeGroupFilterOrderByTotalScoreDesc(ageFilter);
            } else if (jobFilter != null && !"UNDECIDED".equals(jobFilter) && (ageFilter == null || "UNDECIDED".equals(ageFilter))) { // job 대상
                chatRoomList = chatRoomRepository.findAllByJobFilterOrderByTotalScoreDesc(jobFilter);
            } else { // 필터 없을 때는 전체 대상
                chatRoomList = chatRoomRepository.findAllByOrderByTotalScoreDesc();
            }

            // 채팅방 백분위 계산
            Double topRate = null; // 5명 이하일 때 null 반환 위해 null 초기화
            int ranking = 1;
            for (int i = 0; i < chatRoomList.size(); i++) {
                if (chatRoomList.get(i).getId().equals(chatRoom.getId())) {
                    ranking = i + 1;
                    break;
                }
            }
            topRate = ((double) ranking / chatRoomList.size()) * 100.0;

            chatRoom.setRanking(topRate);

            return chatRoom;
        };
    }

    @Bean
    public ItemWriter<ChatRoom> chatRoomItemWriter() {
        return new JpaItemWriterBuilder<ChatRoom>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}
