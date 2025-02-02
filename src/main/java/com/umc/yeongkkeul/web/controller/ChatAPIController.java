package com.umc.yeongkkeul.web.controller;

import com.umc.yeongkkeul.apiPayload.ApiResponse;
import com.umc.yeongkkeul.aws.s3.AmazonS3Manager;
import com.umc.yeongkkeul.service.ChatService;
import com.umc.yeongkkeul.web.dto.chat.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    // TODO: 로컬 DB에 저장한다고 해도 채팅방 상태가 바뀔 수도 있기 때문에 이를 지속적으로 추적하거나 요청해도 변경점을 찾아야 하는 로직이 필요하다.
    @GetMapping("/{chatRoomId}")
    @Operation(summary = "특정 채팅방 메시지 조회", description = "특정 채팅방의 모든 메시지를 조회합니다.")
    public ApiResponse<List<MessageDto>> getChatMessages(@PathVariable Long chatRoomId) {

        Long userId = toId(getCurrentUserId());

        List<MessageDto> messageDtos = chatService.getMessages(chatRoomId);

        return ApiResponse.onSuccess(messageDtos);
    }

    /**
     * @param chatRoomDetailRequestDto 채팅방 생성 DTO
     * @return 로그인한 사용자를 방장으로 한 채팅방을 생성하고 채팅방의 ID를 반환합니다.
     */
    @PostMapping
    @Operation(summary = "채팅방 생성", description = "그룹 채팅방을 생성합니다.")
    public ApiResponse<Long> createChatRoom(@RequestBody @Valid ChatRoomDetailRequestDto chatRoomDetailRequestDto) {

        Long userId = toId(getCurrentUserId());

        return ApiResponse.onSuccess(chatService.createChatRoom(userId, chatRoomDetailRequestDto));
    }

    // FIXME: 해당 API를 쓸지 생각해보자. STOMP로 보내줘도 됨.
    /*
    @PostMapping("/{chatRoomId}")
    @Operation(summary = "채팅방 가입", description = "그룹 채팅방을 가입합니다.")
    public ResponseEntity<Long> joinChatRoom(@PathVariable Long chatRoomId) {

        Long userId = toId(getCurrentUserId());

        return ResponseEntity.ok().body(chatService.joinChatRoom(userId, chatRoomId));
    }
     */

    @PostMapping("/{chatRoomId}/validate")
    @Operation(summary = "채팅방 패스워드 확인", description = "그룹 채팅방을 가입 할 때 사용하는 패스워드를 사용합니다. 채팅방 정보 조회의 isPassword를 통해 패스워드 여부를 확인")
    public ApiResponse<Boolean> validateChatRoomPassword(@PathVariable Long chatRoomId, @RequestBody ChatRoomJoinPasswordRequestDto chatRoomJoinPasswordRequestDto) {

        Long userId = toId(getCurrentUserId());

        return ApiResponse.onSuccess(chatService.validateChatRoomPassword(chatRoomId, chatRoomJoinPasswordRequestDto.password()));
    }

    /**
     * @param chatRoomId
     * @return 채팅방 가입을 위해 사용자에게 보여줄 채팅방 정보를 조회합니다.
     */
    @GetMapping("/{chatRoomId}/detail")
    @Operation(summary = "채팅방 정보 조회", description = "특정 채팅방의 정보를 조회합니다.")
    public ApiResponse<ChatRoomDetailResponseDto> getChatRoomDetail(@PathVariable Long chatRoomId) {

        return ApiResponse.onSuccess(chatService.getChatRoomDetail(chatRoomId));
    }

    /**
     * 클라이언트는 메시지 타입을 통해 해당 메시지가 영수증인 것을 확인합니다.
     * 메시지 타입이 영수증이면 해당 메시지의 content에는 Expense 테이블의 ID가 있습니다.
     * 클라이언트는 테이블의 ID를 통해 영수증 조회 메서드를 GET 요청합니다.
     *
     * @param expenseId
     * @return
     */
    @GetMapping("/receipts/{expenseId}")
    @Operation(summary = "영수증 조회", description = "채팅방에 올라온 영수증의 정보를 조회합니다.")
    public ApiResponse<ReceiptMessageDto> getReceiptDetail(@PathVariable Long expenseId) {

        return ApiResponse.onSuccess(chatService.getReceipt(expenseId));
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