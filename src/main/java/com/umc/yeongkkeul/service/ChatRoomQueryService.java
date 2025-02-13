package com.umc.yeongkkeul.service;

import com.umc.yeongkkeul.apiPayload.code.status.ErrorStatus;
import com.umc.yeongkkeul.apiPayload.exception.GeneralException;
import com.umc.yeongkkeul.domain.ChatRoom;
import com.umc.yeongkkeul.repository.ChatRoomRepository;
import com.umc.yeongkkeul.web.dto.BannerResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ChatRoomQueryService {

    private final ChatRoomRepository chatRoomRepository;

    public BannerResponseDto getChatRoomBanner(Long chatRoomId) {

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new GeneralException(ErrorStatus._CHATROOM_NOT_FOUND));

        // 배너 기준 날짜 (공지하는 날짜 = 오늘)
        String createdAt = LocalDate.now().format(DateTimeFormatter.ofPattern("MM.dd"));

        return BannerResponseDto.from(chatRoom, createdAt);
    }
}
