package com.umc.yeongkkeul.socket;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class SocketConnectionTracker {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String ONLINE_KEY_PREFIX = "socket:online:";

    public SocketConnectionTracker(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 사용자를 온라인 상태로 표시합니다.
     */
    public void setUserOnline(Long userId) {
        redisTemplate.opsForValue().set(ONLINE_KEY_PREFIX + userId, true);
    }

    /**
     * 사용자를 오프라인 상태로 표시합니다.
     */
    public void setUserOffline(Long userId) {
        redisTemplate.delete(ONLINE_KEY_PREFIX + userId);
    }

    /**
     * 사용자의 연결 상태(온라인 여부)를 반환합니다.
     */
    public boolean isUserOnline(Long userId) {
        Object status = redisTemplate.opsForValue().get(ONLINE_KEY_PREFIX + userId);
        return status != null && (Boolean) status;
    }
}