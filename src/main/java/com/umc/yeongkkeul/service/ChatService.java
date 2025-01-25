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

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final RabbitTemplate rabbitTemplate; // RabbitMQ
    private final RedisTemplate<String, Object> redisTemplate; // Redis
    private static final String READ_STATUS_KEY_PREFIX = "read:message:";

    @Value("${rabbitmq.exchange.name}")
    private String CHAT_EXCHANGE_NAME;

    public void sendMessage(MessageDto messageDto) {
        rabbitTemplate.convertAndSend(CHAT_EXCHANGE_NAME, "chat.room." + messageDto.chatRoomId(), messageDto);
    }

    public void enterMessage(MessageDto messageDto) {
        rabbitTemplate.convertAndSend(CHAT_EXCHANGE_NAME, "chat.room." + messageDto.chatRoomId(), messageDto);
    }

    public void exitMessage(MessageDto messageDto) {
        rabbitTemplate.convertAndSend(CHAT_EXCHANGE_NAME, "chat.room." + messageDto.chatRoomId(), messageDto);
    }

    public List<MessageDto> getMessages(Long chatRoomId) {

        String redisKey = "chat:room:" + chatRoomId + ":message";
        List<Object> messgeList = redisTemplate.opsForList().range(redisKey, 0, -1);

        return messgeList.stream()
                .map(object -> (MessageDto) object)
                .toList();
    }

    @Transactional
    public void saveMessages(MessageDto messageDto) {

        User sender = userRepository.findById(messageDto.senderId() )
                .orElseThrow(() -> new UserHandler(ErrorStatus._USER_NOT_FOUND));

        ChatRoom chatRoom = chatRoomRepository.findById(messageDto.chatRoomId())
                .orElseThrow(() -> new RuntimeException());

        String redisKey = "chat:room:" + messageDto.chatRoomId() + ":message";
        redisTemplate.opsForList().leftPush(redisKey, messageDto);
    }
}