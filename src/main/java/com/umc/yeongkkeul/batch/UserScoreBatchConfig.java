package com.umc.yeongkkeul.batch;

import com.umc.yeongkkeul.domain.*;
import com.umc.yeongkkeul.domain.mapping.ChatRoomMembership;
import com.umc.yeongkkeul.repository.ChatRoomMembershipRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableBatchProcessing
public class UserScoreBatchConfig {

    private final EntityManagerFactory entityManagerFactory;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public UserScoreBatchConfig(EntityManagerFactory entityManagerFactory,
                                JobRepository jobRepository,
                                PlatformTransactionManager transactionManager) {
        this.entityManagerFactory = entityManagerFactory;
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    public Job updateUserScoreJob() {
        return new JobBuilder("updateUserScoreJob", jobRepository)
                .start(updateUserScoreStep())
                .build();
    }

    @Bean
    public Step updateUserScoreStep() {
        return new StepBuilder("updateUserScoreStep", jobRepository)
                .<User, ChatRoomMembership>chunk(10, transactionManager)
                .reader(userReader())
                .processor(userScoreProcessor())
                .writer(userScoreWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<User> userReader() {
        return new JpaPagingItemReaderBuilder<User>()
                .name("userReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT u FROM User u JOIN FETCH u.chatRoomMembershipList")
                .pageSize(10)
                .build();
    }

    @Bean
    public ItemProcessor<User, ChatRoomMembership> userScoreProcessor() {
        return user -> {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            try {

                int totalCategories = user.getCategoryList().size();
                Integer dayTargetExpenditure = user.getDayTargetExpenditure();
                long totalExpenditure = entityManager.createQuery(
                                "SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.day = :yesterday", Long.class)
                        .setParameter("userId", user.getId())
                        .setParameter("yesterday", LocalDate.now().minusDays(1))
                        .getSingleResult();

                long noSpendingCount = entityManager.createQuery(
                                "SELECT COUNT(e) FROM Expense e WHERE e.user.id = :userId AND e.isNoSpending = true AND e.day = :yesterday", Long.class)
                        .setParameter("userId", user.getId())
                        .setParameter("yesterday", LocalDate.now().minusDays(1))
                        .getSingleResult();

                long filledCategories = entityManager.createQuery(
                                "SELECT COUNT(DISTINCT e.category.id) FROM Expense e WHERE e.user.id = :userId AND e.day = :yesterday", Long.class)
                        .setParameter("userId", user.getId())
                        .setParameter("yesterday", LocalDate.now().minusDays(1))
                        .getSingleResult();


                double score = 0.0;
                if (dayTargetExpenditure != null && dayTargetExpenditure > 0) {
                    score += (100 - (double) totalExpenditure / dayTargetExpenditure * 100) * 0.5;
                }
                if (totalCategories > 0) {
                    score += ((double) noSpendingCount / totalCategories * 100) * 0.3;
                    score += ((double) filledCategories / totalCategories * 100) * 0.2;
                }


                List<ChatRoomMembership> memberships = user.getChatRoomMembershipList();
                for (ChatRoomMembership membership : memberships) {
                    membership.setUserScore(score);
                }

                return memberships.isEmpty() ? null : memberships.get(0);
            } finally {
                entityManager.close();
            }
        };
    }

    @Bean
    @Transactional
    public ItemWriter<ChatRoomMembership> userScoreWriter() {
        JpaItemWriter<ChatRoomMembership> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter::write;
    }
}