package com.umc.yeongkkeul.web.controller;

import com.umc.yeongkkeul.service.ChatService;
import com.umc.yeongkkeul.web.dto.MessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ChatController 클래스
 * 채팅 메시지 전송, 입장, 퇴장, 채팅방 내 메시지 조회 등의 기능을 제공하는 컨트롤러 클래스.
 * STOMP 메시지와 REST API를 함께 사용하여 채팅 기능을 구현.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 채팅 메시지를 특정 채팅방(roomId)으로 전송.
     * 클라이언트가 STOMP 프로토콜을 사용해 "chat.message.{roomId}" 경로로 메시지를 전송하면 처리.
     *
     * @param roomId      채팅방 ID (STOMP 경로 변수)
     * @param messageDto  전송된 메시지 데이터
     */
    @MessageMapping("chat.message.{roomId}")
    public void sendMessage(@DestinationVariable String roomId, @RequestBody MessageDto messageDto) {

        chatService.sendMessage(messageDto); // 메시지 전송
        log.info("Send a message to the group chat room with roomID");
        chatService.saveMessages(messageDto); // 메시지 저장 TODO: 나중에 Consumer를 통해서 저장하자
    }

    /**
     * 유저가 특정 채팅방(roomId)에 입장했을 때 처리.
     * "chat.enter.{roomId}" 경로로 STOMP 메시지가 전송되면 호출.
     *
     * @param roomId      채팅방 ID
     * @param messageDto  전송된 메시지 데이터
     */
    @MessageMapping("chat.enter.{roomId}")
    public void enterUser(@DestinationVariable("roomId") Long roomId, MessageDto messageDto) {

        // 유저 입장을 알리는 메시지 생성
        MessageDto enterMessageDto = MessageDto.builder()
                .id(messageDto.id()) // 메시지 ID
                .messageType(messageDto.messageType()) // 메시지 타입
                .content(messageDto.senderId() + "님이 채팅방에 입장하였습니다.") // 입장 메시지 내용
                .chatRoomId(messageDto.chatRoomId()) // 채팅방 ID
                .senderId(messageDto.senderId()) // 발신자 ID
                .timestamp(messageDto.timestamp()) // 메시지 타임스탬프
                .build();

        chatService.enterMessage(enterMessageDto);
        log.info("The user with senderID has entered the chat room.");
        chatService.saveMessages(enterMessageDto);
    }

    /**
     * 유저가 특정 채팅방(roomId)에서 퇴장했을 때 처리.
     * "chat.exit.{roomId}" 경로로 STOMP 메시지가 전송되면 호출.
     *
     * @param roomId      채팅방 ID
     * @param messageDto  전송된 메시지 데이터
     */
    @MessageMapping("chat.exit.{roomId}")
    public void exitUser(@DestinationVariable("roomId") Long roomId, MessageDto messageDto) {

        // 유저 퇴장을 알리는 메시지 생성
        MessageDto exitMessageDto = MessageDto.builder()
                .id(messageDto.id())
                .messageType(messageDto.messageType())
                .content(messageDto.senderId() + "님이 채팅방에 퇴장하였습니다.")
                .chatRoomId(messageDto.chatRoomId())
                .senderId(messageDto.senderId())
                .timestamp(messageDto.timestamp())
                .build();

        chatService.exitMessage(exitMessageDto);
        log.info("The user with senderID has left the chat room.");
        chatService.saveMessages(exitMessageDto);
    }

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
    @GetMapping("/chat/{chatRoomId}")
    public ResponseEntity<List<MessageDto>> getChatMessages(@PathVariable Long chatRoomId) {

        // TODO: 로그인한 회원의 ID

        // User - chatroom에서 해당 user가 구독하고 있는 채팅방의 메시지만 디비에서 가져옴.

        List<MessageDto> messageDtos = chatService.getMessages(chatRoomId);

        return ResponseEntity.ok().body(messageDtos);
    }
}