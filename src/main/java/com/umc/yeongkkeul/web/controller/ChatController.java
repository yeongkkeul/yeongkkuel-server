package com.umc.yeongkkeul.web.controller;

import com.github.f4b6a3.tsid.TsidCreator;
import com.umc.yeongkkeul.apiPayload.code.status.ErrorStatus;
import com.umc.yeongkkeul.apiPayload.exception.handler.ChatRoomHandler;
import com.umc.yeongkkeul.service.ChatService;
import com.umc.yeongkkeul.web.dto.chat.EnterMessageDto;
import com.umc.yeongkkeul.web.dto.chat.MessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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

        MessageDto message = MessageDto.builder()
                .id(TsidCreator.getTsid().toLong()) // TSID ID 생성기, 시간에 따라 ID에 영향이 가고 최신 데이터일수록 ID 값이 커진다.
                .chatRoomId(messageDto.chatRoomId())
                .senderId(messageDto.senderId())
                .messageType(messageDto.messageType())
                .content(messageDto.content())
                .timestamp(LocalDateTime.now().toString())
                .build();

        chatService.sendMessage(message); // 메시지 전송

        log.info("Send a message to the group chat room with roomID");
        chatService.saveMessages(message); // 메시지 저장 TODO: 나중에 Consumer를 통해서 저장하자
    }

    /**
     * 유저가 특정 채팅방(roomId)에 입장했을 때 처리.
     * "chat.enter.{roomId}" 경로로 STOMP 메시지가 전송되면 호출.
     * FIXME: message 특성 상 로그인 한 사용자를 알기 힘드므로 보안 상의 문제가 있을 수도 있다.
     *
     * @param roomId      채팅방 ID
     * @param enterMessageDto  전송된 메시지 데이터
     */
    @MessageMapping("chat.enter.{roomId}")
    public void enterUser(@DestinationVariable("roomId") Long roomId, EnterMessageDto enterMessageDto) {

        // 비밀번호 재확인 -> 앞선 과정에서 했지만 보안을 위해서 한 번 더 해야한다.
        if (!chatService.validateChatRoomPassword(enterMessageDto.chatRoomId(), enterMessageDto.password())) {
            log.error("채팅방의 비밀번호가 일치하지 않습니다.");
            throw new ChatRoomHandler(ErrorStatus._CHATROOM_NO_PERMISSION);
        }

        // 유저 입장을 알리는 메시지 생성
        MessageDto messageDto = MessageDto.builder()
                .id(TsidCreator.getTsid().toLong()) // 메시지 ID
                .messageType(enterMessageDto.messageType()) // 메시지 타입
                .content(enterMessageDto.senderId() + "님이 채팅방에 입장하였습니다.") // 입장 메시지 내용
                .chatRoomId(enterMessageDto.chatRoomId()) // 채팅방 ID
                .senderId(enterMessageDto.senderId()) // 발신자 ID
                .timestamp(LocalDateTime.now().toString()) // 메시지 타임스탬프
                .build();

        // FIXME: Exception 예외 처리 추가 코드가 필요
        // 사용자-채팅방 관계 테이블 저장과 가입 메시지 전송.
        try {
            chatService.joinChatRoom(enterMessageDto.senderId(), roomId, messageDto);
        }
        catch (AmqpException e) {
            log.error("The message was not sent by AmqpException {}.", e); return;
        } catch (Exception e) {
            log.error("error {}.", e); return;
        }

        log.info("The user with senderID has entered the chat room."); // JPA 저장과 메시지 전송이 성공함.
        chatService.saveMessages(messageDto); // Redis에 가입 메시지 저장
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
                .id(TsidCreator.getTsid().toLong())
                .messageType(messageDto.messageType())
                .content(messageDto.senderId() + "님이 채팅방에 퇴장하였습니다.")
                .chatRoomId(messageDto.chatRoomId())
                .senderId(messageDto.senderId())
                .timestamp(LocalDateTime.now().toString())
                .build();

        try {
            chatService.exitChatRoom(messageDto.senderId(), roomId, messageDto);
        } catch (AmqpException e) {
            log.error("The message was not sent by AmqpException {}.", e); return;
        } catch (Exception e) {
            log.error("error {}.", e); return;
        }

        log.info("The user with senderID has left the chat room.");
        chatService.saveMessages(exitMessageDto);
    }


}