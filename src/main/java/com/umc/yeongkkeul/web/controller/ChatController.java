package com.umc.yeongkkeul.web.controller;

import com.umc.yeongkkeul.apiPayload.ApiResponse;
import com.umc.yeongkkeul.apiPayload.code.status.ErrorStatus;
import com.umc.yeongkkeul.apiPayload.exception.handler.ChatRoomHandler;
import com.umc.yeongkkeul.aws.s3.AmazonS3Manager;
import com.umc.yeongkkeul.service.ChatService;
import com.umc.yeongkkeul.web.dto.chat.EnterMessageDto;
import com.umc.yeongkkeul.web.dto.chat.MessageDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.http.*;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
                .id(enterMessageDto.id()) // 메시지 ID
                .messageType(enterMessageDto.messageType()) // 메시지 타입
                .content(enterMessageDto.senderId() + "님이 채팅방에 입장하였습니다.") // 입장 메시지 내용
                .chatRoomId(enterMessageDto.chatRoomId()) // 채팅방 ID
                .senderId(enterMessageDto.senderId()) // 발신자 ID
                .timestamp(enterMessageDto.timestamp()) // 메시지 타임스탬프
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
     * 채팅방에 업로드된 이미지 목록 조회 API
     * messageType이 "IMAGE"인 메시지의 S3 key를 활용해 이미지 URL을 생성합니다.
     */
    @GetMapping("/{chatRoomId}/images")
    @Operation(summary = "채팅방 이미지 조회", description = "채팅방에 업로드된 이미지 목록(이미지 URL)을 조회합니다.")
    public ApiResponse<List<String>> getChatRoomImages(@PathVariable Long chatRoomId) {
        List<String> imageUrls = chatService.getChatRoomImageUrls(chatRoomId);
        return ApiResponse.onSuccess(imageUrls);
    }

    /**
     * 채팅방 이미지 다운로드 API
     * 특정 이미지 메시지(messageId)에 대해, S3에서 파일 데이터를 다운로드한 후 원본 콘텐츠 타입에 맞게 응답 헤더를 설정합니다.
     */
    @GetMapping("/{chatRoomId}/images/{messageId}/download")
    @Operation(summary = "채팅방 이미지 다운로드", description = "채팅방에 업로드된 이미지를 다운로드합니다.")
    public ResponseEntity<byte[]> downloadChatImage(@PathVariable Long chatRoomId, @PathVariable Long messageId) {
        AmazonS3Manager.S3DownloadResponse downloadResponse = chatService.downloadChatImage(chatRoomId, messageId);

        HttpHeaders headers = new HttpHeaders();
        // S3에 저장된 원본 콘텐츠 타입을 사용합니다.
        headers.setContentType(MediaType.parseMediaType(downloadResponse.getContentType()));
        headers.setContentLength(downloadResponse.getData().length);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("message_" + messageId)
                .build());

        return new ResponseEntity<>(downloadResponse.getData(), headers, HttpStatus.OK);
    }

    /**
     * 채팅 이미지 업로드 API
     * 클라이언트는 이미지를 업로드한 후, 반환된 이미지 URL을 포함하여 채팅 메시지를 전송할 수 있습니다.
     *
     * @param chatRoomId 채팅방 ID
     * @param file 업로드할 이미지 파일 (Multipart 형식)
     * @return S3에 저장된 이미지 URL
     */
    @PostMapping("/{chatRoomId}/images")
    @Operation(summary = "채팅 이미지 업로드", description = "채팅 이미지를 S3에 업로드하고 이미지 URL을 반환합니다.")
    public ApiResponse<String> uploadChatImage(@PathVariable Long chatRoomId,
                                               @RequestParam("file") MultipartFile file) {
        String imageUrl = chatService.uploadChatImage(chatRoomId, file);
        return ApiResponse.onSuccess(imageUrl);
    }
}