package com.umc.yeongkkeul.repository;

import com.umc.yeongkkeul.domain.mapping.ChatRoomMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ChatRoomMembershipRepository extends JpaRepository<ChatRoomMembership, Long> {

    @Query("SELECT c FROM ChatRoomMembership c WHERE c.chatroom.id = :chatRoomId ORDER BY c.userScore DESC")
    List<ChatRoomMembership> findByChatroomIdOrderByUserScoreDesc(@Param("chatRoomId") Long chatRoomId);

    Optional<ChatRoomMembership> findByUserIdAndChatroomId(Long userId, Long chatRoomId);

    List<ChatRoomMembership> findAllByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ChatRoomMembership c WHERE c.chatroom.id = :chatRoomId")
    void deleteChatRoomMemberships(@Param("chatRoomId") Long chatRoomId);

    int countByChatroomId(Long chatRoomId);

    @Query("SELECT cm.user.id FROM ChatRoomMembership cm WHERE cm.chatroom.id = :chatRoomId")
    List<Long> findUserIdByChatroomId(@Param("chatRoomId") Long chatRoomId);
}
