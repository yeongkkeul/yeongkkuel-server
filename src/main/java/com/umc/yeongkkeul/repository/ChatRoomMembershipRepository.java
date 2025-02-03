package com.umc.yeongkkeul.repository;

import com.umc.yeongkkeul.domain.ChatRoom;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.domain.mapping.ChatRoomMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ChatRoomMembershipRepository extends JpaRepository<ChatRoomMembership, Long> {

    Optional<ChatRoomMembership> findByUserIdAndChatroomId(Long userId, Long chatRoomId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ChatRoomMembership c WHERE c.chatroom.id = :chatRoomId")
    void deleteChatRoomMemberships(@Param("chatRoomId") Long chatRoomId);
}