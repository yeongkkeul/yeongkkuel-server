package com.umc.yeongkkeul.batch;

import com.umc.yeongkkeul.domain.ChatRoom;
import com.umc.yeongkkeul.domain.mapping.ChatRoomMembership;
import com.umc.yeongkkeul.repository.ChatRoomMembershipRepository;
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

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableBatchProcessing
public class ChatRoomScoreBatchConfig {

    private final JobRepository jobRepository;
    private final ChatRoomMembershipRepository chatRoomMembershipRepository;
    private final EntityManagerFactory entityManagerFactory;
    private final PlatformTransactionManager transactionManager;

    // ChatRoomMembership 엔티티에서 chat_room_id 같은 사람들끼리의 score를 통해 ChatRoom의 totalScore 계산
    @Bean
    public Job updateChatRoomScoreJob() {
        return new JobBuilder("updateChatRoomScoreJob", jobRepository)
                .start(updateChatRoomScoreStep())
                .build();
    }

    @Bean
    public Step updateChatRoomScoreStep() {
        return new StepBuilder("updateChatRoomScoreStep", jobRepository)
                .<ChatRoom, ChatRoom>chunk(10, transactionManager)
                .reader(chatRoomScoreItemReader())
                .processor(chatRoomScoreItemProcessor())
                .writer(chatRoomScoreItemWriter())
                .build();
    }

    @Bean
    public ItemReader<ChatRoom> chatRoomScoreItemReader() {
        return new JpaPagingItemReaderBuilder<ChatRoom>()
                .name("chatRoomScoreItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT c FROM ChatRoom c ORDER BY c.id ASC")
                .pageSize(10)
                .build();
    }

    @Bean
    public ItemProcessor<ChatRoom, ChatRoom> chatRoomScoreItemProcessor() {
        return chatRoom -> {
            List<ChatRoomMembership> chatRoomMembershipList = chatRoomMembershipRepository.findByChatroomIdOrderByUserScoreDesc(chatRoom.getId());

            if (chatRoomMembershipList.size() >= 5) { // 5명 이상인 방만 점수 계산

                // userScore가 null 아닌 유저만 남기기
                chatRoomMembershipList = chatRoomMembershipList.stream()
                        .filter(membership -> membership.getUserScore() != null)
                        .toList();

                // totalScore = 참여자들의 score 합
                double totalScore = chatRoomMembershipList.stream()
                        .mapToDouble(ChatRoomMembership::getUserScore)
                        .sum();

                // 해당 채팅방의 상위 10% 사용자 score만 top10PercentScores 리스트에 추가
                List<ChatRoomMembership> top10ScoreList = chatRoomMembershipList.stream()
                        .limit((int) (chatRoomMembershipList.size() * 0.1))
                        .toList();

                // 상위 10% 평균 점수 계산
                double top10Average = top10ScoreList.stream()
                        .mapToDouble(ChatRoomMembership::getUserScore)
                        .average()
                        .orElse(0);

                /*
                최종 점수 = 상위 사용자 고려 점수 * log(해당 chatRoom 사람수 + 1)
                        = ((totalScore/해당 chatRoom 사람 수)*0.8 + (해당 chatRoom에서 score가 상위 10%인 사람들의 평균 * 0.2))
                               * log(해당 chatRoom 사람수 + 1)
                */
                double finalScore = ((totalScore / chatRoomMembershipList.size()) * 0.8 + (top10Average * 0.2)) * (Math.log(chatRoomMembershipList.size() + 1));

                chatRoom.setTotalScore(finalScore); // 최종 점수 설정
            } else {
                chatRoom.setTotalScore(null);
            }

            return chatRoom;
        };
    }

    @Bean
    public ItemWriter<ChatRoom> chatRoomScoreItemWriter() { // 최종 점수 저장
        return new JpaItemWriterBuilder<ChatRoom>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

}
