package com.umc.yeongkkeul.web.controller;

import com.umc.yeongkkeul.apiPayload.ApiResponse;
import com.umc.yeongkkeul.aws.s3.AmazonS3Manager;
import com.umc.yeongkkeul.service.ChatService;
import com.umc.yeongkkeul.web.dto.chat.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
     * 주의: 이 API는 단순히 테스트 용도로 사용.
     */
    @GetMapping("/{chatRoomId}/messages/test")
    @Operation(summary = "특정 채팅방 메시지 조회 - 테스트 용도", description = "특정 채팅방의 모든 메시지를 조회합니다. 실제 환경에서 채팅방의 모든 메시지를 가져오는 것은 성능의 문제가 있으므로 테스트 용도로 사용합니다.")
    public ApiResponse<List<MessageDto>> getChatMessages(@PathVariable Long chatRoomId) {

        Long userId = toId(getCurrentUserId());

        List<MessageDto> messageDtos = chatService.getMessages(chatRoomId);

        return ApiResponse.onSuccess(messageDtos);
    }

    @Operation(summary = "특정 채팅방의 메시지 동기화(조회)", description = "웹소켓이 재연결되거나 오류로 인해 메시지를 받지 못할 경우를 생각해서 특정 채팅방에 들어가면 항상 이 API를 호출합니다. 인터넷에 연결되지 않거나 다른 이유로 정상적인 응답을 받지 못하면 기존에 클라이언트에 저장되었던 정보를 화면에 유지합니다.")
    @GetMapping("/{chatRoomId}/messages")
    public ApiResponse<List<MessageDto>> synchronizationChatMessages(@PathVariable Long chatRoomId, @RequestParam("messageId") Long lastClientMessageId) {

        Long userId = toId(getCurrentUserId());

        return ApiResponse.onSuccess(chatService.synchronizationChatMessages(userId, chatRoomId, lastClientMessageId));
    }

    @Operation(summary = "클라이언트와 서버의 채팅방 정보 동기화(조회)", description = "웹소켓 연결이 끊어지면 클라이언트와 서버 간의 채팅방 정보가 일치 하지 않을 수 있기에 이 API를 호출해서 클라이언트가 서버의 데이터를 조회하도록 시켜줍니다.")
    @GetMapping
    public ApiResponse<List<ChatRoomInfoResponseDto>> synchronizationChatRoomsInfo() {

        Long userId = toId(getCurrentUserId());

        return ApiResponse.onSuccess(chatService.synchronizationChatRoomsInfo(userId));
    }

    @Operation(summary = "특정 채팅방의 유저 정보 동기화(조회)", description = "특정 채팅방의 그룹 챌린저 정보를 가져옵니다. 순서는 나, 방장, 나머지 유저 이름순 입니다.")
    @GetMapping("/{chatRoomId}/users")
    public ApiResponse<ChatRoomUserInfos> synchronizationChatRoomUserInfos(@PathVariable Long chatRoomId) {

        Long userId = toId(getCurrentUserId());

        return ApiResponse.onSuccess(chatService.synchronizationChatRoomUsers(userId, chatRoomId));
    }

    /**
     * @param chatRoomDetailRequestDto 채팅방 생성 DTO
     * @return 로그인한 사용자를 방장으로 한 채팅방을 생성하고 채팅방의 ID를 반환합니다.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "채팅방 생성", description = "그룹 채팅방을 생성합니다.")
    public ApiResponse<Long> createChatRoom(
            @RequestPart("chatRoomInfo") @Valid ChatRoomDetailRequestDto chatRoomDetailRequestDto,
            @RequestPart(value = "chatRoomImage", required = false) MultipartFile chatRoomImage
    ) { // 컨트롤러에서 @RequestPart를 통해 이미지와 채팅방 정보를 둘다 받기. 이미지는 필수 아님

        Long userId = toId(getCurrentUserId());
        Long newChatRoomId = chatService.createChatRoom(userId, chatRoomDetailRequestDto, chatRoomImage);
        return ApiResponse.onSuccess(newChatRoomId);
    }

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
    // TODO: Response 수정
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
     * @return S3에 저장된 이미지 URL
     */
    @PostMapping(value = "/{chatRoomId}/images", consumes = "multipart/form-data")
    @Operation(summary = "채팅 이미지 업로드 & 전송", description = "채팅 이미지를 S3에 업로드하고 url을 채팅으로 전송합니다.")
    public ApiResponse<String> sendChatImage(@RequestBody ImageChatRequestDTO.ImageDTO request,
                                               @PathVariable Long chatRoomId
                                               ) {
        Long userId = toId(getCurrentUserId());
        String imageUrl = chatService.uploadChatImage(userId,chatRoomId, request.getChatPicture());
        chatService.sendImageChat(userId, chatRoomId, imageUrl);
        return ApiResponse.onSuccess(imageUrl); // 전송된 이미지 url 리턴
    }

    /**
     * null 값이면 필터링에 포함하지 않습니다.
     *
     * @param age 연령대
     * @param minAmount 최소 금액 (null 값이면 0원으로)
     * @param maxAmount 최대 금액 (null 값이면 2147483647원으로)
     * @param job 직업 분야
     * @param page 페이지 (기본값 0)
     * @return 필터링에 맞는 모든 그룹 채팅방을 조회합니다.
     */
    @Operation(summary = "채팅방 둘러보기", description = "필터에 맞는 모든 채팅방을 페이징 단위로 조회합니다.")
    @GetMapping("/explore")
    public ResponseEntity<PublicChatRoomsDetailResponseDto> getPublicChatRooms(
            @RequestParam(required = false) String age,
            @RequestParam(required = false) Integer minAmount,
            @RequestParam(required = false) Integer maxAmount,
            @RequestParam(required = false) String job,
            @RequestParam(defaultValue = "0") int page) {

        return ResponseEntity.ok().body(chatService.getPublicChatRooms(page, age, minAmount, maxAmount, job));
    }

    @Operation(summary = "채팅방 둘러보기 - 검색", description = "키워드에 맞는 모든 채팅방을 페이징 단위로 조회합니다.")
    @GetMapping("/search")
    public ResponseEntity<PublicChatRoomsDetailResponseDto> searchPublicChatRooms(
            @RequestParam(required = true) @NotNull @Size(min = 2) String keyword,
            @RequestParam(defaultValue = "0") int page) {

        return ResponseEntity.ok().body(chatService.searchPublicChatRooms(keyword, page));
    }

    @Operation(summary = "채팅방/방장용 페이지 - 채팅방 수정", description = "채팅방 정보를 수정합니다.")
    @PutMapping("/{chatRoomId}")
    public ApiResponse<Long> updateChatRoom(
            @PathVariable("chatRoomId") Long chatRoomId,
            @RequestBody @Valid ChatRoomDetailRequestDto.ChatRoomUpdateDTO updateDTO
    ){
        Long userId = toId(getCurrentUserId());

        return ApiResponse.onSuccess(chatService.updateChatRoom(userId, chatRoomId, updateDTO));
    }
}