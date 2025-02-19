package com.umc.yeongkkeul.repository;

import com.umc.yeongkkeul.domain.ChatRoom;
import com.umc.yeongkkeul.domain.enums.AgeGroup;
import com.umc.yeongkkeul.domain.enums.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("SELECT c FROM ChatRoom c " +
            "WHERE (:age IS NULL OR c.ageGroupFilter = :age) " +
            "AND (c.dailySpendingGoalFilter >= COALESCE(:minAmount, 0)) " +
            "AND (c.dailySpendingGoalFilter <= COALESCE(:maxAmount, 2147483647)) " +
            "AND (:job IS NULL OR c.jobFilter = :job)" +
            "ORDER BY c.participationCount DESC")
    Page<ChatRoom> findAllWithPagination(@Param("age") AgeGroup age,
                                         @Param("minAmount") Integer minAmount,
                                         @Param("maxAmount") Integer maxAmount,
                                         @Param("job") Job job,
                                         Pageable pageable);

    Page<ChatRoom> findByTitleContainingOrderByParticipationCountDesc(String title, Pageable pageable);

    @Query("SELECT c FROM ChatRoom c WHERE c.id NOT IN :chatRoomIds ORDER BY FUNCTION('RAND')")
    Page<ChatRoom> findRandomByIdNotIn(@Param("chatRoomIds") List<Long> chatRoomIds, Pageable pageable);

    @Query("SELECT c FROM ChatRoom c WHERE c.id IN :chatRoomIds")
    List<ChatRoom> findAllByIdIn(@Param("chatRoomIds") List<Long> chatRoomIds);

    @Query("SELECT c FROM ChatRoom c JOIN c.chatRoomMembershipList m " +
            "WHERE c.ageGroupFilter = :ageGroupFilter AND c.jobFilter = :jobFilter " +
            "AND c.totalScore IS NOT NULL " +
            "ORDER BY c.totalScore DESC")
    List<ChatRoom> findAllByAgeGroupFilterAndJobFilterOrderByTotalScoreDesc(@Param("ageGroupFilter") AgeGroup ageGroupFilter,
                                                                            @Param("jobFilter") Job jobFilter);

    @Query("SELECT c FROM ChatRoom c JOIN c.chatRoomMembershipList m " +
            "WHERE c.ageGroupFilter = :ageGroupFilter " +
            "AND c.totalScore IS NOT NULL " +
            "ORDER BY c.totalScore DESC")
    List<ChatRoom> findAllByAgeGroupFilterOrderByTotalScoreDesc(@Param("ageGroupFilter") AgeGroup ageGroupFilter);

    @Query("SELECT c FROM ChatRoom c JOIN c.chatRoomMembershipList m " +
            "WHERE c.jobFilter = :jobFilter " +
            "AND c.totalScore IS NOT NULL " +
            "ORDER BY c.totalScore DESC")
    List<ChatRoom> findAllByJobFilterOrderByTotalScoreDesc(@Param("jobFilter") Job jobFilter);

    @Query("SELECT c FROM ChatRoom c JOIN c.chatRoomMembershipList m " +
            "WHERE c.totalScore IS NOT NULL " +
            "ORDER BY c.totalScore DESC")
    List<ChatRoom> findAllByOrderByTotalScoreDesc();
}
