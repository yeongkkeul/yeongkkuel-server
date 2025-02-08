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
            "AND (:job IS NULL OR c.jobFilter = :job)")
    Page<ChatRoom> findAllWithPagination(@Param("age") AgeGroup age,
                                         @Param("minAmount") Integer minAmount,
                                         @Param("maxAmount") Integer maxAmount,
                                         @Param("job") Job job,
                                         Pageable pageable);

    Page<ChatRoom> findByTitleContainingOrderByParticipationCountDesc(String title, Pageable pageable);

    @Query("SELECT c FROM ChatRoom c WHERE c.id IN :chatRoomIds")
    List<ChatRoom> findAllByIdIn(@Param("chatRoomIds") List<Long> chatRoomIds);

    List<ChatRoom> findAllByAgeGroupFilterAndJobFilterOrderByTotalScoreDesc(AgeGroup ageGroupFilter, Job jobFilter);

    List<ChatRoom> findAllByAgeGroupFilterOrderByTotalScoreDesc(AgeGroup ageGroupFilter);

    List<ChatRoom> findAllByJobFilterOrderByTotalScoreDesc(Job jobFilter);
}
