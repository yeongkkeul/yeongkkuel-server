package com.umc.yeongkkeul.socket;

import com.umc.yeongkkeul.security.FindLoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class SocketSessionInterceptor implements ChannelInterceptor {

    private final SocketConnectionTracker tracker;

    @Autowired
    public SocketSessionInterceptor(SocketConnectionTracker tracker) {
        this.tracker = tracker;
    }

    /**
     * 클라이언트에서 들어오는 메시지를 가로채어 STOMP 명령어에 따라
     * 온라인/오프라인 상태를 업데이트합니다.
     *
     * CONNECT: 클라이언트 연결 시, Principal을 통해 사용자 이메일을 얻고,
     *          이를 FindLoginUser.toId()로 변환해 사용자 ID를 획득한 후 온라인 상태로 기록합니다.
     *
     * DISCONNECT: 클라이언트 연결 종료 시, 동일한 방식으로 사용자 ID를 획득한 후 오프라인 상태로 처리합니다.
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // STOMP 헤더 정보를 래핑합니다.
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (accessor.getCommand() == null) {
            return message;
        }

        // STOMP 명령어에 따라 처리합니다.
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            if (accessor.getUser() != null) {
                // REST API와 동일하게 FindLoginUser를 통해 사용자 ID를 확인합니다.
                String email = accessor.getUser().getName();
                Long userId = FindLoginUser.toId(email);
                tracker.setUserOnline(userId);
                System.out.println("User " + userId + " is now ONLINE.");
            }
        } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            if (accessor.getUser() != null) {
                String email = accessor.getUser().getName();
                Long userId = FindLoginUser.toId(email);
                tracker.setUserOffline(userId);
                System.out.println("User " + userId + " is now OFFLINE.");
            }
        }
        return message;
    }
}