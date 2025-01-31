package com.umc.yeongkkeul.service;

import com.umc.yeongkkeul.apiPayload.code.status.ErrorStatus;
import com.umc.yeongkkeul.apiPayload.exception.handler.ChatRoomHandler;
import com.umc.yeongkkeul.apiPayload.exception.handler.ExpenseHandler;
import com.umc.yeongkkeul.apiPayload.exception.handler.UserHandler;
import com.umc.yeongkkeul.converter.ChatRoomConverter;
import com.umc.yeongkkeul.domain.ChatRoom;
import com.umc.yeongkkeul.domain.Expense;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.domain.mapping.ChatRoomMembership;
import com.umc.yeongkkeul.repository.ChatRoomMembershipRepository;
import com.umc.yeongkkeul.repository.ChatRoomRepository;
import com.umc.yeongkkeul.repository.ExpenseRepository;
import com.umc.yeongkkeul.repository.UserRepository;
import com.umc.yeongkkeul.web.dto.chat.ChatRoomDetailRequestDto;
import com.umc.yeongkkeul.web.dto.chat.ChatRoomDetailResponseDto;
import com.umc.yeongkkeul.web.dto.chat.MessageDto;
import com.umc.yeongkkeul.web.dto.chat.ReceiptMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

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

    private final RabbitTemplate rabbitTemplate; // RabbitMQ를 통해 메시지를 전송하는 템플릿
    private final RedisTemplate<String, Object> redisTemplate; // Redis에 메시지를 저장하고 조회하는 템플릿

    private final String READ_STATUS_KEY_PREFIX = "read:message:"; // 읽은 메시지 상태를 저장할 때 사용할 Redis 키 접두어
    private final String ROUTING_PREFIX_KEY = "chat.room."; // ROUTING KEY 접미사

    @Value("${rabbitmq.exchange.name}")
    private String CHAT_EXCHANGE_NAME; // RabbitMQ Exchange 이름

    /**
     * 메시지를 특정 채팅방으로 전송.
     * RabbitMQ를 사용하여 메시지를 해당 채팅방에 있는 모든 클라이언트로 전송.
     *
     * @param messageDto 전송할 메시지 정보
     */
    public void sendMessage(MessageDto messageDto) {

        // RabbitMQ의 특정 채팅방으로 메시지 전송
        rabbitTemplate.convertAndSend(CHAT_EXCHANGE_NAME, ROUTING_PREFIX_KEY + messageDto.chatRoomId(), messageDto);
    }

    /**
     * 사용자가 채팅방에 입장했을 때의 메시지를 전송.
     * 입장 메시지도 RabbitMQ를 통해 해당 채팅방에 있는 모든 클라이언트로 전송.
     *
     * @param messageDto 전송할 메시지 정보
     */
    private void enterMessage(MessageDto messageDto) {

        rabbitTemplate.convertAndSend(CHAT_EXCHANGE_NAME, ROUTING_PREFIX_KEY + messageDto.chatRoomId(), messageDto);
    }

    /**
     * 사용자가 채팅방을 퇴장했을 때의 메시지를 전송.
     * 퇴장 메시지도 RabbitMQ를 통해 해당 채팅방에 있는 모든 클라이언트로 전송.
     *
     * @param messageDto 전송할 메시지 정보
     */
    public void exitMessage(MessageDto messageDto) {

        rabbitTemplate.convertAndSend(CHAT_EXCHANGE_NAME, ROUTING_PREFIX_KEY + messageDto.chatRoomId(), messageDto);
    }

    /**
     * 특정 채팅방의 모든 메시지를 조회.
     *
     * @param chatRoomId 조회할 채팅방 ID
     * @return List<MessageDto> 채팅방의 메시지 리스트
     */
    public List<MessageDto> getMessages(Long chatRoomId) {

        String redisKey = "chat:room:" + chatRoomId + ":message";
        List<Object> messgeList = redisTemplate.opsForList().range(redisKey, 0, -1);

        return messgeList.stream()
                .map(object -> (MessageDto) object)
                .toList();
    }

    /**
     * 메시지를 DB에 저장하고 Redis에 캐시.
     * FIXME: 트랜잭션 범위 떄문에 이에 관한 생각을 조금 해봐야 한다. 현재 메시지는 단순히 백업 용도이기 때문에 중요도가 RDB보다 낮기에 RDB와 트랜잭션을 묶지 않았다.
     *
     * @param messageDto 저장할 메시지 정보
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

        // TODO: ID와 timestamp도 같이 생성해줘야 한다.

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
    // TODO: Long으로 반환해줄지 Json으로 반환할지 고민 해봐야 된다.
    @Transactional
    public Long createChatRoom(Long userId, ChatRoomDetailRequestDto chatRoomDetailRequestDto) {

        // 방장
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus._USER_NOT_FOUND));

        // 채팅방 저장
        ChatRoom chatRoom = ChatRoomConverter.toChatRoomEntity(chatRoomDetailRequestDto);
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        // 채팅방-사용자 저장
        // 방장이기에 isHost에 true값 설정
        boolean isHost = true;
        ChatRoomMembership chatRoomMembership = ChatRoomConverter.toChatRoomMembershipEntity(user, savedChatRoom, isHost);
        chatRoomMembershipRepository.save(chatRoomMembership);

        return savedChatRoom.getId();
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
        Object lastMessageObject = redisTemplate.opsForList().index(redisKey, -1);

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

        boolean isHost = false; // 호스트가 아니기에 false
        ChatRoomMembership chatRoomMembership = ChatRoomConverter.toChatRoomMembershipEntity(user, chatRoom, isHost);

        // 채팅방-사용자 관계 테이블 저장
        chatRoomMembershipRepository.save(chatRoomMembership);

        // RabbitMQ 메시지 전달 - 예외 발생 시 트랜 잭션 롤백
        try {
            enterMessage(messageDto);
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
                .isNoSpending(expense.isNoSpending())
                .build();
    }

    /**
     * 주어진 기준에 따라 문자열로 반환. 일주일을 초과할 경우 NULL 반환.
     *
     * @param lastActivityTime 마지막 메시지를 보낸 시간
     * @return 마지막 활동 시간을 현재 시간과 비교하여 문자열로 반환
     */
    private String convertToLastActivity(LocalDateTime lastActivityTime) {

        // lastActivityTime이 null이라면 메서드 종료
        if (lastActivityTime == null) return null;

        long seconds = Duration.between(lastActivityTime, LocalDateTime.now()).getSeconds();

        if (seconds < 60) return seconds + "초 전 활동";
        if (seconds < 3600) return (seconds / 60) + "분 전 활동";
        if (seconds < 86400) return (seconds / 3600) + "시간 전 활동";
        if (seconds < 604800) return (seconds / 86400) + "일 전 활동";
        if (seconds < 691200) return  "일주일 전 활동";

        return null;
    }
}