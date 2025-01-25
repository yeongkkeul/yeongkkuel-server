package com.umc.yeongkkeul.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 외부 브로커인 RabbitMQ를 설정하는 구성 클래스.
 */
@Configuration
@EnableRabbit
public class RabbitMQConfig {

    private final String CHAT_QUEUE_NAME; // RabbitMQ Queue 이름
    private final String CHAT_EXCHANGE_NAME; // RabbitMQ Exchange 이름
    private final String CHAT_ROUTING_KEY; // RabbitMQ Binding 이름, TopicExchange를 사용하기에 Binding이 routing 역할을 수행하도록 한다.
    private final String RABBITMQ_HOST;

    public RabbitMQConfig(
            @Value("${rabbitmq.queue.name}") String CHAT_QUEUE_NAME,
            @Value("${rabbitmq.exchange.name}") String CHAT_EXCHANGE_NAME,
            @Value("${rabbitmq.routing.key}") String CHAT_ROUTING_KEY,
            @Value("${spring.rabbitmq.host}") String RABBITMQ_HOST
    ) {

        this.CHAT_QUEUE_NAME = CHAT_QUEUE_NAME;
        this.CHAT_EXCHANGE_NAME = CHAT_EXCHANGE_NAME;
        this.CHAT_ROUTING_KEY = CHAT_ROUTING_KEY;
        this.RABBITMQ_HOST = RABBITMQ_HOST;
    }

    /**
     * @return "chat.queue"라는 이름의 Queue 생성
     */
    @Bean
    public Queue chatQueue() {

        // durable을 true로 제공 -> 메시지 브로커가 재시작해도 해당 Exchange는 삭제 되지 않는다.
        return new Queue(CHAT_QUEUE_NAME, true);
    }

    /**
     * TopicExchange: 메시지의 Routing Key와 Binding Key를 패턴 매칭을 통해 비교하여 메시지를 라우팅하는 역할
     *                메시지의 Routing Key와 Binding Key가 일치하면, 해당 메시지가 연결된 Queue로 전달됨.
     *
     * @return "chat.exchange"라는 이름의 Exchange 생성
     */
    @Bean
    public TopicExchange chatExchange() {

        return new TopicExchange(CHAT_EXCHANGE_NAME);
    }

    /**
     * Exchange와 Queue를 특정 라우팅 키로 연결하는 역할.
     *
     * @param chatQueue "chat.queue"
     * @param chatExchange TopicExchage "chat.exchange", 메시지를 특정 패턴(Routing Key)에 따라 라우팅하는 역할
     * @return "chat.queue"에 "chat.exchange" 규칙을 Binding한다.
     */
    @Bean
    public Binding chatBinding(Queue chatQueue, TopicExchange chatExchange) {

        return BindingBuilder
                .bind(chatQueue) // Queue를 바인딩할 대상 지정
                .to(chatExchange) // TopicExchange를 지정
                .with(CHAT_ROUTING_KEY); // 라우팅 키 설정 -> 이 키와 일치하는 메시지만 Queue로 전달
    }

    /**
     * CachingConnectionFactory: RabbitMQ와의 연결을 효율적으로 관리하기 위해 커넥션 풀링 및 캐싱 기능을 제공
     *
     * @return RabbitMQ와의 연결을 관리하는 ConnectionFactory를 생성
     */
    @Bean
    public ConnectionFactory createConnectionFactory() {

        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(RABBITMQ_HOST);
        factory.setUsername("guest"); // RabbitMQ 관리자 아이디
        factory.setPassword("guest"); // RabbitMQ 관리자 비밀번호
        factory.setPort(5672); // RabbitMQ 연결할 port
        factory.setVirtualHost("/"); // vhost 지정

        return factory;
    }

    /**
     * @return RabbitMQ로 메시지를 송신하거나 수신할 때 사용하는 템플릿 클래스를 생성
     */
    @Bean
    public RabbitTemplate rabbitTemplate() {

        RabbitTemplate rabbitTemplate = new RabbitTemplate(createConnectionFactory()); // RabbitTemplate이 사용할 연결 팩토리를 지정
        rabbitTemplate.setMessageConverter(messageConverter()); // 메시지 처리와 직렬화/역직렬화를 자동으로 처리하는 Converter 설정
        return rabbitTemplate;
    }

    /**
     * @param connectionFactory RabbitMQ와의 연결을 관리하는 연결 팩토리
     * @return RabbitMQ에 자동으로 자원을 등록
     */
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {

        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.declareExchange(chatExchange()); // Exchange 등록
        rabbitAdmin.declareQueue(chatQueue()); // Queue 등록
        rabbitAdmin.declareBinding(chatBinding(chatQueue(), chatExchange())); // Binding 등록
        return rabbitAdmin;
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter(); // 메시지를 JSON으로 직렬/역직렬화
    }

    /**
     * RabbitMQConsumer
     * 이 기능을 통해서 메시지를 저장, 모니터링을 비동기적으로 처리 가능
     *
     * @param connectionFactory RabbitMQ 연결 팩토리
     * @param messageConverter Json 데이터 직렬화/역직렬화
     *//*
    @Bean
    public SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory(ConnectionFactory connectionFactory, MessageConverter messageConverter) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }*/
}