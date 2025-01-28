package com.umc.yeongkkeul.repository;

import com.umc.yeongkkeul.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
}
