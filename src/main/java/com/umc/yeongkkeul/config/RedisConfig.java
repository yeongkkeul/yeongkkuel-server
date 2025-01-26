package com.umc.yeongkkeul.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * RedisConfig 클래스
 * Redis 데이터베이스의 설정을 관리하는 Configuration 클래스.
 * application.yml에 정의된 Redis 설정 정보를 주입받아 사용합.
 */
@Configuration
public class RedisConfig {

    private final String REDIS_HOST; // Redis 서버의 호스트 주소
    private final int REDIS_PORT; // Redis 서버의 포트 번호
    private final String REDIS_PASSWORD; // Redis 서버의 비밀번호

    public RedisConfig(
            @Value("${spring.data.redis.host}") String REDIS_HOST,
            @Value("${spring.data.redis.port}") int REDIS_PORT,
            @Value("${spring.data.redis.password}") String REDIS_PASSWORD)
    {

        this.REDIS_HOST = REDIS_HOST;
        this.REDIS_PORT = REDIS_PORT;
        this.REDIS_PASSWORD = REDIS_PASSWORD;
    }

    /**
     * RedisConnectionFactory 빈 정의.
     * Redis 서버와의 물리적 연결을 생성 및 관리합니다.
     * LettuceConnectionFactory를 사용하여 Redis와 연결합니다.
     *
     * @return RedisConnectionFactory - Redis와의 연결 팩토리 객체
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {

        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(REDIS_HOST, REDIS_PORT);
        connectionFactory.setPassword(REDIS_PASSWORD);  // 비밀번호 설정
        return connectionFactory;
    }

    /**
     * RedisTemplate 빈 정의.
     * Redis와의 데이터 작업을 수행하기 위한 주요 도구로 사용.
     * 다양한 Redis 데이터 구조(String, Hash 등)를 쉽게 다룰 수 있도록 지원.
     * Operations 을 제공해 Redis의 데이터에 접근.
     *
     * @return RedisTemplate<String, Object> - Redis 데이터 작업용 템플릿 객체
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());

        template.setKeySerializer(RedisSerializer.string()); // Key 직렬화 방식 설정 (문자열 기반)
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer()); // Value 직렬화 방식 설정 (JSON 직렬화)

        template.setHashKeySerializer(RedisSerializer.string()); // Hash Key 직렬화 방식 설정 (문자열 기반)
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer()); // Hash Value 직렬화 방식 설정 (JSON 직렬화)

        return template;
    }
}