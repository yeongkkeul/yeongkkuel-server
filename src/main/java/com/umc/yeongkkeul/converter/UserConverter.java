package com.umc.yeongkkeul.converter;

import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.web.dto.chat.ChatRoomUserInfos;

import java.util.ArrayList;
import java.util.List;

public class UserConverter {

    public static List<ChatRoomUserInfos.ChatRoomUserInfo> toChatRoomUserInfos(User myUser, User hostUser, List<User> users) {

        List<User> userInfos = new ArrayList<>();
        userInfos.add(myUser);
        userInfos.add(hostUser);
        userInfos.addAll(users);

        return userInfos.stream()
                .map(ChatRoomUserInfos::of)
                .toList();
    }
}
