package com.umc.yeongkkeul.web.controller;

import com.umc.yeongkkeul.service.ChatService;
import com.umc.yeongkkeul.web.dto.ChatRoomDetailRequestDto;
import com.umc.yeongkkeul.web.dto.MessageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.umc.yeongkkeul.security.FindLoginUser.getCurrentUserId;
import static com.umc.yeongkkeul.security.FindLoginUser.toId;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
@Tag(name = "채팅 API", description = "채팅, 채팅방 관련 HTTP API 입니다.")
public class ChatAPIController {

    private final ChatService chatService;

    /**
     * 특정 채팅방의 모든 메시지를 조회.
     * 클라이언트가 REST API로 "/chat/{chatRoomId}" 경로에 GET 요청을 보낼 때 호출.
     *
     * @param chatRoomId  조회할 채팅방 ID
     * @return ResponseEntity<List<MessageDto>> 채팅 메시지 리스트
     *
     * 주의: 이 메서드는 서버 DB에서 데이터를 반복적으로 가져오므로 성능 문제가 발생할 수 있음.
     *       가능한 한 호출 횟수를 줄이는 방식으로 개선 필요.
     */
    // TODO: 로컬 DB와 서버 DB의 사용 여부에 따라 로직을 수정해야 한다.
    @GetMapping("/{chatRoomId}")
    @Operation(summary = "특정 채팅방 메시지 조회", description = "특정 채팅방의 모든 메시지를 조회합니다.")
    public ResponseEntity<List<MessageDto>> getChatMessages(@PathVariable Long chatRoomId) {

        Long userId = toId(getCurrentUserId());

        List<MessageDto> messageDtos = chatService.getMessages(chatRoomId);

        return ResponseEntity.ok().body(messageDtos);
    }

    /**
     * @param chatRoomDetailRequestDto 채팅방 생성 DTO
     * @return 로그인한 사용자를 방장으로 한 채팅방을 생성하고 채팅방의 ID를 반환합니다.
     */
    @PostMapping
    @Operation(summary = "채팅방 생성", description = "그룹 채팅방을 생성합니다.")
    public ResponseEntity<Long> createChatRoom(@RequestBody @Valid ChatRoomDetailRequestDto chatRoomDetailRequestDto) {

        Long userId = toId(getCurrentUserId());

        return ResponseEntity.ok().body(chatService.createChatRoom(userId, chatRoomDetailRequestDto));
    }
}
