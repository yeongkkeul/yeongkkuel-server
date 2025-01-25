package com.umc.yeongkkeul.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocketBrokerConfig 클래스
 * STOMP 프로토콜을 기반으로 웹소켓 메시지 브로커를 설정하는 구성 클래스.
 * Spring Boot 애플리케이션에서 WebSocket 통신 및 메시지 브로커 기능을 활성화.
 */
@Configuration
@EnableWebSocketMessageBroker // WebSocket 메시지 브로커 활성화
public class WebSocketBrokerConfig implements WebSocketMessageBrokerConfigurer {

    private final String RABBITMQ_HOST; // RabbitMQ 호스트 주소를 저장하는 필드

    public WebSocketBrokerConfig(
            @Value("${spring.rabbitmq.host}") String rabbitmqHost
    ) {

        this.RABBITMQ_HOST = rabbitmqHost;
    }

    /**
     * 메시지 브로커 설정 메서드.
     * STOMP에서 사용되는 메시지 브로커와 라우팅 관련 설정을 정의.
     *
     * @param registry MessageBrokerRegistry 객체로 브로커 설정을 수행
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        // 메시지 경로의 구분자를 `/` 대신 `.`으로 변경 (예: /topic/chat -> topic.chat)
        registry.setPathMatcher(new AntPathMatcher("."));

        // 클라이언트가 SEND 요청을 보낼 때 라우팅될 경로의 접두어 설정
        // 예: /pub/chat -> @MessageMapping("chat")으로 매핑
        registry.setApplicationDestinationPrefixes("/pub");

        // STOMP 브로커 릴레이 활성화 -> 내장 STOMP가 아닌 외부 브로커를 사용
        // 지정된 경로(/queue, /topic, /exchange, /amq/queue)에 대해 RabbitMQ 브로커와 통신
        registry.enableStompBrokerRelay("/queue", "/topic", "/exchange", "/amq/queue");
    }

    /**
     * STOMP 엔드포인트 등록 메서드.
     * 클라이언트가 특정 엔드포인트로 연결할 수 있도록 STOMP 엔드포인트를 정의.
     *
     * @param registry StompEndpointRegistry 객체로 엔드포인트 설정을 수행
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        // 클라이언트가 웹소켓 연결을 시도할 기본 엔드포인트 설정
        // /ws 경로로 연결을 수락하며, 모든 오리진(*: CORS 허용)을 허용
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*"); // FIXME: CORS 정책 허용 (필요 시 보안 강화를 위해 특정 Origin 설정 가능)
    }
}