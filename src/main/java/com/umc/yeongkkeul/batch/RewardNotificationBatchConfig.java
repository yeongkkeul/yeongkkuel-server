package com.umc.yeongkkeul.batch;

import com.umc.yeongkkeul.domain.ChatRoom;
import com.umc.yeongkkeul.domain.Notification;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.domain.enums.NotificationType;
import com.umc.yeongkkeul.domain.mapping.ChatRoomMembership;
import com.umc.yeongkkeul.domain.mapping.NotificationRead;
import com.umc.yeongkkeul.repository.*;
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
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;


@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class RewardNotificationBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMembershipRepository chatRoomMembershipRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationReadRepository notificationReadRepository;
    private final UserRepository userRepository;

    @Bean
    public Job updateRewardNotificationJob() {
        return new JobBuilder("updateRewardNotificationJob", jobRepository)
                .start(updateRewardNotificationStep())
                .build();
    }

    @Bean
    public Step updateRewardNotificationStep() {
        return new StepBuilder("updateRewardNotificationStep", jobRepository)
                .<ChatRoom, ChatRoom>chunk(10, transactionManager)
                .reader(chatRoomReader())
                .processor(chatRoomProcessor())
                .writer(chatRoomWriter())
                .build();
    }

    @Bean
    public ItemReader<ChatRoom> chatRoomReader() {
        return new ListItemReader<>(chatRoomRepository.findAll());
    }

    @Bean
    public ItemProcessor<ChatRoom, ChatRoom> chatRoomProcessor() {
        return chatRoom -> {
            int totalMembers = chatRoom.getParticipationCount();
            int achievedMembers = chatRoom.getAchievedCount();
            double achievementRate = (double) achievedMembers / totalMembers * 100;

            List<ChatRoomMembership> members = chatRoomMembershipRepository.findAllByChatroom(chatRoom);

            // 채팅방 전체 랭킹 보상 적용
            int rewardAmount = 0;
            if (chatRoom.getRanking() == 1) {
                rewardAmount += 5;
            } else if (chatRoom.getRanking() <= 5) {
                rewardAmount += 2;
            }

            // 달성률 보상 적용
            if (achievementRate == 100) {
                rewardAmount += 5;
            } else if (achievementRate >= 70) {
                rewardAmount += 2;
            }

            // 채팅방 내 사용자 점수에 따른 랭킹 계산
            TreeMap<Double, List<ChatRoomMembership>> scoreToUsersMap = new TreeMap<>(Collections.reverseOrder());
            scoreToUsersMap.putAll(members.stream()
                    .collect(Collectors.groupingBy(ChatRoomMembership::getUserScore)));

            int rank = 1;
            for (Map.Entry<Double, List<ChatRoomMembership>> entry : scoreToUsersMap.entrySet()) {
                List<ChatRoomMembership> rankedUsers = entry.getValue();
                int rankReward = 0;
                if (rank == 1) {
                    rankReward = 10;
                } else if (rank == 2) {
                    rankReward = 5;
                } else if (rank == 3) {
                    rankReward = 3;
                }
                for (ChatRoomMembership membership : rankedUsers) {
                    User user = membership.getUser();
                    user.setRewardBalance(user.getRewardBalance() + rewardAmount + rankReward);
                    userRepository.save(user);
                    createNotification(user, rewardAmount + rankReward);
                }
                rank += rankedUsers.size(); // 동점자는 같은 등수로 간주
                if (rank > 3) break; // 3등까지만 보상 지급
            }
            return chatRoom;
        };
    }

    private void createNotification(User user, int rewardAmount) {
        Notification notification = Notification.builder()
                .notificationType(NotificationType.AWARD_RANKING_REWARDS)
                .notificationContent("리워드 " + rewardAmount + " 지급되었습니다.")
                .build();
        notificationRepository.save(notification);

        NotificationRead notificationRead = NotificationRead.builder()
                .userId(user)
                .notificationId(notification)
                .isRead(false)
                .build();
        notificationReadRepository.save(notificationRead);
    }

    @Bean
    public ItemWriter<ChatRoom> chatRoomWriter() {
        return chatRooms -> chatRoomRepository.saveAll(chatRooms);
    }


}
