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
import org.springframework.batch.item.support.CompositeItemProcessor;
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
                .processor(chatRoomCompositeProcessor())
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
    public ItemProcessor<ChatRoom, ChatRoom> expenseProcessor() { // 목표 달성 인원, 지출 평균 계산
        return chatRoom -> {
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

                if (totalExpenditure <= chatRoom.getDailySpendingGoalFilter()) {
                    achievedCount++;
                }
                sumAmount += totalExpenditure; // 유저들의 하루 지출
            }

            // 채팅방 총 참여자 수
//            int chatRoomUserCount = chatRoomMembershipRepository.countByChatroomId(chatRoom.getId());
            int chatRoomUserCount = chatRoom.getParticipationCount();

            // 지출 평균 계산
            int averageExpense = sumAmount / chatRoomUserCount;

            chatRoom.setAchievedCount(achievedCount); // 목표 달성 인원 설정
            chatRoom.setAverageExpense(averageExpense); // 지출 평균 설정

            return chatRoom;
        };
    }

    @Bean
    public ItemProcessor<ChatRoom, ChatRoom> rankingProcessor() { // 백분위 계산 로직
        return chatRoom -> {
            if (chatRoom.getTotalScore() == null) { // totalScore가 null인 경우(5명 이하인 채팅방은 null)
                return null;
            }

            AgeGroup ageFilter = chatRoom.getAgeGroupFilter(); // age 필터
            com.umc.yeongkkeul.domain.enums.Job jobFilter = chatRoom.getJobFilter(); // job 필터

            // job, age 기준으로 필터링 (totalScore null 아닌 방만 랭킹 집계)
            boolean hasAgeFilter = ageFilter!=null && ageFilter!=AgeGroup.UNDECIDED;
            boolean hasJobFilter = jobFilter!=null && jobFilter!=com.umc.yeongkkeul.domain.enums.Job.UNDECIDED;
            List<ChatRoom> chatRoomList;
            if (hasAgeFilter && hasJobFilter) { // age, job 대상
                chatRoomList = chatRoomRepository.findAllByAgeGroupFilterAndJobFilterOrderByTotalScoreDesc(ageFilter, jobFilter);
            } else if (hasAgeFilter) { // age 대상
                chatRoomList = chatRoomRepository.findAllByAgeGroupFilterOrderByTotalScoreDesc(ageFilter);
            } else if (hasJobFilter) { // job 대상
                chatRoomList = chatRoomRepository.findAllByJobFilterOrderByTotalScoreDesc(jobFilter);
            } else { // 필터 없을 때는 전체 대상
                chatRoomList = chatRoomRepository.findAllByOrderByTotalScoreDesc();
            }

            int rank = 1;
            for (int i = 0; i < chatRoomList.size(); i++) {
                if (chatRoomList.get(i).getId().equals(chatRoom.getId())) {
                    rank = i + 1;
                    break;
                }
            }

            int topRate = (int) Math.round(((double) rank / chatRoomList.size()) * 100);
            chatRoom.setRanking(topRate);

            return chatRoom;
        };
    }

    @Bean
    public CompositeItemProcessor<ChatRoom, ChatRoom> chatRoomCompositeProcessor() {
        List<ItemProcessor<ChatRoom, ChatRoom>> processors = new ArrayList<>();
        processors.add(expenseProcessor());  // 목표 달성 인원 및 지출 평균 계산
        processors.add(rankingProcessor());  // 백분위 계산

        CompositeItemProcessor<ChatRoom, ChatRoom> processor = new CompositeItemProcessor<>();
        processor.setDelegates(processors);

        return processor;
    }

    @Bean
    public ItemWriter<ChatRoom> chatRoomItemWriter() {
        return new JpaItemWriterBuilder<ChatRoom>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}
