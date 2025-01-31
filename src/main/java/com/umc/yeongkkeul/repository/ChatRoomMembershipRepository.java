package com.umc.yeongkkeul.repository;

import com.umc.yeongkkeul.domain.mapping.ChatRoomMembership;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomMembershipRepository extends JpaRepository<ChatRoomMembership, Long> {
}