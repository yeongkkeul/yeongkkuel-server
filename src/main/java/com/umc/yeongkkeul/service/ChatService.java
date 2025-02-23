package com.umc.yeongkkeul.service;

import com.github.f4b6a3.tsid.TsidCreator;
import com.umc.yeongkkeul.apiPayload.code.status.ErrorStatus;
import com.umc.yeongkkeul.apiPayload.exception.handler.ChatRoomHandler;
import com.umc.yeongkkeul.apiPayload.exception.handler.ChatRoomMembershipHandler;
import com.umc.yeongkkeul.apiPayload.exception.handler.ExpenseHandler;
import com.umc.yeongkkeul.apiPayload.exception.handler.UserHandler;
import com.umc.yeongkkeul.aws.s3.AmazonS3Manager;
import com.umc.yeongkkeul.converter.ChatRoomConverter;
import com.umc.yeongkkeul.converter.UserConverter;
import com.umc.yeongkkeul.domain.ChatRoom;
import com.umc.yeongkkeul.domain.Expense;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.domain.enums.AgeGroup;
import com.umc.yeongkkeul.domain.enums.Job;
import com.umc.yeongkkeul.domain.common.Uuid;
import com.umc.yeongkkeul.domain.mapping.ChatRoomMembership;
import com.umc.yeongkkeul.repository.ChatRoomMembershipRepository;
import com.umc.yeongkkeul.repository.ChatRoomRepository;
import com.umc.yeongkkeul.repository.ExpenseRepository;
import com.umc.yeongkkeul.repository.UserRepository;
import com.umc.yeongkkeul.socket.SocketConnectionTracker;
import com.umc.yeongkkeul.web.dto.chat.*;
import com.umc.yeongkkeul.repository.*;
import com.umc.yeongkkeul.web.dto.chat.ChatRoomDetailRequestDto;
import com.umc.yeongkkeul.web.dto.chat.ChatRoomDetailResponseDto;
import com.umc.yeongkkeul.web.dto.chat.MessageDto;
import com.umc.yeongkkeul.web.dto.chat.ReceiptMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ChatService 클래스
 * 채팅 서비스의 비즈니스 로직을 처리하는 서비스 클래스.
 * 메시지 전송, 입장, 퇴장, 메시지 저장 및 조회 기능을 담당.
 * Redis를 활용하여 메시지를 캐싱하고, RabbitMQ를 사용하여 메시지 브로드캐스트를 처리.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMembershipRepository chatRoomMembershipRepository;
    private final ExpenseRepository expenseRepository;
    private final UuidRepository uuidRepository;

    private final RabbitTemplate rabbitTemplate; // RabbitMQ를 통해 메시지를 전송하는 템플릿
    private final SimpMessagingTemplate messagingTemplate; // 내장 STOMP를 통해 메시지를 전송하는 템플릿
    private final RedisTemplate<String, Object> redisTemplate; // Redis에 메시지를 저장하고 조회하는 템플릿
    private final AmazonS3Manager amazonS3Manager;

    // 기본 이미지 URL
    private final String DEFAULT_CHATROOM_IMAGE =
            "https://yeongkkeul-s3.s3.ap-northeast-2.amazonaws.com/chatroom-profile/basic-profile.png";


    private final String ROUTING_PREFIX_KEY = "chat.room."; // ROUTING KEY 접미사
    private final String STOMP_SETTING_PREFIX = "/topic/setting.room.";
    private final String STOMP_READ_PREFIX = "/topic/read.room.";

    @Value("${rabbitmq.exchange.name}")
    private String CHAT_EXCHANGE_NAME; // RabbitMQ Exchange 이름

    // SocketConnectionTracker를 추가하여 온라인 상태를 확인할 수 있도록 함
    private final SocketConnectionTracker socketConnectionTracker;

    private final int CHATROOM_PAGING_SIZE = 20; // 한 페이지 당 최대 30개를 조회

    /**
     * 오픈 채팅방에 메시지를 전송하는 통합 메서드.
     * 온라인 수신자에게는 RabbitMQ를 통해 실시간 전송,
     * 오프라인 수신자에게는 FCM 푸시 분기 처리를 수행합니다.
     */
    @Transactional
    public MessageDto sendMessage(MessageDto messageDto) {

        int userCount = chatRoomMembershipRepository.findAllByChatroomId(messageDto.chatRoomId()).size();

        MessageDto message = MessageDto.builder()
                .id(TsidCreator.getTsid().toLong()) // TSID ID 생성기, 시간에 따라 ID에 영향이 가고 최신 데이터일수록 ID 값이 커진다.
                .chatRoomId(messageDto.chatRoomId())
                .senderId(messageDto.senderId())
                .messageType(messageDto.messageType())
                .content(messageDto.content())
                .timestamp(LocalDateTime.now().toString())
                .unreadCount(userCount - 1)
                .rabbitMQTransmissionStatus(true)
                .finalTransmissionStatus(true)
                .saveStatus(true)
                .build();

        // 기존 RabbitMQ를 통한 실시간 메시지 전송 (온라인 구독자 대상) -> 온라인이면 sub 정보 남아있고, 오프라인이면 휘발돼서 상관없음
        rabbitTemplate.convertAndSend(CHAT_EXCHANGE_NAME, ROUTING_PREFIX_KEY + message.chatRoomId(), message, new CorrelationData(UUID.randomUUID().toString()));

        // 해당 채팅방의 모든 멤버 조회 (MySQL의 membership 테이블)
        List<ChatRoomMembership> memberships = chatRoomMembershipRepository.findByChatroomIdOrderByUserScoreDesc(message.chatRoomId());

        // 각 멤버에 대해 온라인 상태 확인 후, 오프라인이면 FCM 푸시 처리 (현재는 로그 출력)
        for (ChatRoomMembership membership : memberships) {
            Long memberId = membership.getUser().getId();
            // 보낸 사용자는 제외
            if (memberId.equals(message.senderId())) {
                continue;
            }

            if (!socketConnectionTracker.isUserOnline(memberId)) {
                // 오프라인인 경우 FCM 푸시 분기 처리 (아직 FCM 로직은 구현하지 않음)
                log.info("User {} is offline. FCM push triggered.", memberId); // TODO: FCM 전송 로직 추가
            }
        }

        return message;
    }

    /**
     * 사용자가 채팅방에 입장했을 때의 메시지를 전송.
     * 입장 메시지도 RabbitMQ를 통해 해당 채팅방에 있는 모든 클라이언트로 전송.
     *
     * @param messageDto 전송할 메시지 정보
     */
    private void enterMessage(MessageDto messageDto) {

        rabbitTemplate.convertAndSend(CHAT_EXCHANGE_NAME, ROUTING_PREFIX_KEY + messageDto.chatRoomId(), messageDto, new CorrelationData(UUID.randomUUID().toString()));
        messagingTemplate.convertAndSend(STOMP_SETTING_PREFIX + messageDto.chatRoomId(), new ChatSettingResponseDto(true));
    }

    /**
     * 사용자가 채팅방을 퇴장했을 때의 메시지를 전송.
     * 퇴장 메시지도 RabbitMQ를 통해 해당 채팅방에 있는 모든 클라이언트로 전송.
     *
     * @param messageDto 전송할 메시지 정보
     */
    public void exitMessage(MessageDto messageDto) {

        rabbitTemplate.convertAndSend(CHAT_EXCHANGE_NAME, ROUTING_PREFIX_KEY + messageDto.chatRoomId(), messageDto, new CorrelationData(UUID.randomUUID().toString()));
        messagingTemplate.convertAndSend(STOMP_SETTING_PREFIX + messageDto.chatRoomId(), new ChatSettingResponseDto(true));
    }

    /**
     * 특정 채팅방의 모든 메시지를 조회 - 테스트 용도.
     *
     * @param chatRoomId 조회할 채팅방 ID
     * @return List<MessageDto> 채팅방의 메시지 리스트
     */
    public List<MessageDto> getMessages(Long chatRoomId) {

        String redisKey = "chat:room:" + chatRoomId + ":message";
        List<Object> messageList = redisTemplate.opsForList().range(redisKey, 0, -1);

        return messageList.stream()
                .map(object -> (MessageDto) object)
                .toList();
    }

    /**
     * 특정 채팅방 클라이언트에서 업데이트 되지 않은 메시지를 조회
     * chatRoomId르 가진 채팅방의 클라이언트의 채팅 내역과 서버의 채팅 내역을 동기화 하는 메서드
     *
     * @param userId
     * @param chatRoomId
     * @param lastClientMessageId 클라이언트에 저장된 마지막 채팅 ID
     * @return
     */
    public List<MessageDto> synchronizationChatMessages(Long userId, Long chatRoomId, Long lastClientMessageId) {

        List<MessageDto> resultMessageList = new ArrayList<>();

        ChatRoomMembership chatRoomMembership = chatRoomMembershipRepository.findByUserIdAndChatroomId(userId, chatRoomId)
                .orElseThrow();

        // 채팅방 입장 메시지 ID
        // 이전의 채팅 내역은 못본다.
        Long joinServerMessageId = chatRoomMembership.getJoinMessageId();

        String redisKey = "chat:room:" + chatRoomId + ":message";
        ListOperations<String, Object> listOps = redisTemplate.opsForList();
        long messageSize = listOps.size(redisKey);

        // 30개씩 읽어오며, ID를 찾을 때까지 반복
        final int REDIS_BATCH_SIZE = 30;
        for (long start = 0; start < messageSize; start += REDIS_BATCH_SIZE) {

            long end = start + REDIS_BATCH_SIZE - 1;
            if (end >= messageSize) end = messageSize - 1;

            List<Object> messageList = redisTemplate.opsForList().range(redisKey, start, end);

            // 읽어온 메시지들에서 lastClientMessageId를 포함하는 메시지 찾기
            for (Object message : messageList) {
                if (message != null) {

                    MessageDto messageDto = (MessageDto) message;
                    resultMessageList.add(messageDto);

                    // 최신 메시지일 수록 ID값이 커지고 현재 탐색한 메시지가 채팅방 입장 메시지보다 작다는 것은 채팅방 이전의 메시지를 본다는 것이기에 반환해준다.
                    if (messageDto.id() < joinServerMessageId) return resultMessageList;

                    if (Objects.equals(messageDto.id(), lastClientMessageId)) {
                        return resultMessageList;
                    }
                } else {
                    log.error("The Message is NULL.");
                    return null;
                }
            }
        }

        return resultMessageList;
    }

    /**
     * userId인 사용자가 구독한 채팅방을 클라이언트-서버 사이에서 동기화
     *
     * @param userId
     * @return
     */
    public List<ChatRoomInfoResponseDto> synchronizationChatRoomsInfo(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus._USER_NOT_FOUND));

        List<ChatRoomMembership> chatRoomMemberships = chatRoomMembershipRepository.findAllByUserId(user.getId());
        List<Long> chatRoomIds = chatRoomMemberships.stream()
                .map(chatRoomMembership -> chatRoomMembership.getChatroom().getId())
                .toList();

        List<ChatRoom> chatRooms = chatRoomRepository.findAllByIdIn(chatRoomIds);

        return chatRooms.stream()
                .map(ChatRoomInfoResponseDto::of)
                .toList();
    }

    public ChatRoomInfoResponseDto synchronizationChatRoomInfo(Long userId, Long chatRoomId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus._USER_NOT_FOUND));

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatRoomHandler(ErrorStatus._CHATROOM_NOT_FOUND));

        return ChatRoomInfoResponseDto.builder()
                .chatRoomId(chatRoom.getId())
                .chatRoomTitle(chatRoom.getTitle())
                .chatRoomRule(chatRoom.getTitle())
                .chatRoomThumbnail(chatRoom.getImageUrl())
                .participationCount(chatRoom.getParticipationCount())
                .build();
    }

    public ChatRoomUserInfos synchronizationChatRoomUsers(Long chatRoomId, Long userId) {

        List<Long> hostUserId = new ArrayList<>();

        List<ChatRoomMembership> chatRoomMemberships = chatRoomMembershipRepository.findAllByChatroomId(chatRoomId);
        List<Long> userIds = chatRoomMemberships.stream()
                .filter(chatRoomMembership -> {
                    boolean isHost = chatRoomMembership.getIsHost();
                    boolean isMe = (chatRoomMembership.getUser().getId() == userId);

                    if (isHost) hostUserId.add(chatRoomMembership.getUser().getId());

                    return !isHost && !isMe;
                })
                .map(chatRoomMembership -> chatRoomMembership.getUser().getId())
                .toList();

        // 오류가 있지 않는한 hostUser가 없을 순 없다.
        if (hostUserId == null) throw new RuntimeException();

        List<User> users = userRepository.findAllByIdInOrderByNickname(userIds);
        User myUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus._USER_NOT_FOUND));
        User hostUser = userRepository.findById(hostUserId.get(0))
                .orElseThrow(() -> new UserHandler(ErrorStatus._USER_NOT_FOUND));

        return ChatRoomUserInfos.builder()
                .userInfos(UserConverter.toChatRoomUserInfos(myUser, hostUser, users))
                .build();
    }

    @Transactional
    public void readMessage(Long chatRoomId, Long lastClientMessageId, Long recentClientMessageId) {

        String redisKey = "chat:room:" + chatRoomId + ":message";
        ListOperations<String, Object> listOps = redisTemplate.opsForList();
        long messageSize = listOps.size(redisKey);

        final int REDIS_BATCH_SIZE = 30;
        boolean updateStatus = false;

        for (long start = 0; start < messageSize; start += REDIS_BATCH_SIZE) {

            long end = Math.min(start + REDIS_BATCH_SIZE - 1, messageSize - 1);

            List<Object> messageList = redisTemplate.opsForList().range(redisKey, start, end);

            for (int i = 0; i < messageList.size(); i++) {
                Object messageObj = messageList.get(i);
                if (messageObj == null) {
                    log.error("The Message is NULL at index {}", start + i);
                    continue;
                }

                // 역직렬화 처리
                MessageDto messageDto;
                if (messageObj instanceof MessageDto) {
                    messageDto = (MessageDto) messageObj;
                } else {
                    continue;
                }

                if (messageDto.id().equals(recentClientMessageId)) {
                    updateStatus = true;
                }

                if (messageDto.id() < recentClientMessageId && updateStatus == false) {
                    log.error("Start Message Read Error!");
                    return;
                }

                // 메시지 ID 체크 후 종료
                if (messageDto.id() <= lastClientMessageId) {

                    // 최종 읽음 상태를 포함한 응답 객체 생성
                    ReadMessageResponseDto readMessageResponseDto = ReadMessageResponseDto.builder()
                            .startMessageId(lastClientMessageId.toString())
                            .endMessageId(recentClientMessageId.toString())
                            .build();

                    // RabbitMQ에 읽음 상태 정보 전송
                    messagingTemplate.convertAndSend(STOMP_READ_PREFIX + chatRoomId, readMessageResponseDto);

                    return;
                }

                if (!messageDto.messageType().equals("TEXT") && !messageDto.messageType().equals("IMAGE") && !messageDto.messageType().equals("RECEIPT")) {
                    continue;
                }

                if (updateStatus) {
                    messageDto = MessageDto.builder()
                            .id(messageDto.id())
                            .chatRoomId(messageDto.chatRoomId())
                            .senderId(messageDto.senderId())
                            .messageType(messageDto.messageType())
                            .content(messageDto.content())
                            .timestamp(messageDto.timestamp())
                            .unreadCount(messageDto.unreadCount() > 0 ? messageDto.unreadCount() - 1 : 0)
                            .rabbitMQTransmissionStatus(messageDto.rabbitMQTransmissionStatus())
                            .finalTransmissionStatus(messageDto.finalTransmissionStatus())
                            .saveStatus(messageDto.saveStatus())
                            .build();

                    listOps.set(redisKey, start + i, messageDto); // Redis 리스트 업데이트
                }
            }
        }
    }

    /**
     * 메시지를 DB에 저장하고 Redis에 캐시.
     * FIXME: 트랜잭션 범위 떄문에 이에 관한 생각을 조금 해봐야 한다. 현재 메시지는 단순히 백업 용도이기 때문에 중요도가 RDB보다 낮기에 RDB와 트랜잭션을 묶지 않았다.
     *
     * @param messageDto 저장할 메시지 정보 (Id와 timestamp가 저장된 상태)
     */
    @Transactional
    public void saveMessages(MessageDto messageDto) {

        /*
        User sender = userRepository.findById(messageDto.senderId())
                .orElseThrow(() -> new UserHandler(ErrorStatus._USER_NOT_FOUND));
         */

        /*
        ChatRoom chatRoom = chatRoomRepository.findById(messageDto.chatRoomId())
                .orElseThrow(() -> new ChatRoomHandler(ErrorStatus._CHATROOM_NOT_FOUND));
         */

        String redisKey = "chat:room:" + messageDto.chatRoomId() + ":message";
        redisTemplate.opsForList().leftPush(redisKey, messageDto);
    }

    /**
     * 로그인한 사용자를 방장으로 한 채팅방을 생성하고 채팅방-사용자 정보를 저장합니다.
     * 클라이언트는 이 API 호출 후 웹 소켓을 연결(안되 있는 상태면)한 뒤, 해당 채팅방을 Subscribe 한다.
     *
     * @param userId
     * @param chatRoomDetailRequestDto 채팅방 생성 DTO
     * @return 생성한 채팅방의 ID를 반환합니다.
     */
    @Transactional
    public Long createChatRoom(Long userId, ChatRoomDetailRequestDto chatRoomDetailRequestDto, MultipartFile chatRoomImage) {

        // 방장
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus._USER_NOT_FOUND));

        // 채팅방 저장
        ChatRoom chatRoom = ChatRoomConverter.toChatRoomEntity(chatRoomDetailRequestDto);

        // 이미지 업로드 및 url 셋팅 -> 이미지 업로드 or 기본 이미지
        if (chatRoomImage == null || chatRoomImage.isEmpty()) {
            // 파일이 없으므로 기본이미지 URL
            chatRoom.setImageUrl(DEFAULT_CHATROOM_IMAGE);
        } else {
            // 파일이 있으면 업로드 후 URL 세팅
            String uploadedUrl = uploadChatRoomImage(chatRoomImage);
            chatRoom.setImageUrl(uploadedUrl);
        }

        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        // 채팅방-사용자 저장
        // 방장이기에 isHost에 true값 설정
        boolean isHost = true;
        ChatRoomMembership chatRoomMembership = ChatRoomConverter.toChatRoomMembershipEntity(user, savedChatRoom, isHost, -1L);
        chatRoomMembershipRepository.save(chatRoomMembership);

        return savedChatRoom.getId();
    }

    /**
     * S3 업로드 유틸 매소드
     * - UUID 엔티티 만들고 DB에 저장
     * - S3 keyName 생성 후 업로드
     * - 업로드 완료된 URL 리턴
     */
    private String uploadChatRoomImage(MultipartFile file) {
        // 1. 새 Uuid 엔티티 생성
        Uuid uuidEntity = Uuid.builder()
                .uuid(UUID.randomUUID().toString())
                .build();
        uuidRepository.save(uuidEntity);

        // 2. S3 keyName 생성 (chatroom-profile 폴더 저장)
        String keyName = amazonS3Manager.generateChatroomProfileKeyName(uuidEntity);

        // 3. S3 업로드
        return amazonS3Manager.uploadFile(keyName, file);
    }

    /**
     * @param chatRoomId
     * @return 해당 채팅방의 정보를 반환하는 메서드 - 채팅방 가입하기
     */
    public ChatRoomDetailResponseDto getChatRoomDetail(Long chatRoomId) {

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatRoomHandler(ErrorStatus._CHATROOM_NOT_FOUND));

        // 최근 활동을 확인하기 위해 Redis에서 가장 마지막에 저장된 List를 가져오는 로직
        String redisKey = "chat:room:" + chatRoomId + ":message";
        Object lastMessageObject = redisTemplate.opsForList().index(redisKey, 0);

        // redisKey가 존재하지 않거나 리스트가 비어 있으면 null 반환
        if (lastMessageObject != null) {
            MessageDto lastMessage = (MessageDto) lastMessageObject;
            String lastActiviy = convertToLastActivity(lastMessage.timestamp());
            return ChatRoomConverter.toChatRoomDetailResponseDto(chatRoom, lastActiviy);
        }
        else {
            return ChatRoomConverter.toChatRoomDetailResponseDto(chatRoom, null);
        }
    }

    /**
     * 해당 사용자가 특정 채팅방에 입장을 할 때 채팅방-사용자 관계 테이블에 정보를 저장하는 메서드.
     *
     * @param userId
     * @param chatRoomId
     * @return 채팅방 ID
     */
    @Transactional
    public void joinChatRoom(Long userId, Long chatRoomId, MessageDto messageDto) {

        // 가입 희망 사용자
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus._USER_NOT_FOUND));

        // 가입할 그룹 채팅방
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatRoomHandler(ErrorStatus._CHATROOM_NOT_FOUND));

        // 이미 가입 되어 있다면
        if (chatRoomMembershipRepository.findByUserIdAndChatroomId(userId, chatRoomId).isPresent()) {
            throw new ChatRoomMembershipHandler(ErrorStatus._CHATROOMMEMBERSHIP_ALREADY_EXISTS);
        }

        // 인원 추가
        chatRoom.setParticipationCount(chatRoom.getParticipationCount() + 1);

        boolean isHost = false; // 호스트가 아니기에 false
        ChatRoomMembership chatRoomMembership = ChatRoomConverter.toChatRoomMembershipEntity(user, chatRoom, isHost, messageDto.id());

        // 채팅방-사용자 관계 테이블 저장
        chatRoomMembershipRepository.save(chatRoomMembership);
        chatRoomRepository.save(chatRoom);

        // RabbitMQ 메시지 전달 - 예외 발생 시 트랜 잭션 롤백
        try {
            enterMessage(messageDto);
        } catch (AmqpException e) {
            throw new AmqpException("메시지 전송 실패", e); // 예외를 던져서 트랜잭션 롤백 유도
        }
    }

    @Transactional
    public void exitChatRoom(Long userId, Long chatRoomId, MessageDto messageDto) {

        // 탈퇴 희망 사용자
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus._USER_NOT_FOUND));

        // 탈퇴할 그룹 채팅방
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatRoomHandler(ErrorStatus._CHATROOM_NOT_FOUND));

        // 이미 탈퇴되었다면
        chatRoomMembershipRepository.findByUserIdAndChatroomId(userId, chatRoomId)
                .orElseThrow(() -> new ChatRoomMembershipHandler(ErrorStatus._CHATROOMMEMBERSHIP_NOT_FOUND));

        // 해당 유저가 방장인지 확인
        ChatRoomMembership chatRoomMembership = chatRoomMembershipRepository.findByUserIdAndChatroomId(user.getId(), chatRoom.getId())
                        .orElseThrow(() -> new ChatRoomMembershipHandler(ErrorStatus._CHATROOMMEMBERSHIP_NOT_FOUND));

        chatRoom.setParticipationCount(chatRoom.getParticipationCount() - 1);

        // 방장이라면 해당 채팅방을 삭제
        if (chatRoomMembership.getIsHost()) {

            chatRoomMembershipRepository.deleteChatRoomMemberships(chatRoom.getId()); // 모든 연관 엔티티 삭제
            chatRoomRepository.delete(chatRoom);

        } else {
            // 방장이 아니라면 관계 테이블만 삭제
            chatRoomMembershipRepository.delete(chatRoomMembership);
            chatRoomRepository.save(chatRoom);
        }

        // RabbitMQ 메시지 전달 - 예외 발생 시 트랜 잭션 롤백
        try {
            exitMessage(messageDto);
        } catch (AmqpException e) {
            throw new AmqpException("메시지 전송 실패", e); // 예외를 던져서 트랜잭션 롤백 유도
        }
    }

    /**
     * @param chatRoomId
     * @param password 패스워드 정보
     * @return 사용자가 입력한 패스워드와 채팅방의 패스워드가 일치하면 True를 반환합니다.
     */
    public Boolean validateChatRoomPassword(Long chatRoomId, String password) {

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatRoomHandler(ErrorStatus._CHATROOM_NOT_FOUND));

        // 채팅방의 비밀번호가 없다면
        if (chatRoom.getPassword() == null) return true;

        // 채팅방의 비밀번호가 있는데 사용자가 입력한 비밀번호가 없다면
        if (password == null) return false;

        return password.equals(chatRoom.getPassword());
    }

    /**
     * @param expenseId 지출 내역 ID
     * @return 지출 내역 ID로 지출 정보를 가져와서 영수증으로 반환하는 메서드
     */
    public ReceiptMessageDto getReceipt(Long expenseId) {

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ExpenseHandler(ErrorStatus.EXPENSE_NOT_FOUND));

        return ReceiptMessageDto.builder()
                .senderName(expense.getUser().getNickname())
                .category(expense.getCategory().getName())
                .content(expense.getContent())
                .amount(expense.getAmount())
                .imageUrl(expense.getImageUrl())
                .isNoSpending(expense.getIsNoSpending())
                .build();
    }

    /**
     * 필터에 맞는 모든 그룹 채팅방을 탐색하는 메서드입니다.
     * null 값이면 필터링에서 무시합니다.
     *
     * @param page 페이지
     * @param age 연령대
     * @param minAmount 최소 목표 금액
     * @param maxAmount 최대 목표 금액
     * @param job 직업 분야
     * @return
     */
    public PublicChatRoomsDetailResponseDto getPublicChatRooms(int page, String age, Integer minAmount, Integer maxAmount, String job) {

        List<ChatRoom> chatRoomList = null;

        AgeGroup ageEnum = (age != null) ? AgeGroup.valueOf(age) : null; // 연령대 타입 변환
        Job jobEnum = (job != null) ? Job.valueOf(job.toUpperCase()) : null; // 직업 타입 변환

        // null 값이면 필터링에서 무시합니다.
        // 최소 금액이 null 값이면 0원으로, 최대 금액이 null 값이면 2147483647 로 변환해서 필터링합니다.
        // page 값이 0일 때만 사용합니다. (추천순 정렬)
        Page<ChatRoom> chatRoomPageOrder = chatRoomRepository.findAllWithPagination(ageEnum, minAmount, maxAmount, jobEnum, PageRequest.of(0, CHATROOM_PAGING_SIZE));
        List<Long> chatRoomIds = chatRoomPageOrder.getContent().stream()
                .map(chatRoom -> chatRoom.getId())
                .toList();

        System.out.println("chatRoomList Size: " + chatRoomIds.size());

        if (page == 0) {
            chatRoomList = chatRoomPageOrder.getContent();
        } else if (page > 0) {

            Pageable pageable = PageRequest.of(page - 1, CHATROOM_PAGING_SIZE); // 한 페이지 당 최대 20개를 가져온다.
            Page<ChatRoom> chatRoomPageRandom = chatRoomRepository.findRandomByIdNotIn(chatRoomIds, pageable);
            chatRoomList = chatRoomPageRandom.getContent();
        }

        return PublicChatRoomsDetailResponseDto.builder()
                .publicChatRoomDetailDtos(chatRoomList.stream()
                        .map(chatRoom -> PublicChatRoomsDetailResponseDto.PublicChatRoomDetailDto.of(chatRoom))
                        .toList())
                .build();
    }

    public PublicChatRoomsDetailResponseDto searchPublicChatRooms(String keyword, int page) {

        Pageable pageable = PageRequest.of(page, CHATROOM_PAGING_SIZE);
        Page<ChatRoom> chatRoomPage = chatRoomRepository.findByTitleContainingOrderByParticipationCountDesc(keyword, pageable);
        List<ChatRoom> chatRoomList = chatRoomPage.getContent();

        return PublicChatRoomsDetailResponseDto.builder()
                .publicChatRoomDetailDtos(chatRoomList.stream()
                        .map(chatRoom -> PublicChatRoomsDetailResponseDto.PublicChatRoomDetailDto.of(chatRoom))
                        .toList())
                .build();
    }

    /**
     * 주어진 기준에 따라 문자열로 반환. 일주일을 초과할 경우 NULL 반환.
     *
     * @param lastActivityTime 마지막 메시지를 보낸 시간
     * @return 마지막 활동 시간을 현재 시간과 비교하여 문자열로 반환
     */
    private String convertToLastActivity(String lastActivityTime) {

        // lastActivityTime이 null이라면 메서드 종료
        if (lastActivityTime == null || lastActivityTime.isEmpty()) return null;

        long seconds = Duration.between(LocalDateTime.parse(lastActivityTime), LocalDateTime.now()).getSeconds();

        if (seconds < 60) return seconds + "초 전 활동";
        if (seconds < 3600) return (seconds / 60) + "분 전 활동";
        if (seconds < 86400) return (seconds / 3600) + "시간 전 활동";
        if (seconds < 604800) return (seconds / 86400) + "일 전 활동";
        if (seconds < 691200) return  "일주일 전 활동";

        return null;
    }

    /**
     * 이미지 메시지 URL 리스트를 반환
     * messageType이 "IMAGE"인 경우, content에 저장된 S3 key를 이용해 URL을 생성
     */
    public List<ImageChatResponseDTO> getChatRoomImageUrls(Long chatRoomId) {
        List<MessageDto> messages = getMessages(chatRoomId);

        return messages.stream()
                .filter(m -> "IMAGE".equalsIgnoreCase(m.messageType()))
                .map(m -> new ImageChatResponseDTO(
                        m.id(),                              // 메시지 ID
                        amazonS3Manager.getFileUrl(m.content())  // S3에 있는 실제 이미지 URL
                ))
                .collect(Collectors.toList());
    }

    /**
     * 채팅방 내의 특정 이미지 메시지에 해당하는 파일을 S3에서 다운로드
     * S3DownloadResponse에는 파일 데이터와 원본 콘텐츠 타입이 포함
     */
    @Transactional(readOnly = true)
    public AmazonS3Manager.S3DownloadResponse downloadChatImage(Long chatRoomId, Long messageId) {
        Optional<MessageDto> optionalImageMessage = getMessages(chatRoomId).stream()
                .filter(m -> {
                    System.out.println("Comparing: m.id()=" + m.id() + " (" + m.id().getClass() + ") vs messageId=" + messageId + " (" + messageId.getClass() + ")");
                    return m.id().equals(messageId) && "IMAGE".equalsIgnoreCase(m.messageType());
                })
                .findFirst();
        if (optionalImageMessage.isEmpty()) {
            throw new ChatRoomHandler(ErrorStatus._CHAT_IMAGE_NOT_FOUND);
        }
        MessageDto imageMessage = optionalImageMessage.get();
        String imageUrl = imageMessage.content();

        // URL에서 S3 key 파싱 ("https://yeongkkeul-s3.s3.ap-northeast-2.amazonaws.com/chat/badb12fa-8d10-487b-9536-d43735f6f59f" -> "chat/badb12fa-8d10-487b-9536-d43735f6f59f")
        // 다운로드시, S3의 키를 통해서 다운로드 해야함.
        try {
            URL url = new URL(imageUrl);
            String key = url.getPath(); // "/chat/badb12fa-8d10-487b-9536-d43735f6f59f"
            if (key.startsWith("/")) {
                key = key.substring(1);
            }
            return amazonS3Manager.downloadFileWithMetadata(key);
        } catch (MalformedURLException e) {
            throw new ChatRoomHandler(ErrorStatus._CHAT_IMAGE_NOT_FOUND);
        }
    }

    /**
     * 채팅 이미지 업로드 메서드
     * 1. 새로운 Uuid 엔티티를 생성 및 저장
     * 2. 저장한 Uuid를 기반으로 S3 key 생성
     * 3. AmazonS3Manager를 통해 파일 업로드 후 S3에 저장된 URL 반환
     *
     * @return S3에 저장된 이미지 URL
     */
    @Transactional
    public String uploadChatImage(Long userId,Long chatRoomId, MultipartFile file) {
        // chatRoomId를 통해, 유저가 해당 채팅방에 소속해있는지 점검
        User user = userRepository.findById(userId).orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        chatRoomMembershipRepository.findByUserIdAndChatroomId(userId, chatRoomId).orElseThrow(()->new ChatRoomMembershipHandler(ErrorStatus._CHATROOM_NO_PERMISSION));

        // 해당 유저가 채팅방에 소속되어 권한 인증이 완료되면, 이미지를 업로드해서 url 리턴
        // 새로운 Uuid 엔티티 생성 (랜덤 UUID 문자열 생성)
        Uuid uuidEntity = Uuid.builder().uuid(UUID.randomUUID().toString()).build();
        // DB에 저장 (중복 방지)
        uuidRepository.save(uuidEntity);
        // Uuid 엔티티를 이용해 S3 key 생성
        String keyName = amazonS3Manager.generateChatKeyName(uuidEntity);
        // S3에 파일 업로드 및 URL 반환
        return amazonS3Manager.uploadFile(keyName, file);
    }


    // 채팅방 방장이 맞는지 확인하고 수정
    @Transactional
    public Long updateChatRoom(Long userId, Long chatRoomId, ChatRoomDetailRequestDto.ChatRoomUpdateDTO updateDTO) {
        // 유저 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 채팅방 찾기
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatRoomHandler(ErrorStatus._CHATROOM_NOT_FOUND));

        // 해당 유저가 방장인지 확인
        ChatRoomMembership chatRoomMembership = chatRoomMembershipRepository.findByUserIdAndChatroomId(user.getId(), chatRoom.getId())
                .orElseThrow(() -> new ChatRoomMembershipHandler(ErrorStatus._CHATROOMMEMBERSHIP_NOT_FOUND));

        // 방장일 경우 내용 수정
        if (chatRoomMembership.getIsHost()){
            // 비밀번호 - 빈칸이나 null로 작성하였을 때
            if (updateDTO.getChatRoomPassword() == null || updateDTO.getChatRoomPassword().isEmpty()) {
                chatRoom.setPassword(null);
            } else {
                chatRoom.setPassword(updateDTO.getChatRoomPassword());
            }
            chatRoom.setPassword(updateDTO.getChatRoomPassword());
            chatRoom.setTitle(updateDTO.getChatRoomName());
            chatRoom.setDailySpendingGoalFilter(updateDTO.getChatRoomSpendingAmountGoal());
            chatRoom.setMaxParticipants(updateDTO.getChatRoomMaxUserCount());
        } else {
            // 방장이 아닌데 채팅방 내용을 수정하려고 할 때 에러 발생하도록
            new ChatRoomMembershipHandler(ErrorStatus._CHATROOMMEMBERSHIP_NO_PERMISSION);
        }

        // 수정 내용 저장
        messagingTemplate.convertAndSend(STOMP_SETTING_PREFIX + chatRoomId, new ChatSettingResponseDto(true));
        chatRoomRepository.save(chatRoom);

        return chatRoom.getId();
    }


    public void sendReceiptChatRoom(Expense response, Long userId){
        List<ChatRoomMembership> chatRooms = getChatRoomMemberships(userId);
        chatRooms.stream().map(membership -> MessageDto.builder()
                        .id(TsidCreator.getTsid().toLong()) // TSID ID 생성기, 시간에 따라 ID에 영향이 가고 최신 데이터일수록 ID 값이 커진다.
                        .chatRoomId(membership.getChatroom().getId())
                        .senderId(userId)
                        .messageType("RECEIPT")
                        .content(String.valueOf(response.getId())) // 지출내역 아이디를 String으로 전환해서 넣기.
                        .timestamp(LocalDateTime.now().toString())
                        .build())
                .forEach(message -> {
                    MessageDto messageDto = sendMessage(message);// 메시지 전송
                    log.info("Send a receipt message to chat room ID: {}", message.chatRoomId());
                    saveMessages(messageDto); // 메시지 저장
                });
    }

    public void sendImageChat(Long userId, Long chatRoomId, String imageUrl){
        MessageDto message = MessageDto.builder()
                .id(TsidCreator.getTsid().toLong()) // TSID ID 생성기, 시간에 따라 ID에 영향이 가고 최신 데이터일수록 ID 값이 커진다.
                .chatRoomId(chatRoomId)
                .senderId(userId)
                .messageType("IMAGE")
                .content(imageUrl)
                .timestamp(LocalDateTime.now().toString())
                .build();
        MessageDto messageDto = sendMessage(message);// 메시지 전송
        log.info("Send a image message to chat room ID: {}", message.chatRoomId());
        saveMessages(messageDto); // 메시지 저장
    }

    @Transactional(readOnly = true)
    public List<ChatRoomMembership> getChatRoomMemberships(Long userId) {
        List<ChatRoomMembership> chatRooms = chatRoomMembershipRepository.findAllByUserId(userId);
        return chatRooms;
    }

    /**
     * 사용자가 방장일 때, 특정 사용자를 퇴출 시키는 경우 메시지를 전송
     * 퇴장 메시지도 RabbitMQ를 통해 해당 채팅방에 있는 모든 클라이언트로 전송.
     *
     * @param messageDto 전송할 메시지 정보
     */
    public void expelMessage(MessageDto messageDto) {

        rabbitTemplate.convertAndSend(CHAT_EXCHANGE_NAME, ROUTING_PREFIX_KEY + messageDto.chatRoomId(), messageDto, new CorrelationData(UUID.randomUUID().toString()));
    }

    @Transactional
    public void expelChatRoom(Long userId, Long targetUserId, Long chatRoomId, MessageDto messageDto) {
        // 현재 유저 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 퇴장시키고자 하는 유저 찾기
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 채팅방 찾기
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatRoomHandler(ErrorStatus._CHATROOM_NOT_FOUND));

        // 해당 유저가 방장인지 확인
        ChatRoomMembership userChatRoomMembership = chatRoomMembershipRepository.findByUserIdAndChatroomId(user.getId(), chatRoom.getId())
                .orElseThrow(() -> new ChatRoomMembershipHandler(ErrorStatus._CHATROOMMEMBERSHIP_NOT_FOUND));

        // 퇴장시키고자 하는 유저가 그룹 채팅방 유저가 맞는지 확인
        ChatRoomMembership targetUserChatRoomMembership = chatRoomMembershipRepository.findByUserIdAndChatroomId(targetUser.getId(), chatRoom.getId())
                .orElseThrow(() -> new ChatRoomMembershipHandler(ErrorStatus._CHATROOMMEMBERSHIP_NOT_FOUND));

        // 방장만 퇴출 가능
        if (!userChatRoomMembership.getIsHost()) {
            throw new ChatRoomMembershipHandler(ErrorStatus._CHATROOMMEMBERSHIP_NO_PERMISSION);
        }

        chatRoom.setParticipationCount(chatRoom.getParticipationCount() - 1);

        // 퇴장시키는 유저의 관계 테이블 삭제
        chatRoomMembershipRepository.delete(targetUserChatRoomMembership);
        chatRoomRepository.save(chatRoom);

        // RabbitMQ 메시지 전달 - 예외 발생 시 트랜 잭션 롤백
        try {
            expelMessage(messageDto);
        } catch (AmqpException e) {
            throw new AmqpException("메시지 전송 실패", e); // 예외를 던져서 트랜잭션 롤백 유도
        }
    }
}