package com.umc.yeongkkeul.service;

import com.umc.yeongkkeul.apiPayload.code.status.ErrorStatus;
import com.umc.yeongkkeul.apiPayload.exception.handler.UserHandler;
import com.umc.yeongkkeul.domain.ChatRoom;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.repository.ChatRoomRepository;
import com.umc.yeongkkeul.repository.UserRepository;
import com.umc.yeongkkeul.web.dto.MessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void enterMessage(MessageDto messageDto) {

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
     *
     * @param messageDto 저장할 메시지 정보
     */
    @Transactional
    public void saveMessages(MessageDto messageDto) {

        /*
        User sender = userRepository.findById(messageDto.senderId())
                .orElseThrow(() -> new UserHandler(ErrorStatus._USER_NOT_FOUND));

         */

        ChatRoom chatRoom = chatRoomRepository.findById(messageDto.chatRoomId())
                .orElseThrow(() -> new RuntimeException());

        String redisKey = "chat:room:" + messageDto.chatRoomId() + ":message";
        redisTemplate.opsForList().leftPush(redisKey, messageDto);
    }
}