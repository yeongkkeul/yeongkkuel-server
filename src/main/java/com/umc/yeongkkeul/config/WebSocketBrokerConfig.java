package com.umc.yeongkkeul.config;

import com.umc.yeongkkeul.socket.SocketSessionInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompReactorNettyCodec;
import org.springframework.messaging.tcp.reactor.ReactorNettyTcpClient;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import reactor.netty.tcp.TcpClient;

/**
 * WebSocketBrokerConfig 클래스
 * STOMP 프로토콜을 기반으로 웹소켓 메시지 브로커를 설정하는 구성 클래스.
 * Spring Boot 애플리케이션에서 WebSocket 통신 및 메시지 브로커 기능을 활성화.
 */
@Configuration
@EnableWebSocketMessageBroker // WebSocket 메시지 브로커 활성화
public class WebSocketBrokerConfig implements WebSocketMessageBrokerConfigurer {

    private final String RABBITMQ_HOST;
    private final String RABBITMQ_USERNAME;
    private final String RABBITMQ_PASSWORD;

    @Autowired
    private SocketSessionInterceptor socketSessionInterceptor;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(socketSessionInterceptor);
    }


    public WebSocketBrokerConfig (
            @Value("${spring.rabbitmq.host}") String RABBITMQ_HOST,
            @Value("${spring.rabbitmq.username}") String RABBITMQ_USERNAME,
            @Value("${spring.rabbitmq.password}") String RABBITMQ_PASSWORD
    ) {
        this.RABBITMQ_HOST = RABBITMQ_HOST;
        this.RABBITMQ_USERNAME = RABBITMQ_USERNAME;
        this.RABBITMQ_PASSWORD = RABBITMQ_PASSWORD;
    }

    /**
     * 메시지 브로커 설정 메서드.
     * STOMP에서 사용되는 메시지 브로커와 라우팅 관련 설정을 정의.
     *
     * @param registry MessageBrokerRegistry 객체로 브로커 설정을 수행
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        TcpClient tcpClient = TcpClient
                .create()
                .host(RABBITMQ_HOST)
                .port(61613);

        ReactorNettyTcpClient<byte[]> client = new ReactorNettyTcpClient<>(tcpClient, new StompReactorNettyCodec());

        // STOMP 브로커 릴레이 활성화 -> 내장 STOMP가 아닌 외부 브로커를 사용
        // 지정된 경로(/queue, /topic, /exchange, /amq/queue)에 대해 RabbitMQ 브로커와 통신
        registry.enableStompBrokerRelay("/queue", "/topic", "/exchange", "/amq/queue")
                .setAutoStartup(true)
                .setTcpClient(client) // RabbitMQ와 연결할 클라이언트 설정
                .setRelayHost(RABBITMQ_HOST) // RabbitMQ 서버 주소
                .setRelayPort(61613) // RabbitMQ 포트(5672), STOMP(61613)
                .setSystemLogin(RABBITMQ_USERNAME) // RabbitMQ 시스템 계정
                .setSystemPasscode(RABBITMQ_PASSWORD) // RabbitMQ 시스템 비밀번호
                .setClientLogin(RABBITMQ_USERNAME) // RabbitMQ 클라이언트 계정
                .setClientPasscode(RABBITMQ_PASSWORD); // RabbitMQ 클라이언트 비밀번호

        // 메시지 경로의 구분자를 `/` 대신 `.`으로 변경 (예: /topic/chat -> topic.chat)
        registry.setPathMatcher(new AntPathMatcher("."));

        // 클라이언트가 SEND 요청을 보낼 때 라우팅될 경로의 접두어 설정
        // 예: /pub/chat -> @MessageMapping("chat")으로 매핑
        registry.setApplicationDestinationPrefixes("/pub");
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