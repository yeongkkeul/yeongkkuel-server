package com.umc.yeongkkeul.repository;

import com.umc.yeongkkeul.domain.mapping.ChatRoomMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRoomMembershipRepository extends JpaRepository<ChatRoomMembership, Long> {

    @Query("SELECT c FROM ChatRoomMembership c WHERE c.chatroom.id = :chatRoomId ORDER BY c.userScore DESC")
    List<ChatRoomMembership> findByChatroomIdOrderByUserScoreDesc(@Param("chatRoomId") Long chatRoomId);
    
}
