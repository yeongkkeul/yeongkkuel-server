package com.umc.yeongkkeul.web.dto.chat;

import com.umc.yeongkkeul.domain.User;
import lombok.Builder;

import java.util.List;

@Builder
public record ChatRoomUserInfos(
        List<ChatRoomUserInfo> userInfos
) {

    @Builder
    public record ChatRoomUserInfo(
            Long userId,
            String userName
    ) {}

    public static ChatRoomUserInfo of(User user) {

        return ChatRoomUserInfo.builder()
                .userId(user.getId())
                .userName(user.getNickname())
                .build();
    }
}
